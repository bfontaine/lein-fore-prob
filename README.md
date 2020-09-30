# lein-fore-prob

[![Build Status](https://travis-ci.org/bfontaine/lein-fore-prob.png?branch=master)](https://travis-ci.org/bfontaine/lein-fore-prob)
[![Coverage Status](https://coveralls.io/repos/bfontaine/lein-fore-prob/badge.png)](https://coveralls.io/r/bfontaine/lein-fore-prob)

A leiningen plugin which given a problem from 4clojure will populate the
current project’s tests with the problem’s tests. This is based on
[`lein-foreclojure-plugin`][lfp] code updated for Leiningen 2.

[lfp]: https://github.com/broquaint/lein-foreclojure-plugin

## Usage

Add the plugin in your `~/.lein/profiles.clj`:

```clj
{:user {:plugins [ ; ... other plugins ...
                  [lein-fore-prob "0.1.3"]]}}
```

Create a project to store 4clojure problems:

```
lein new probs-from-4clj
cd probs-from-4clj
```

Then use the plugin:

```
lein fore-prob [open] <problem id> [<problem id> ...]
```

You can use multiple problem ids at the same time. Use the `open` subcommand to
open these problems in a browser instead of adding it to the current project.

## What’s added

Given a new project like this:

	.
    ├── ...
    ├── project.clj
    ├── src
    │   └── probs_from_4clj
    │       └── core.clj
    └── test
        └── probs_from_4clj
            └── core_test.clj

A stub function will be added to `src/probs_from_4clj/core.clj` e.g for
[problem 98][98] an `equivalence-classes-solution` function is defined. Then in
`test/probs_from_4clj/core_test.clj` the tests for that problem will be added
e.g. again for [problem 98][98] the test would be `can-equivalence-classes`. At
that point you can flesh out the function until the test passes then copy
it over to [4clojure.org][4clj] when you’re happy with it.

[98]: http://www.4clojure.com/problem/98 "98. Equivalence Classes"
[4clj]: http://www.4clojure.com/

### Example

Here is what `lein fore-prob 43` would add:

```clj
;; in src/your-project/core.clj:

;; problem 43 (Medium)
(defn reverse-interleave-solution
  [& args] ;; update args as needed
  ;; Write a function which reverses the interleave process into x number of subsequences.
  nil)


;; in test/your-project/core_test.clj:

;; problem 43
(deftest can-reverse-interleave
  (is (= (reverse-interleave-solution [1 2 3 4 5 6] 2) '((1 3 5) (2 4 6))))
  (is (= (reverse-interleave-solution (range 9) 3) '((0 3 6) (1 4 7) (2 5 8))))
  (is (= (reverse-interleave-solution (range 10) 5) '((0 5) (1 6) (2 7) (3 8) (4 9)))))
```

## License

Copyright © 2014-2020 Baptiste Fontaine

**Original code:**

Copyright © 2011 Dan Brook

Distributed under the Eclipse Public License, the same as Clojure.
