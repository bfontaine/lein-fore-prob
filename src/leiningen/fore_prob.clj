(ns leiningen.fore-prob
  "Populate the current project with a 4clojure problem."
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string  :as cs]))

;; Adapted from leiningen/src/leiningen/util/paths.clj
(defn- ns->path
  "Convert a namespace into a path"
  [n]
  (apply str (replace {\- \_
                       \. \/} n)))

(defn- title->fn
  "Convert a problem title into a valid function name"
  [title]
  (.toLowerCase
    (-> title
      (cs/replace #"^\W+|\W+$" "") ; trim special chars
      (cs/replace #"\W+" "-"))))

(defn- drop-replace-me
  "Remove 'replace-me' tests from a file"
  [fn]
  (let [t (slurp fn)
        r #"(?m)^\(deftest replace-me[^)]+\)+$"]
    (if (re-find r t)
      (spit fn (cs/replace t r "")))))

(defn- indent
  "Return a string representing the given level of indentation"
  ([] (indent 1))
  ([lvl]
    (apply str (repeat lvl "  "))))

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

(defn- add-stub [project prob]
  (let [src (io/file "src" (ns->path (project :group)) "core.clj")]
    (if-not (re-find (re-pattern (str "(?m)^\\(defn " prob)) (slurp src))
      (spit src
            (str "\n(defn " prob "-solution [] ; Update args as needed!\n"
                 (-> project :description desc->comments)
                 (indent) "nil)\n")
            :append true))))

(defn- no-test-yet
  "test if there are any fore-prob tests yet"
  [fn prob]
  (nil? (re-find (re-pattern (str "(?m)^\\(deftest can-" prob)) (slurp fn))))

(defn- enquote [s]
  "Allow something that might have quotes to live in a string"
  [s]
  (cs/replace s  "\"" "\\\""))

;; XXX - Format too?
(defn- expand-prob
  "expand a problem string"
  [prob tests]
  (->>
   ;; TODO factorize this '-solution' appending in a function
   (map #(cs/replace % #"\b__\b" (str prob "-solution")) tests)
   (map #(cs/replace % #"\r\n" "\n"))
   (map #(cs/join " " [(str (indent) "(is") % "\"" (enquote %) "\")"]))
   (cs/join "\n")))

(defn- write-tests
  "write tests to the main tests file (core_test.clj)"
  [project problem]
  (let [prob      (title->fn (problem :title))
        test-file (io/file
                    "test" (ns->path (project :group)) "core_test.clj")]
    (add-stub project prob)
    (drop-replace-me test-file)    
    (if (no-test-yet test-file prob)
      (spit test-file
            (str "\n(deftest can-" prob "\n"
                 (expand-prob prob (problem :tests)) ")\n")
            :append true)
      true)))

(def fore-url "http://4clojure.com/api/problem/")

(defn get-prob
  "Return a problem map from its number using 4clojure API"
  [n]
  (let [req (http/get (str fore-url n) {:as :json
                                        :throw-exceptions false})]
    (if (= (req :status) 200)
      ;; we don't need scores here and it's polluting debug logs
      (dissoc (req :body) :scores))))

(defn fore-prob
  "main function, used by leiningen"
  [project prob-num]
  (if-let [prob (get-prob prob-num)]
    (try
      (let [title (prob :title)]
        (if-not (write-tests project prob) ; spit returns nil by default hence
                                           ; the inversion of logic.
          (println (str "Problem \"" title "\" added!"))
          (println (str "Problem \"" title "\" has already been added!"))))
      (catch Exception e
        (println (str "Failed setting problem " prob-num ": " (.getMessage e)))))
    (println "Cannot get problem" prob-num "(HTTP status != 200).")))
