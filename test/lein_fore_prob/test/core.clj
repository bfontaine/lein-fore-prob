(ns lein-fore-prob.test.core
  (:use clojure.test)
  (:require [leiningen.fore-prob :as fp]))

(deftest ns->path
  (testing "empty ns"
    (is (= (#'fp/ns->path "") "")))
  (testing "no dots"
    (is (= (#'fp/ns->path "foobar") "foobar")))
  (testing "ns without hyphens"
    (is (= (#'fp/ns->path "foo.bar") "foo/bar")))
  (testing "ns with an hyphen"
    (is (= (#'fp/ns->path "foo-bar.qux") "foo_bar/qux")))
  (testing "ns with multiple hyphens"
    (is (= (#'fp/ns->path "foo-bar-qux.foo") "foo_bar_qux/foo")))
  (testing "ns with multiple hyphens & dots"
    (is (= (#'fp/ns->path "a.foo-bar-qux.foo") "a/foo_bar_qux/foo"))))

(deftest title->fn
  (testing "one word"
    (is (= (#'fp/title->fn "foo") "foo")))
  (testing "two words"
    (is (= (#'fp/title->fn "foo bar") "foo-bar")))
  (testing "multiple words"
    (is (= (#'fp/title->fn "foo bar qux") "foo-bar-qux")))
  (testing "capitalized word"
    (is (= (#'fp/title->fn "Foo") "foo")))
  (testing "mixed case words"
    (is (= (#'fp/title->fn "FoO bAR QuX") "foo-bar-qux")))
  (testing "special char"
    (is (= (#'fp/title->fn "a*b") "a-b")))
  (testing "multiple special chars"
    (is (= (#'fp/title->fn "a***b") "a-b")))
  (testing "ending with special chars"
    (is (= (#'fp/title->fn "I got 99$:)") "i-got-99")))
  (testing "starting with special chars"
    (is (= (#'fp/title->fn "$ is the dollar symbol") "is-the-dollar-symbol"))))

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

(deftest enquote
  (testing "empty string"
    (is (= (#'fp/enquote "") "")))
  (testing "no quotes"
    (let [s "foo bar _qux $"]
      (is (= (#'fp/enquote s) s))))
  (testing "quotes"
    (is (= (#'fp/enquote "\"foo\"") "\\\"foo\\\""))))

(deftest expand-prob) ; TODO

;; TODO we need to use sample and/or temporary files for these ones

(deftest drop-replace-me)
(deftest add-stub)
(deftest no-test-yet)
(deftest write-tests)

;; TODO we need to mock clj-http for this one

(deftest get-prob)

;; TODO we need both requirements for this one

(deftest fore-prob)
