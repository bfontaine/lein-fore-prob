(ns foreclojure-plugin.test.core
  (:use [foreclojure-plugin.core])
  (:use [clojure.test]))

(deftest replace-me ;; FIXME: write
  (is false "No tests have been written."))

(deftest can-universal-computation-engine
(= 2 ((universal-computation-engine-solution '(/ a b))
'{b 8 a 16}))
(= 8 ((universal-computation-engine-solution '(+ a b 2))
'{a 2 b 4}))
(= [6 0 -4]
(map (universal-computation-engine-solution '(* (+ 2 a)
(- 10 b)))
'[{a 1 b 8}
{b 5 a -2}
{a 2 b 11}]))
(= 1 ((universal-computation-engine-solution '(/ (+ x 2)
(* 3 (+ y 1))))
'{x 4 y 1}))
)

(deftest can-universal-computation-engine
(= 2 ((universal-computation-engine-solution '(/ a b))
'{b 8 a 16}))
(= 8 ((universal-computation-engine-solution '(+ a b 2))
'{a 2 b 4}))
(= [6 0 -4]
(map (universal-computation-engine-solution '(* (+ 2 a)
(- 10 b)))
'[{a 1 b 8}
{b 5 a -2}
{a 2 b 11}]))
(= 1 ((universal-computation-engine-solution '(/ (+ x 2)
(* 3 (+ y 1))))
'{x 4 y 1}))
)

(deftest can-universal-computation-engine
(= 2 ((universal-computation-engine-solution '(/ a b))
'{b 8 a 16}))
(= 8 ((universal-computation-engine-solution '(+ a b 2))
'{a 2 b 4}))
(= [6 0 -4]
(map (universal-computation-engine-solution '(* (+ 2 a)
(- 10 b)))
'[{a 1 b 8}
{b 5 a -2}
{a 2 b 11}]))
(= 1 ((universal-computation-engine-solution '(/ (+ x 2)
(* 3 (+ y 1))))
'{x 4 y 1}))
)

(deftest can-universal-computation-engine
(= 2 ((universal-computation-engine-solution '(/ a b))
'{b 8 a 16}))
(= 8 ((universal-computation-engine-solution '(+ a b 2))
'{a 2 b 4}))
(= [6 0 -4]
(map (universal-computation-engine-solution '(* (+ 2 a)
(- 10 b)))
'[{a 1 b 8}
{b 5 a -2}
{a 2 b 11}]))
(= 1 ((universal-computation-engine-solution '(/ (+ x 2)
(* 3 (+ y 1))))
'{x 4 y 1}))
)

(deftest can-universal-computation-engine
(= 2 ((universal-computation-engine-solution '(/ a b))
'{b 8 a 16}))
(= 8 ((universal-computation-engine-solution '(+ a b 2))
'{a 2 b 4}))
(= [6 0 -4]
(map (universal-computation-engine-solution '(* (+ 2 a)
(- 10 b)))
'[{a 1 b 8}
{b 5 a -2}
{a 2 b 11}]))
(= 1 ((universal-computation-engine-solution '(/ (+ x 2)
(* 3 (+ y 1))))
'{x 4 y 1}))
)
