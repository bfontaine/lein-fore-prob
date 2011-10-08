(defproject foreclojure-plugin "1.0.0-SNAPSHOT"
  :description "A leiningen plugin to populate a project from a 4clojure problem."
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]])

(ns leiningen.foreclojure-problem
  ; WARNING: bytes already refers to: #'clojure.core/bytes in namespace: leiningen.foo, being replaced by: #'clojure.contrib.http.agent/bytes
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

; Format too?
(defn expand-prob [prob tests]
  (->>
   (map #(cs/replace % #"\b__\b" (str prob "-solution")) tests)
   (map #(cs/replace % "\\n" "\n"))
   (clojure.string/join "\n")))

(defn write-tests [project problem]
  (let [prob (title->fn (problem :title))]
    (spit (file "test" (ns->path (project :group)) "test" "core.clj")
          (str "\n(deftest can-" prob "\n" (expand-prob prob (problem :tests)) "\n)\n")
          :append true)))

(defn foreclojure-problem [project & args]
  (let [json (read-json (string (http-agent "http://localhost/example.json" :method "GET")))]
    (write-tests project json)))

; {:compile-path "/home/dbrook/dev/4clojure/foreclojure-plugin/classes", :group "foreclojure-plugin", :source-path "/home/dbrook/dev/4clojure/foreclojure-plugin/src", :dependencies [[org.clojure/clojure "1.2.1"] [org.clojure/clojure-contrib "1.2.0"]], :dev-dependencies nil, :name "foreclojure-plugin", :root "/home/dbrook/dev/4clojure/foreclojure-plugin", :jar-dir "/home/dbrook/dev/4clojure/foreclojure-plugin", :version "1.0.0-SNAPSHOT", :jar-exclusions [#"^\."], :test-path "/home/dbrook/dev/4clojure/foreclojure-plugin/test", :test-resources-path "/home/dbrook/dev/4clojure/foreclojure-plugin/test-resources", :target-dir "/home/dbrook/dev/4clojure/foreclojure-plugin", :uberjar-exclusions [#"^META-INF/DUMMY.SF"], :dev-resources-path "/home/dbrook/dev/4clojure/foreclojure-plugin/test-resources", :library-path "/home/dbrook/dev/4clojure/foreclojure-plugin/lib", :resources-path "/home/dbrook/dev/4clojure/foreclojure-plugin/resources", :native-path "/home/dbrook/dev/4clojure/foreclojure-plugin/native", :description "FIXME: write description"}
