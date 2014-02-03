# Changelog

## 0.0.3

* Using `tests/project_name/core_tests.clj` for tests instead of
  `tests/project_name/tests/core.clj`
* Improved functions formatting
* Description added in comments of problems

## 0.0.2

* Better error message for HTTP errors when retrieving a problem

## 0.0.1

First version, from [`lein-foreclojure-plugin`][lfp] code.

* Added support for Leiningen 2.x, dropping support for 1.x
* Using Clojure 1.5 instead of 1.2
* Up to date dependencies
* Code formatting slightly improved
* All functions but the main one are private
* New name, to match Leiningen plugins naming convention, `lein-<command>`

[lfp]: https://github.com/broquaint/lein-foreclojure-plugin