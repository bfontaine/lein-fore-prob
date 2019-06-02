(defproject lein-fore-prob "0.1.3-SNAPSHOT"
  :description "Leiningen plugin to populate a project from a 4clojure problem."
  :url "https://github.com/bfontaine/lein-fore-prob"
  :license {:name "Eclipse Public License"
            :url "https://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http              "3.10.0"]
                 [clj-soup/clojure-soup "0.1.3"]]
  :profiles {:dev {}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10 {:dependencies [[org.clojure/clojure "1.10.0"]]}}
  :aliases {"all" ["with-profile" "dev,1.8:dev,1.9:dev,1.10:dev"]}
  :eval-in-leiningen true
  :plugins [[lein-cloverage "1.0.2"]])
