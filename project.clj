(defproject foreclojure-plugin "1.0.0-SNAPSHOT"
  :description "A leiningen plugin to populate a project from a 4clojure problem."
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]])

(ns leiningen.foreclojure-problem
  (:use [clojure.contrib.http.agent :only(http-agent string)]
        [clojure.contrib.json]
        [clojure.java.io :only [file]]
        [clojure.string :as cs :exclude [replace reverse]])
  )

;; Purloined from leiningen/src/leiningen/util/paths.clj
(defn ns->path [n]
  (str (.. (str n)
           (replace \- \_)
           (replace \. \/))))

(defn title->fn [title]
  (.toLowerCase (cs/replace title #"[^\w]" "-")))

(defn drop-replace-me [fn]
  (let [t (slurp fn)
        r #"(?m)^\(deftest replace-me[^)]+\)+$"]
    (if (re-find r t)
      (spit fn (cs/replace t r "")))))

(defn add-stub [project prob]
  (let [src (file "src" (ns->path (project :group)) "core.clj")]
    (if-not (re-find (re-pattern (str "(?m)^\\(defn " prob)) (slurp src))
      (spit src
            (str "\n(defn " prob "-solution [] ; Update args as needed!\nnil\n)\n")
            :append true))))

(defn no-test-yet [fn prob]
  (nil? (re-find (re-pattern (str "(?m)^\\(deftest can-" prob)) (slurp fn))))

;; Seeing these four characters repeated was making my eyes bleed.
(def Q "\"")
;; Allow something that might have quotes to live in a string.
(defn enquote [s]
  (cs/replace s  Q "\\\""))
;; XXX - Format too?
(defn expand-prob [prob tests]
  (->>
   (map #(cs/replace % #"\b__\b" (str prob "-solution")) tests)
   (map #(cs/replace % "\\r?\\n" "\n")) ; XXX \r isn't getting replaced.
   (map #(cs/join " " ["(is" % Q (enquote %) Q ")"]))
   (cs/join "\n")))

(defn write-tests [project problem]
  (let [prob      (title->fn (problem :title))
        test-file (file "test" (ns->path (project :group)) "test" "core.clj")]
    (add-stub project prob)
    (drop-replace-me test-file)    
    (if (no-test-yet test-file prob)
      (spit test-file
            (str "\n(deftest can-" prob "\n" (expand-prob prob (problem :tests)) "\n)\n")
            :append true)
      true)))

;; TODO - Turn into a proper plugin then upload it to clojars.
(defn foreclojure-problem [project prob-num]
  (try
    ;; TODO - Check HTTP status.
    (let [json (read-json (string (http-agent (str "http://4clojure.com/api/problem/" prob-num) :method "GET")))]
      (if-not (write-tests project json) ; spit returns nil by default hence the inversion of logic.
        (println (cs/join "" ["Problem " Q (json :title) Q " added!"]))
        (println (cs/join "" ["Problem " Q (json :title) Q " has already been added!"]))
      ))
    (catch Exception e
      (println (str "Failed setting problem " prob-num ": " (.getMessage e)))
      (shutdown-agents)
      )))

