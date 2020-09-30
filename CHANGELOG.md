# Changelog

## Unreleased

* Tests are now named following the `<function> + "-test"` convention
* Exceptions are not caught when writing a problem anymore
* Bump dependencies
* Rewrite the code so it’s easier to read

## 0.1.3 (2019/06/03)

* Special characters allowed in problem names, thus fixing the name conflict
  that occurs when two problems share the same name apart from a special char.
* Bump dependencies. This enables 1.10 support (see [#7][issue-7]).

[issue-7]: https://github.com/bfontaine/lein-fore-prob/issues/7

## 0.1.2

* minor memory usage improvement
* problem number added in a comment above its function and its test
* problem difficulty and restricted functions added in the comment above its
  solution placeholder
* `open` subcommand added: `lein fore-prob open 42` will open problem 42 in a
  browser.

## 0.1.1

* HTML code is now stripped from problems’ descriptions
* Multiple problems can be fetched at once, like in `lein fore-prob 1 2 3`

## 0.1.0

* A large part of the code has been rewritten, making it more efficient
* Tests added, full code coverage

## 0.0.4

* Missing argument in an internal function fixed
* Documentation added in the code

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
