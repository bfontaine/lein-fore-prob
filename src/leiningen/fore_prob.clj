(ns leiningen.fore-prob
  "Populate the current project with a 4clojure problem."
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string  :as cs]))

;; == Formatting helpers ==

(defn- indent
  "Return a string representing the given level of indentation"
  ([] (indent 1))
  ([lvl]
    (apply str (repeat lvl "  "))))

(def ^{:private true} test-template
  [ "(deftest can-" :prob-fn "\n"
      :tests ")\n" ])

(def ^{:private true} solution-template
  [ "(defn " :prob-fn "-solution\n"
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

(defn- desc->comments
  "format a problem description as Clojure comments with one level of
   indentation"
  ([desc] (desc->comments desc 1))
  ([desc i]
    (if (empty? desc)
      ""
      (str
        (indent i) ";; "
        (cs/replace desc #"\r?\n" (str "\n" (indent i) ";; ")) "\n"))))

(defn- expand-prob-tests
  "expand a problem’s tests string"
  [prob tests]
  (->>
   (map #(cs/replace % #"\b__\b" (str prob "-solution")) tests)
   (map #(cs/replace % #"\r\n" "\n"))
   (map #(str (indent) "(is " % ")")) ; wrap tests in 'is calls
   (cs/join "\n")))

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
  (cs/replace (slurp (tests-path)) #"(?m)^\(deftest replace-me[^)]+\)+$" ""))

(defn- get-src
  "return the content of the main source file of the current project"
  [project]
  (slurp (src-path)))

(defn- has-problem-tests?
  "test if the current project has tests for a given problem"
  [tests problem]
  (boolean (re-find (re-pattern (str "\\(" (prob->fn problem) "\\b")))))

(defn- has-problem-src?
  "test if the current project has the function for a given problem"
  [src problem]
  (boolean (re-find (re-pattern (str "\\(defn " (prob->fn problem))) src)))

(defn- write-problem-tests
  "write tests for a given problem in the current project"
  [project existing-tests prob]
  (spit
    (tests-path)
    (str existing-tests
         (mk-template
           (cons "\n\n" test-template)
           {:prob-fn prob
            :tests (expand-prob-tests prob (prob :tests))}))))

(defn- write-problem-src
  "add a function for a given problem in the current project"
  [project prob]
  (spit (src-path)
        (mk-template
          (cons "\n\n" solution-template)
          {:prob-fn prob
           :description (-> project :description desc->comments)})
        :append true))

(defn- write-prob
  "write a problem source and tests"
  [project prob]
  (let [tests (get-tests project)
        src   (get-src   project)]
    (if-not (has-problem-tests? tests prob)
      (write-problem-tests project tests prob)
      (println "tests already exist, skipping."))
    (if-not (has-problem-src? src prob)
      (write-problem-src project prob)
      (println "source already exist, skipping."))))

;; == 4clojure API interaction ==

(def fore-url "http://4clojure.com/api/problem/")

(defn get-prob
  "Return a problem map from its number using 4clojure API"
  [n]
  (let [req (http/get (str fore-url n) {:as :json
                                        :throw-exceptions false})]
    (if (= (req :status) 200)
      ;; we don't need scores here and it's polluting debug logs
      (dissoc (req :body) :scores))))

;; == main function ==

(defn fore-prob
  "main function, used by leiningen"
  [project prob-num]
  (if-let [prob (get-prob prob-num)]
    (println "Problem" (str \" (prob :title) \") "added!")
    (println (str "Cannot get problem " prob-num "."))))
