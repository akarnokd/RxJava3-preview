# RxJava3-preview

**Discontinued.** Version 3 development will be branched off of 2.x sometime after **2.2** has been finalized in the main [RxJava repository](https://github.com/ReactiveX/RxJava).

-------------------------

<a href='https://travis-ci.org/akarnokd/RxJava3-preview/builds'><img src='https://travis-ci.org/akarnokd/RxJava3-preview.svg?branch=master'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJava3-preview/coverage.svg?branch=master)](http://codecov.io/github/akarnokd/RxJava3-preview?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-common/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-common) 

Preview for version 3 of RxJava, the modern ReactiveX style library for composing (a)synchronous sequences of data and events.

## Dependency

```groovy
// shared components
compile "com.github.akarnokd:rxjava3-common:0.2.0"

// Flowable only
compile "com.github.akarnokd:rxjava3-flowable:0.2.0"

// Observable, Single, Maybe, Completable
compile "com.github.akarnokd:rxjava3-observable:0.2.0"

// Interoperation between Flowable and the rest
compile "com.github.akarnokd:rxjava3-interop:0.2.0"
```

## Structure

This is an unofficial preparation place for RxJava 3 where the major change is the repackaging of certain components into separate libraries:

- `rxjava3-common`
  - Disposable
  - Scheduler
  - concurrent queues 
  - utility classes
  - dependencies: **none**
- `rxjava3-flowable`
  - `Flowable` class
  - operators return `Flowable`
  - backpressure related utilities
  - dependencies: **rxjava3-commons**, **reactive-streams-extensions** (-> **reactive-streams**)
- `rxjava3-observable`
  - `Observable`, `Single`, `Maybe`, `Completable` classes
  - operators return the most appropriate reactive type
  - `FusedQueueDisposable` - operator fusion for `Observable` operators
  - utility classes
  - dependencies: **rxjava3-commons**
- `rxjava3-interop`
  - transformers and converters between the backpressured `Flowable` and the non-backpressured `Observable` types
  - dependencies: **rxjava3-flowable**, **rxjava3-observable**, (-> **rxjava3-commons**, **reactive-streams-extensions**, **reactive-streams**)
  

## TODOs

### Work out how the snapshot release and final release works in RxJava 1/2's Nebula plugin

Currently, this preview releases manually and not in response to merging or hitting the GitHub release button. I don't really know which and how the unsupported Nebula plugin works and if it supports Gradle subprojects. Also due to the encrypted credentials, such auto-release must happen from within ReactiveX/RxJava.
