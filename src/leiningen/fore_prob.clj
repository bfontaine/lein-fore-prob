(ns leiningen.fore-prob
  "Populate the current project with a 4clojure problem."
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string  :as cs]
            [jsoup.soup      :as soup]))

;; == Formatting helpers ==

(defn- indent
  "Return a string representing the given level of indentation"
  ([] (indent 1))
  ([lvl]
    (apply str (repeat lvl "  "))))

(def ^:private test-template
  [ ";; problem " :prob-num "\n"
    "(deftest can-" :prob-fn "\n"
      :tests ")\n" ])

(def ^:private solution-template
  [ ";; problem " :prob-num " (" :difficulty ")\n"
    "(defn " :prob-fn "-solution\n"
    (indent) "[& args] ;; update args as needed\n"
    :description
    (indent) "nil)\n" ])

(defn- mk-template
  "return a string from a template and a map"
  [tpl kv]
  (apply str (map #(if (keyword? %) (kv %) %) tpl)))

(defn- prob->fn
  "Convert a problem into a valid function name"
  [prob]
  (.toLowerCase
    (-> prob
      :title
      (cs/replace #"^\W+|\W+$" "") ; trim special chars
      (cs/replace #"\W+" "-"))))

(defn- strip-html
  "strip HTML tags from a string"
  [html]
  ;; Jsoup doesn’t preserve newlines, we’re using a little trick inspired
  ;; of http://stackoverflow.com/a/6031463/735926 -- we replace newlines with
  ;; a special word, strip HTML then replace each special word back to a
  ;; newline.
  (let [special-word "xZ%q9a"]
    (->
      html
      (cs/replace #"(?i)(?:<br[^>]*>|\r?\n)\s*" special-word)
      (soup/parse)
      .text
      (cs/replace (re-pattern special-word) "\n"))))


(defn- desc->comments
  "format a problem description as Clojure comments with one level of
   indentation"
  ([desc] (desc->comments desc 1))
  ([desc i]
    (if (empty? desc)
      ""
      (->
        desc
        (strip-html)
        (cs/replace #"\r?\n" (str "\n" (indent i) ";; "))
        (#(str (indent i) ";; " % "\n"))))))

(defn- expand-prob-tests
  "expand a problem’s tests string"
  [prob]
  (let [prob-fn (str (prob->fn prob) "-solution")]
    (->>
      (prob :tests)
      (map #(cs/replace % #"\b__\b" prob-fn))
      (map #(cs/replace % #"\r\n" "\n"))
      (map #(str (indent) "(is " % ")")) ; wrap tests in 'is calls
      (cs/join "\n"))))

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
    (cs/replace #"(?m)^\(deftest replace-me[^)]+\)+$" "")
    ;; lein2
    (cs/replace #"(?m)^\(deftest a-test\s+\(testing \"FIXME[^)]+\)+$" "")))

(defn- get-src
  "return the content of the main source file of the current project"
  [project]
  (slurp (src-path project)))

(defn- has-problem-tests?
  "test if the current project has tests for a given problem"
  [tests probfn]
  (boolean (re-find (re-pattern (str "\\(" probfn "\\b")) tests)))

(defn- has-problem-src?
  "test if the current project has a given function"
  [src probfn]
  (boolean (re-find (re-pattern (str "\\(defn " probfn "\\b")) src)))

(defn- write-problem-tests
  "write tests for a given problem in the current project"
  [project existing-tests prob]
  (spit (tests-path project)
        (str existing-tests
             (mk-template
               (cons "\n\n" test-template)
               {:prob-fn (prob->fn prob)
                :tests (expand-prob-tests prob)
                :prob-num (prob :prob-num)}))))

(defn- write-problem-src
  "add a given problem function in the current project"
  [project prob]
  (spit (src-path project)
        (mk-template
          (cons "\n\n" solution-template)
          (merge prob
            {:prob-fn (prob->fn prob)
             :description (-> prob :description desc->comments)}))
        :append true))

(defn- write-prob
  "write a problem source and tests"
  [project prob]
  (let [tests  (get-tests project)
        src    (get-src   project)
        probfn (prob->fn prob)]
    (if-not (has-problem-tests? tests probfn)
      (write-problem-tests project tests prob)
      (println "tests already exist, skipping."))
    (if-not (has-problem-src? src probfn)
      (write-problem-src project prob)
      (println "source already exists, skipping."))))

;; == 4clojure API interaction ==

(def fore-url "http://4clojure.com/api/problem/")

(defn- prob-url
  "return an API URL for a problem number. This doesn’t check that the URL is
   valid"
  [n]
  (str fore-url n))

(defn- fetch-prob-data
  "return a problem map from its number using 4clojure API"
  [n]
  (let [req (http/get (prob-url n) {:as :json
                                    :throw-exceptions false})]
    (if (= (req :status) 200)
      (assoc
        ;; we don't need scores here and it's polluting debug logs
        (dissoc (req :body) :scores)
        :prob-num n))))

(defn- add-prob
  "add a problem to the current project from its number"
  [project prob-num]
  (if-let [prob (fetch-prob-data prob-num)]
    (try
      (write-prob project prob)
      (println "Problem" (str \" (prob :title) \") "added!")
      (catch Exception e
        (println "An error occured when writing the problem."
                 (. e getMessage))))
    (println (str "Cannot get problem " prob-num "."))))

;; == main function ==

(defn fore-prob
  "main function, used by leiningen"
  [project & prob-nums]
  (doseq [n prob-nums]
    (add-prob project n)))
