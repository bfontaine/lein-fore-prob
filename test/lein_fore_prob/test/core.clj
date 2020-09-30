(ns lein-fore-prob.test.core
  (:require [leiningen.fore-prob :as fp]
            [clojure.string :as cs]
            [clojure.java.browse :as browse]
            [clojure.test :refer :all])
  (:import [java.io File]))

;; samples & helpers

(def prob1
  {:title        "Foo Bar"
   :description  "write a foo bar"
   :difficulty   "Medium"
   :restricted   ["f1" "f2"]
   :times-solved 42
   :scores       {}
   :user         "foo"
   :tags         ["bar"]
   :tests        ["(= (__ 42) 21)" "(= (__ 21) 42)"]
   :prob-num     42})

(def project-foo {:group "foo"})

(def sep (File/separator))

(def src-dir "src")
(def tests-dir "test")

(def tests-file "core_test.clj")
(def src-file "core.clj")

(defn mk-path
  "tests helper: make a path from one or more string(s)"
  [& parts]
  (apply str (interpose sep parts)))

(def ^{:doc "test helper: create an url for a problem number"} mk-url
  #'fp/prob-url)

(defn assert-println
  "test helper: return a test to replace 'println' and check that a specific
   string is \"printed\""
  [s]
  (fn [& ss]
    (is (= (cs/join " " ss) s))))

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
    (is (= (#'fp/prob->fn {:title "I got 99$:)"}) "i-got-99$")))
  (testing "starting with special chars"
    (is (= (#'fp/prob->fn {:title "{ is the dollar symbol"})
           "is-the-dollar-symbol")))
  (testing "with special but valid chars"
    (is (= (#'fp/prob->fn {:title "a problem with ->"})
           "a-problem-with->"))
    (is (= (#'fp/prob->fn {:title "a problem with ->>"})
           "a-problem-with->>"))
    (is (= (#'fp/prob->fn {:title "%!"}) "%!")))
  (testing "with special chars including valid & invalid ones"
    (is (= (#'fp/prob->fn {:title "hey! the problem is here."})
           "hey!-the-problem-is-here"))))

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
    (is (= (re-find #"\r\n" (#'fp/desc->comments "foo\r\nbar\nq\r\na")) nil)))
  (testing "stripping HTML (#1)"
    (is (= (#'fp/desc->comments "foo <a href=\"/qux\">bar</a>")
           "  ;; foo bar\n"))
    (testing "stripping HTML while preserving newlines (#1)"
      (is (= (#'fp/desc->comments "foo\n <a href=\"/qux\">bar</a><br />qux")
             "  ;; foo\n  ;; bar\n  ;; qux\n")))))

(deftest tests-path
  (testing "class"
    (is (= (type (#'fp/tests-path project-foo)) java.io.File)))
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
    (is (= (type (#'fp/src-path project-foo)) java.io.File)))
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
  (testing "stringified number"
    (is (= (#'fp/prob-url "5") "http://4clojure.com/api/problem/5")))
  (testing "positive number"
    (is (= (#'fp/prob-url 1) "http://4clojure.com/api/problem/1"))
    (is (= (#'fp/prob-url 42) "http://4clojure.com/api/problem/42"))
    (is (= (#'fp/prob-url 133) "http://4clojure.com/api/problem/133"))))

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
    (let [content (str "(deftest a-test\n"                            ; default lein2
                       "  (testing \"FIXME, I fail.\"\n"              ; test placeholder
                       "    (is (= 0 1))))")]
      (is (= (with-redefs [slurp (constantly content)] (#'fp/get-tests {:title "yo"}))
             "")))))

(deftest get-src
  (testing "empty file"
    (is (= (with-redefs [slurp (constantly "")] (#'fp/get-src {:title "yo"}))
           "")))
  (is (= (with-redefs [slurp (constantly "xY3")] (#'fp/get-src {:title "yo"}))
         "xY3")))

(deftest write-problem-tests
  (with-redefs [spit (fn [f code & _]
                       (is (= "test/foo/core_test.clj" (. f getPath)))
                       (is (= (str "\n\n"
                                   ";; problem " (:prob-num prob1) "\n"
                                   "(deftest can-foo-bar\n"
                                   "  (is (= (foo-bar-solution 42) 21))\n"
                                   "  (is (= (foo-bar-solution 21) 42)))\n")
                              code)))]
    (#'fp/write-problem-tests project-foo "" prob1)))

(deftest write-problem-src
  (with-redefs [spit (fn [f code & _]
                       (is (= "src/foo/core.clj" (. f getPath)))
                       (is (= (str "\n\n"
                                   ";; problem " (:prob-num prob1) " (" (:difficulty prob1) ")\n"
                                   ";; restrictions: f1, f2\n"
                                   "(def foo-bar-solution\n"
                                   "  (fn [& args] ;; update args as needed\n"
                                   "  ;; write a foo bar\n"
                                   "  nil))\n")
                              code)))]
    (#'fp/write-problem-src project-foo prob1)))

(deftest write-problem-src-no-restrictions
  (with-redefs [spit (fn [f code & _]
                       (is (= "src/foo/core.clj" (. f getPath)))
                       (is (= (str "\n\n"
                                   ";; problem " (:prob-num prob1) " (" (:difficulty prob1) ")\n"
                                   "(def foo-bar-solution\n"
                                   "  (fn [& args] ;; update args as needed\n"
                                   "  ;; write a foo bar\n"
                                   "  nil))\n")
                              code)))]
    (#'fp/write-problem-src project-foo (dissoc prob1 :restricted))))

(deftest write-prob
  ;; TODO factorize duplicate code here, but it seems that nested
  ;; with-redefs-fn don’t work
  (testing "doesn’t write tests if they are already present"
    (with-redefs-fn {#'fp/get-tests           (constantly "")
                     #'fp/get-src             (constantly "")
                     #'fp/has-problem-tests?  (constantly true)
                     #'fp/has-problem-src?    (constantly false)
                     #'fp/write-problem-tests (fn [& _]
                                                (is (= true false)
                                                    "should not write tests"))
                     #'fp/write-problem-src   (constantly nil)
                     #'println                (fn [s]
                                                (is (= s "tests already exist, skipping.")))}
      #(#'fp/write-prob project-foo prob1)))
  (testing "doesn’t write source function if it’s already present"
    (with-redefs-fn {#'fp/get-tests           (constantly "")
                     #'fp/get-src             (constantly "")
                     #'fp/has-problem-tests?  (constantly false)
                     #'fp/has-problem-src?    (constantly true)
                     #'fp/write-problem-src   (fn [& _]
                                                (is (= true false)
                                                    "should not write in src"))
                     #'fp/write-problem-tests (constantly nil)
                     #'println                (fn [s]
                                                (is (= s "source already exists, skipping.")))}
      #(#'fp/write-prob project-foo prob1)))
  (testing "doesn’t write source and tests if they’re already present"
    (with-redefs-fn {#'fp/get-tests           (constantly "")
                     #'fp/get-src             (constantly "")
                     #'fp/has-problem-tests?  (constantly true)
                     #'fp/has-problem-src?    (constantly true)
                     #'fp/write-problem-src   (fn [& _]
                                                (is (= true false)
                                                    "should not write in src"))
                     #'fp/write-problem-tests (fn [& _]
                                                (is (= true false)
                                                    "should not write tests"))
                     #'println                (constantly nil)}
      #(#'fp/write-prob project-foo prob1)))
  (testing "write both source and tests if they’re not already present"
    (let [write-tests-called? (atom false)
          write-src-called?   (atom false)]
      (with-redefs-fn {#'fp/get-tests           (constantly "")
                       #'fp/get-src             (constantly "")
                       #'fp/has-problem-tests?  (constantly false)
                       #'fp/has-problem-src?    (constantly false)
                       #'fp/write-problem-src   (fn [& _] (swap! write-src-called? not))
                       #'fp/write-problem-tests (fn [& _] (swap! write-tests-called? not))
                       #'println                (constantly nil)}
        (fn []
          (#'fp/write-prob project-foo prob1)
          (is (= @write-tests-called? true))
          (is (= @write-src-called? true)))))))

(deftest add-prob
  (testing "cannot get problem"
    (with-redefs-fn {#'fp/fetch-prob-data (constantly nil)
                     #'println            (assert-println "Cannot get problem 33.")}
      (fn []
        (#'fp/add-prob project-foo 33))))
  (testing "got an exception while writing problem"
    (with-redefs-fn {#'fp/fetch-prob-data (constantly prob1)
                     #'fp/write-prob      (fn [& _] (throw (Exception. "foo")))
                     #'println            (assert-println (str "An error occured when"
                                                               " writing the problem."
                                                               " foo"))}
      (fn []
        (#'fp/add-prob project-foo 42))))

  (testing "writing problem"
    (let [written (atom false)]
      (with-redefs-fn {#'fp/fetch-prob-data (constantly prob1)
                       #'fp/write-prob      (fn [& _] (swap! written not))
                       #'println            (assert-println (str "Problem \"Foo Bar\""
                                                                 " added!"))}
        (fn []
          (#'fp/add-prob project-foo 42)
          (is (= @written true)))))))

(deftest open-prob-url
  (testing "one prob"
    (let [opened (atom false)]
      (with-redefs-fn
        {#'browse/browse-url (fn [u]
                               (swap! opened not)
                               (is (= u "http://www.4clojure.com/problem/42")))}
        (fn []
          (#'fp/open-prob-url 42)
          (is (= @opened true))))))

  (testing "multiple probs"
    (let [opened (atom #{})
          probs  [42 37 25]]
      (with-redefs-fn {#'browse/browse-url (fn [u]
                                             (swap! opened #(conj % u)))}
        (fn []
          (apply #'fp/open-prob-url probs)
          (is (= @opened
                 (into #{}
                       (map
                         #(str "http://www.4clojure.com/problem/" %)
                         probs)))))))))

(deftest fore-prob
  (let [proj {:foo :bar}]
    (testing "one prob"
      (let [added (atom false)
            n     "42"]
        (with-redefs-fn {#'fp/add-prob (fn [prj prob-num]
                                         (is (= prj proj))
                                         (is (= prob-num n))
                                         (swap! added not))}
          (fn []
            (fp/fore-prob proj n)
            (is (= @added true))))))

    (testing "multiple probs"
      (let [cnt (atom 0)]
        (with-redefs-fn {#'fp/add-prob (fn [prj _]
                                         (is (= prj proj))
                                         (swap! cnt inc))}
          (fn []
            (fp/fore-prob proj "42" "17" "26" "32")
            (is (= @cnt 4))))))

    (testing "opening one prob"
      (let [opened (atom false)
            n      "32"]
        (with-redefs-fn {#'fp/open-prob-url (fn [& pn]
                                              (is (= pn [n]))
                                              (swap! opened not))}
          (fn []
            (fp/fore-prob proj "open" n)
            (is (= @opened true))))))

    (testing "opening one prob"
      (let [opened (atom [])
            probs  ["34" "12" "17"]]
        (with-redefs-fn {#'fp/open-prob-url (fn [& pn]
                                              (is (= pn probs))
                                              (swap! opened #(concat % probs)))}
          (fn []
            (fp/fore-prob proj "open" "34" "12" "17")
            (is (= @opened probs))))))))
