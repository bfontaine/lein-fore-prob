# foreclojure-plugin

A leiningen plugin which given a problem from 4clojure will populate the
current project's tests with the problem's tests.

## Usage

    lein plugin install fore-prob 0.0.1
    lein new probs-from-4clj
    cd probs-from-4clj
    lein fore-prob PROBLEMID

## License

Copyright (C) 2011 Dan Brook

Distributed under the Eclipse Public License, the same as Clojure.
