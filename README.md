# foreclojure-plugin

A leiningen plugin which given a problem from 4clojure will populate the
current project's tests with the problem's tests.

## Usage

    lein plugin install foreclojure-plugin 0.0.1
    lein new probs-from-4clj
    cd probs-from-4clj
    lein fore-prob PROBLEMID

## What's added

Given a new project like this:

    $ tree -A
	.
    ├── classes
    ├── project.clj
    ├── README
    ├── src
    │   └── probs_from_4clj
    │       └── core.clj
    └── test
        └── probs_from_4clj
            └── test
                └── core.clj

A stub function will be added to `src/probs_from_4clj/core.clj` e.g for
[problem 98][] a `equivalence-classes-solution` function is defined. Then in
`test/probs_from_4clj/test/core.clj` the tests for that problem will be added
e.g again for [problem 98][] the test would be `can-equivalence-classes`. At
that point you can flesh out the function until the test passes then copy
it over to http://4clojure.org when you're happy with it.

[problem 98]: http://www.4clojure.com/problem/98 "98. Equivalence Classes"

## License

Copyright (C) 2011 Dan Brook

Distributed under the Eclipse Public License, the same as Clojure.
