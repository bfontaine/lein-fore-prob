(ns lein-fore-prob.test.core
  (:use clojure.test)
  (:require [leiningen.fore-prob :as fp]))

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

(def sample-prob1
  {:title "Foo Bar"
   :description "write a foo bar"
   :difficulty "Medium"
   :restricted []
   :times-solved 42
   :scores {}
   :user "foo"
   :tags ["bar"]
   :tests ["(= (__ 42) 21)" "(= (__ 21) 42)"]})

(deftest expand-prob-tests) ; TODO
(deftest tests-path)
(deftest src-path)
(deftest has-problem-tests?)
(deftest has-problem-src?)

;; TODO we need to use sample and/or temporary files for these ones
;; http://my.safaribooksonline.com/book/programming/clojure/9781449366384/4dot-local-io/_using_temporary_files_html

(deftest get-tests)
(deftest get-src)

(deftest write-problem-tests)
(deftest write-problem-src)

;; TODO we need to mock clj-http for this one
;; https://github.com/myfreeweb/clj-http-fake

(deftest get-prob)

;; TODO we need to use with-redefs for these ones

(deftest write-prob)
(deftest fore-prob)
