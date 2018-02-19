(defproject lein-fore-prob "0.1.3-SNAPSHOT"
  :description "Leiningen plugin to populate a project from a 4clojure problem."
  :url "https://github.com/bfontaine/lein-fore-prob"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http              "3.7.0"]
                 [clj-soup/clojure-soup "0.1.3"]]
  :profiles {:dev {:dependencies [[cheshire      "5.8.0"]
                                  [clj-http-fake "1.0.3"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             }
  :eval-in-leiningen true
  :plugins [[lein-cloverage "1.0.2"]])
