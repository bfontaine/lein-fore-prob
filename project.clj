(defproject lein-fore-prob "0.1.1"
  :description "A leiningen plugin to populate a project from a 4clojure problem."
  :url "https://github.com/bfontaine/lein-fore-prob"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-http              "0.7.8"]
                 [clj-soup/clojure-soup "0.1.1"]]
  :profiles {:dev {:dependencies [[cheshire      "5.3.1"]
                                  [clj-http-fake "0.7.8"]]}}
  :eval-in-leiningen true
  :plugins [[lein-cloverage "1.0.2"]])
