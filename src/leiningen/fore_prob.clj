(ns leiningen.fore-prob
  "Populate the current project with a 4clojure problem."
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [jsoup.soup :as soup]
            [clojure.java.browse :as browse]))

;; == Formatting helpers ==

(def ^:private indent
  "  ")

(def ^:private test-template
  (str/join "\n"
            [";; problem {prob-num}"
             "(deftest {prob-fn}-solution-test"
             "{tests})"
             ""]))

(def ^:private solution-template
  (str/join "\n"
            [";; problem {prob-num} ({difficulty}){restrictions-str}"
             "(def {prob-fn}-solution"
             (str indent "(fn [& args] ;; update args as needed")
             "{description}"
             (str indent indent "nil))")
             ""]))

(defn- render-template
  [template values]
  (str/replace template
               #"\{([a-z-]+)}"
               (fn [[_ k]]
                 (str (get values (keyword k))))))

(defn- problem->fn
  "Convert a problem into a valid function name"
  [prob]
  ;; We only allow a subset of valid symbol chars not to have to deal with
  ;; special cases (e.g. `:` is valid only if not the first char). This subset
  ;; is sufficient to cover all problems for now.
  ;; https://github.com/edn-format/edn#symbols
  (let [pat "[^a-zA-Z0-9=><&%$?_!]+"]
    (-> prob
        :title
        (str)
        (.toLowerCase)
        (str/replace (re-pattern
                       (str "^" pat "|" pat "$")) "")                 ; trim special chars
        (str/replace (re-pattern pat) "-"))))

(defn- strip-html
  "strip HTML tags from a string"
  [html]
  ;; Jsoup doesn’t preserve newlines; we’re using a little trick inspired
  ;; of http://stackoverflow.com/a/6031463/735926 -- we replace newlines with
  ;; a special word, strip HTML then replace each special word back to a newline.
  (let [special-word "xZ%q9a"]
    (->
      html
      (str/replace #"(?i)(?:<br[^>]*>|\r?\n)\s*" special-word)
      (soup/parse)
      .text
      (str/replace (re-pattern special-word) "\n"))))

(defn- description->comments
  "format a problem description as Clojure comments with two levels of indentation."
  [description]
  (if (empty? description)
    ""
    (str indent indent ";; "
         (-> description
             (strip-html)
             (str/replace #"\r?\n" (str "\n" indent indent ";; "))))))

(defn- expand-problem-tests
  "expand a problem’s tests string"
  [problem]
  (let [problem-fn (str (problem->fn problem) "-solution")]
    (->> (:tests problem)
         (map #(str/replace % #"\b__\b" problem-fn))
         (map #(str/replace % #"\r\n" "\n"))
         (map #(str indent "(is " % ")"))                             ; wrap tests in 'is calls
         (str/join "\n"))))

;; == Files handling ==

(defn- project->path
  "Convert a project's namespace into a path"
  [project]
  (apply str (replace {\- \_
                       \. \/} (project :group))))

(defn- tests-path
  "return a File object for the main tests file of the current project"
  [project]
  ;; this is the default test file when using `lein new`
  (io/file "test" (project->path project) "core_test.clj"))

(defn- src-path
  "return a File object for the main source file of the current project"
  [project]
  (io/file "src" (project->path project) "core.clj"))

(defn- get-tests
  "return the content of the main tests file of the current project, stripped
   out of any 'replace-me' placeholder test"
  [project]
  (->
    (slurp (tests-path project))
    ;; lein1
    (str/replace #"(?m)^\(deftest replace-me[^)]+\)+$" "")
    ;; lein2
    (str/replace #"(?m)^\(deftest a-test\s+\(testing \"FIXME[^)]+\)+$" "")))

(defn- get-src
  "return the content of the main source file of the current project"
  [project]
  (slurp (src-path project)))

(defn- has-problem-tests?
  "test if the current project has tests for a given problem"
  [tests problem-fn]
  (boolean (re-find (re-pattern (str "\\(" problem-fn "\\b")) tests)))

(defn- has-problem-src?
  "test if the current project has a given function"
  [src problem-fn]
  (boolean (re-find (re-pattern (str "\\(defn " problem-fn "\\b")) src)))

(defn- write-problem-tests!
  "write tests for a given problem in the current project"
  [project existing-tests prob]
  (spit (tests-path project)
        (str existing-tests
             (render-template
               (str "\n\n" test-template)
               {:prob-fn  (problem->fn prob)
                :tests    (expand-problem-tests prob)
                :prob-num (prob :prob-num)}))))

(defn- write-problem-src!
  "add a given problem function in the current project"
  [project problem]
  (spit (src-path project)
        (render-template
          (str "\n\n" solution-template)
          (merge problem
                 {:prob-fn          (problem->fn problem)
                  :description      (-> problem :description description->comments)
                  :restrictions-str (when-not (empty? (:restricted problem))
                                      (str "\n;; restrictions: "
                                           (str/join ", " (:restricted problem))))}))
        :append true))

(defn- write-problem!
  "write a problem source and tests"
  [project problem]
  (let [tests          (get-tests project)
        src            (get-src project)
        problem-fn     (problem->fn problem)
        tests-written? (if (has-problem-tests? tests problem-fn)
                         (do (println "tests already exist, skipping.") false)
                         (do (write-problem-tests! project tests problem) true))
        src-written?   (if (has-problem-src? src problem-fn)
                         (do (println "source already exists, skipping.") false)
                         (do (write-problem-src! project problem) true))]
    (or
      tests-written?
      src-written?)))

;; == 4clojure API interaction ==

(def ^:private fore-url "http://4clojure.com/api/problem/")

(defn- problem-url
  "return an API URL for a problem number. This doesn’t check that the URL is
   valid"
  [n]
  (str fore-url n))

(defn- fetch-problem-data
  "return a problem map from its number using 4clojure API"
  [n]
  (let [req (http/get (problem-url n) {:as               :json
                                       :throw-exceptions false})]
    (when (= (:status req) 200)
      (-> (:body req)
          ;; we don't need scores here and it's polluting debug logs
          (dissoc :scores)
          (assoc :prob-num n)))))

(defn- add-problem!
  "add a problem to the current project from its number"
  [project problem-number]
  (if-let [problem (fetch-problem-data problem-number)]
    (when (write-problem! project problem)
      (println "Problem" (str \" (:title problem) \") "added!"))
    (println (str "Cannot get problem " problem-number "."))))

;; Yes, no HTTPS
(def ^:private problem-url-root "http://www.4clojure.com/problem/")

(defn- open-problem-url
  "open one or more problem URLs in a browser"
  [problem-numbers]
  (doseq [n problem-numbers]
    (browse/browse-url (str problem-url-root n))))

;; == main function ==

(defn fore-prob
  "main function, used by leiningen"
  [project & problem-numbers]
  (if (= (first problem-numbers) "open")
    (open-problem-url (rest problem-numbers))
    (doseq [n problem-numbers]
      (add-problem! project n))))
