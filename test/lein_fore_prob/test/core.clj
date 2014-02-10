(ns lein-fore-prob.test.core
  (:use clojure.test
        clj-http.fake)
  (:require [leiningen.fore-prob :as fp]
            [cheshire.core       :as json])
  (:import  [java.io File]))

(deftest project->path
  (testing "empty ns"
    (is (= (#'fp/project->path {:group ""}) "")))
  (testing "no dots"
    (is (= (#'fp/project->path {:group "foobar"}) "foobar")))
  (testing "ns without hyphens"
    (is (= (#'fp/project->path {:group "foo.bar"}) "foo/bar")))
  (testing "ns with an hyphen"
    (is (= (#'fp/project->path {:group "foo-bar.qux"}) "foo_bar/qux")))
  (testing "ns with multiple hyphens"
    (is (= (#'fp/project->path {:group "foo-bar-qux.foo"}) "foo_bar_qux/foo")))
  (testing "ns with multiple hyphens & dots"
    (is (= (#'fp/project->path {:group "a.foo-bar-qux.foo"})
           "a/foo_bar_qux/foo"))))

(deftest prob->fn
  (testing "one word"
    (is (= (#'fp/prob->fn {:title "foo"}) "foo")))
  (testing "two words"
    (is (= (#'fp/prob->fn {:title "foo bar"}) "foo-bar")))
  (testing "multiple words"
    (is (= (#'fp/prob->fn {:title "foo bar qux"}) "foo-bar-qux")))
  (testing "capitalized word"
    (is (= (#'fp/prob->fn {:title "Foo"}) "foo")))
  (testing "mixed case words"
    (is (= (#'fp/prob->fn {:title "FoO bAR QuX"}) "foo-bar-qux")))
  (testing "special char"
    (is (= (#'fp/prob->fn {:title "a*b"}) "a-b")))
  (testing "multiple special chars"
    (is (= (#'fp/prob->fn {:title "a***b"}) "a-b")))
  (testing "ending with special chars"
    (is (= (#'fp/prob->fn {:title "I got 99$:)"}) "i-got-99")))
  (testing "starting with special chars"
    (is (= (#'fp/prob->fn {:title "$ is the dollar symbol"})
           "is-the-dollar-symbol"))))

(deftest indent
  (testing "no arg"
    (is (= (#'fp/indent) "  ")))
  (testing "negative count"
    (is (= (#'fp/indent -42) "")))
  (testing "zero"
    (is (= (#'fp/indent 0) "")))
  (testing "one"
    (is (= (#'fp/indent 1) "  ")))
  (testing "more than one"
    (is (= (#'fp/indent 3) "      "))))

(deftest desc->comments
  (testing "empty description"
    (is (= (#'fp/desc->comments "") "")))
  (testing "one line"
    (is (= (#'fp/desc->comments "foobar") "  ;; foobar\n")))
  (testing "one line zero indentation"
    (is (= (#'fp/desc->comments "foobar" 0) ";; foobar\n")))
  (testing "multiple lines"
    (is (= (#'fp/desc->comments "foo\nbar") "  ;; foo\n  ;; bar\n")))
  (testing "multiple lines with zero indentation"
    (is (= (#'fp/desc->comments "foo\nbar" 0) ";; foo\n;; bar\n")))
  (testing "multiple lines with and indentation=2"
    (is (= (#'fp/desc->comments "foo\nbar" 2) "    ;; foo\n    ;; bar\n")))
  (testing "UNIX line-ending only"
    (is (= (re-find #"\r\n" (#'fp/desc->comments "foo\r\nbar\nq\r\na")) nil))))

(def sep (File/separator))

(def   src-dir "src")
(def tests-dir "test")

(def tests-file "core_test.clj")
(def   src-file "core.clj")

(defn mk-path
  "tests helper: make a path from one or more string(s)"
  [& parts]
  (apply str (interpose sep parts)))

(deftest tests-path
  (testing "class"
    (is (= (type (#'fp/tests-path {:group "foo"})) java.io.File)))
  (testing "empty namespace"
    (is (= (. (#'fp/tests-path {:group ""}) getPath)
           (mk-path tests-dir tests-file))))
  (testing "one-word namespace"
    (is (= (. (#'fp/tests-path {:group "foo"}) getPath)
           (mk-path tests-dir "foo" tests-file))))
  (testing "namespace with dots"
    (is (= (. (#'fp/tests-path {:group "foo.bar"}) getPath)
           (mk-path tests-dir "foo" "bar" tests-file))))
  (testing "dashed namespace"
    (is (= (. (#'fp/tests-path {:group "foo-bar"}) getPath)
           (mk-path tests-dir "foo_bar" tests-file))))
  (testing "dashed namespace with dots"
    (is (= (. (#'fp/tests-path {:group "foo-bar.qux"}) getPath)
           (mk-path tests-dir "foo_bar" "qux" tests-file)))))

(deftest src-path
  (testing "class"
    (is (= (type (#'fp/src-path {:group "foo"})) java.io.File)))
  (testing "empty namespace"
    (is (= (. (#'fp/src-path {:group ""}) getPath)
           (mk-path src-dir src-file))))
  (testing "one-word namespace"
    (is (= (. (#'fp/src-path {:group "foo"}) getPath)
           (mk-path src-dir "foo" src-file))))
  (testing "namespace with dots"
    (is (= (. (#'fp/src-path {:group "foo.bar"}) getPath)
           (mk-path src-dir "foo" "bar" src-file))))
  (testing "dashed namespace"
    (is (= (. (#'fp/src-path {:group "foo-bar"}) getPath)
           (mk-path src-dir "foo_bar" src-file))))
  (testing "dashed namespace with dots"
    (is (= (. (#'fp/src-path {:group "foo-bar.qux"}) getPath)
           (mk-path src-dir "foo_bar" "qux" src-file)))))

(def prob1
  {:title "Foo Bar"
   :description "write a foo bar"
   :difficulty "Medium"
   :restricted []
   :times-solved 42
   :scores {}
   :user "foo"
   :tags ["bar"]
   :tests ["(= (__ 42) 21)" "(= (__ 21) 42)"]})

(deftest expand-prob-tests
  (testing "no tests"
    (is (= (#'fp/expand-prob-tests (assoc prob1 :tests [])) "")))
  (testing "one test with no function call"
    (is (= (#'fp/expand-prob-tests (assoc prob1 :tests ["true"]))
           "  (is true)")))
  (testing "two tests with no function call"
    (is (= (#'fp/expand-prob-tests (assoc prob1 :tests ["true" "true"]))
           "  (is true)\n  (is true)")))
  (testing "one test with function call, no arguments"
    (is (= (#'fp/expand-prob-tests (assoc prob1 :tests ["(= (__) 2)"]))
           "  (is (= (foo-bar-solution) 2))")))
  (testing "one test with function call, one argument"
    (is (= (#'fp/expand-prob-tests (assoc prob1 :tests ["(= (__ 42) 3)"]))
           "  (is (= (foo-bar-solution 42) 3))")))
  (testing "one test with function call, multiple arguments"
    (is (= (#'fp/expand-prob-tests
             (assoc prob1 :tests ["(= (__ 42 \"bar\") 4)"]))
           "  (is (= (foo-bar-solution 42 \"bar\") 4))")))
  (testing "two tests with function call, one argument"
    (is (= (#'fp/expand-prob-tests
             (assoc prob1 :tests ["(= (__ 2) 17)" "(= (__ 3) 42)"]))
           (str "  (is (= (foo-bar-solution 2) 17))\n"
                "  (is (= (foo-bar-solution 3) 42))"))))
  (testing "multiple tests with function call, multiple arguments"
    (is (= (#'fp/expand-prob-tests
             (assoc prob1
                    :tests
                    ["(= (__ 2 4) 17)" "(= (__ 3 1) 42)" "(= (__ 5 1) 3)"]))
           (str "  (is (= (foo-bar-solution 2 4) 17))\n"
                "  (is (= (foo-bar-solution 3 1) 42))\n"
                "  (is (= (foo-bar-solution 5 1) 3))"))))
  (testing "formatting newlines"
    (is (= (#'fp/expand-prob-tests
             (assoc prob1 :tests ["(= (__ \\a)\r\n42)"]))
           "  (is (= (foo-bar-solution \\a)\n42))"))
    (is (= (#'fp/expand-prob-tests
             (assoc prob1 :tests ["(= (__ \\a)\n42)"]))
           "  (is (= (foo-bar-solution \\a)\n42))"))))

(deftest has-problem-tests?
  (testing "empty content"
    (is (= (#'fp/has-problem-tests? "" "foo") false)))
  (testing "not found"
    (is (= (#'fp/has-problem-tests? "(do (+ 42 12) (bar))" "foo") false)))
  (testing "not found as a function call"
    (is (= (#'fp/has-problem-tests? "(do (+ foo 12) (bar))" "foo") false))
    (is (= (#'fp/has-problem-tests? "(def foo 12)" "foo") false))
    (is (= (#'fp/has-problem-tests? "(def bar \"foo\")" "foo") false)))
  (testing "in a function name"
    (is (= (#'fp/has-problem-tests? "(defn foobar [] 2)" "foo") false))
    (is (= (#'fp/has-problem-tests? "(defn barfoo [] 2)" "foo") false))
    (is (= (#'fp/has-problem-tests? "(defn barfooqux [] 2)" "foo") false))
    (is (= (#'fp/has-problem-tests? "(defn foo [] 2)" "foo") false)))
  (testing "found as a prefix in a function call"
    (is (= (#'fp/has-problem-tests? "(foobar 42)", "foo") false))
    (is (= (#'fp/has-problem-tests? "(barfoo 42)", "foo") false))
    (is (= (#'fp/has-problem-tests? "(barfooqux 42)", "foo") false)))
  (testing "found as a function call"
    (is (= (#'fp/has-problem-tests? "(foo 42)", "foo") true))
    (is (= (#'fp/has-problem-tests? "(foo)", "foo") true))))

(deftest has-problem-src?
  (testing "empty content"
    (is (= (#'fp/has-problem-src? "" "foo") false)))
  (testing "not found"
    (is (= (#'fp/has-problem-src? "(do (+ 42 12) (bar))" "foo") false)))
  (testing "not found as a function definition"
    (is (= (#'fp/has-problem-src? "(do (+ foo 12) (bar))" "foo") false))
    (is (= (#'fp/has-problem-src? "(def foo 12)" "foo") false))
    (is (= (#'fp/has-problem-src? "(def bar \"foo\")" "foo") false)))
  (testing "prefix of a function name"
    (is (= (#'fp/has-problem-src? "(defn foobar [] 2)" "foo") false)))
  (testing "suffix of a function name"
    (is (= (#'fp/has-problem-src? "(defn barfoo [] 2)" "foo") false)))
  (testing "in a function name"
    (is (= (#'fp/has-problem-src? "(defn barfooqux [] 2)" "foo") false)))
  (testing "found as a function name"
    (is (= (#'fp/has-problem-src? "(defn foo [] 2)" "foo") true))))

(deftest prob-url
  (testing "zero"
    (is (= (#'fp/prob-url 0) "http://4clojure.com/api/problem/0")))
  (testing "negative number"
    (is (= (#'fp/prob-url -5) "http://4clojure.com/api/problem/-5")))
  (testing "positive number"
    (is (= (#'fp/prob-url 1) "http://4clojure.com/api/problem/1"))
    (is (= (#'fp/prob-url 42) "http://4clojure.com/api/problem/42"))
    (is (= (#'fp/prob-url 133) "http://4clojure.com/api/problem/133"))))

(def ^{:doc "test helper: create an url for a problem number"} mk-url
  #'fp/prob-url)

(def fake-routes
  {(mk-url 1) (fn [r] {:status 404 :headers {} :body ""})
   (mk-url 2) (fn [r] {:status 500 :headers {} :body ""})
   (mk-url 3) (fn [r] {:status 200 :headers {}
                       :body (json/generate-string
                               {:restricted []
                                :title "Foo Bar"
                                :times-solved 42
                                :difficulty "Elementary"
                                :scores {"45" 42}
                                :tests ["(= __ true)"]
                                :user "someone"
                                :description "This is a problem"
                                :tags []})})})

(deftest get-prob
  (with-fake-routes-in-isolation fake-routes
    (testing "404 error"
      (is (nil? (#'fp/get-prob 1))))
    (testing "500 error"
      (is (nil? (#'fp/get-prob 2))))
    (testing "success"
      (is (= (:title (#'fp/get-prob 3)) "Foo Bar")))))

(deftest get-tests
  (testing "empty file"
    (is (= (with-redefs [slurp (constantly "")] (#'fp/get-tests {:title "yo"}))
           "")))
  (testing "no test placeholder"
    (let [content "(a :fun :call) (deftest \"foo\" (is :a :test))"]
      (is (= (with-redefs [slurp (constantly content)]
               (#'fp/get-tests {:title "yo"}))
             content))))
  (testing "test placeholder"
    (let [content (str "(deftest a-test\n"               ; default lein2
                       "  (testing \"FIXME, I fail.\"\n" ; test placeholder
                       "    (is (= 0 1))))")]
      (is (= (with-redefs [slurp (constantly content)]
             ""))))))

(deftest get-src
  (testing "empty file"
    (is (= (with-redefs [slurp (constantly "")] (#'fp/get-src {:title "yo"})))
        ""))
  (is (= (with-redefs [slurp (constantly "xY3")] (#'fp/get-src {:title "yo"})))
      "xY3"))

(deftest write-problem-tests
  (with-redefs [spit (fn [f code & _]
                       (is (= "test/foo/core_test.clj" (. f getPath)))
                       (is (= (str "\n\n"
                                   "(deftest can-foo-bar\n"
                                   "  (is (= (foo-bar-solution 42) 21))\n"
                                   "  (is (= (foo-bar-solution 21) 42)))\n")
                              code)))]
    (#'fp/write-problem-tests {:group "foo"} "" prob1)))

(deftest write-problem-src
  (with-redefs [spit (fn [f code & _]
                       (is (= "src/foo/core.clj" (. f getPath)))
                       (is (= (str "\n\n"
                                   "(defn foo-bar-solution\n"
                                   "  [& args] ;; update args as needed\n"
                                   "  ;; write a foo bar\n"
                                   "  nil)\n")
                              code)))]
    (#'fp/write-problem-src {:group "foo"} prob1)))

(deftest write-prob)

(deftest fore-prob)
