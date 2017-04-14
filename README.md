# RxJava3-preview
Preview for version 3 of RxJava, the modern ReactiveX style library for composing (a)synchronous sequences of data and events.

This is an unofficial preparation place for RxJava 3 where the major change is the repackaging of certain components into separate libraries:

- `rxjava3-commons`
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
