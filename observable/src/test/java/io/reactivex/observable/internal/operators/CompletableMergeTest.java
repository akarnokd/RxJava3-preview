/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.observable.internal.operators;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.Function;
import io.reactivex.observable.*;
import io.reactivex.observable.Observable;
import io.reactivex.observable.Observer;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.*;

public class CompletableMergeTest {
    @Test
    public void invalidPrefetch() {
        try {
            Completable.merge(Observable.just(Completable.complete()), -99);
            fail("Should have thrown IllegalArgumentExceptio");
        } catch (IllegalArgumentException ex) {
            assertEquals("maxConcurrency > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void cancelAfterFirst() {
        final TestObserver<Void> to = new TestObserver<Void>();

        Completable.mergeArray(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                s.onSubscribe(Disposables.empty());
                s.onComplete();
                to.cancel();
            }
        }, Completable.complete())
        .subscribe(to);

        to.assertEmpty();
    }

    @Test
    public void cancelAfterFirstDelayError() {
        final TestObserver<Void> to = new TestObserver<Void>();

        Completable.mergeArrayDelayError(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                s.onSubscribe(Disposables.empty());
                s.onComplete();
                to.cancel();
            }
        }, Completable.complete())
        .subscribe(to);

        to.assertEmpty();
    }

    @Test
    public void onErrorAfterComplete() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final CompletableObserver[] co = { null };

            Completable.mergeArrayDelayError(Completable.complete(), new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    s.onSubscribe(Disposables.empty());
                    s.onComplete();
                    co[0] = s;
                }
            })
            .test()
            .assertResult();

            co[0].onError(new TestException());

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void completeAfterMain() {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeArray(Completable.complete(), pp)
        .test();

        pp.onComplete();

        to.assertResult();
    }

    @Test
    public void completeAfterMainDelayError() {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeArrayDelayError(Completable.complete(), pp)
        .test();

        pp.onComplete();

        to.assertResult();
    }

    @Test
    public void errorAfterMainDelayError() {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeArrayDelayError(Completable.complete(), pp)
        .test();

        pp.onError(new TestException());

        to.assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.merge(Observable.just(Completable.complete())));
    }

    @Test
    public void disposePropagates() {

        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.merge(Observable.just(pp)).test();

        assertTrue(pp.hasObservers());

        to.cancel();

        assertFalse(pp.hasObservers());

        to.assertEmpty();
    }

    @Test
    public void innerComplete() {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.merge(Observable.just(pp)).test();

        pp.onComplete();

        to.assertResult();
    }

    @Test
    public void innerError() {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.merge(Observable.just(pp)).test();

        pp.onError(new TestException());

        to.assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError() {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeDelayError(Observable.just(pp)).test();

        pp.onError(new TestException());

        to.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorInnerErrorRace() {
        for (int i = 0; i < 500; i++) {
            List<Throwable> errors = TestCommonHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> pp1 = PublishSubject.create();
                final CompletableSubject pp2 = CompletableSubject.create();

                TestObserver<Void> to = Completable.merge(pp1.map(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer v) throws Exception {
                        return pp2;
                    }
                })).test();

                pp1.onNext(1);

                final Throwable ex1 = new TestException();
                final Throwable ex2 = new TestException();

                Runnable r1 = new Runnable() {
                    @Override
                    public void run() {
                        pp1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        pp2.onError(ex2);
                    }
                };

                TestCommonHelper.race(r1, r2, Schedulers.single());

                Throwable ex = to.errors().get(0);
                if (ex instanceof CompositeException) {
                    to.assertSubscribed().assertNoValues().assertNotComplete();

                    errors = TestCommonHelper.compositeList(ex);
                    TestCommonHelper.assertError(errors, 0, TestException.class);
                    TestCommonHelper.assertError(errors, 1, TestException.class);
                } else {
                    to.assertFailure(TestException.class);

                    if (!errors.isEmpty()) {
                        TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
                    }
                }
            } finally {
                RxJavaCommonPlugins.reset();
            }
        }
    }

    @Test
    public void mainErrorInnerErrorDelayedRace() {
        for (int i = 0; i < 500; i++) {
            final PublishSubject<Integer> pp1 = PublishSubject.create();
            final CompletableSubject pp2 = CompletableSubject.create();

            TestObserver<Void> to = Completable.mergeDelayError(pp1.map(new Function<Integer, Completable>() {
                @Override
                public Completable apply(Integer v) throws Exception {
                    return pp2;
                }
            })).test();

            pp1.onNext(1);

            final Throwable ex1 = new TestException();

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    pp1.onError(ex1);
                }
            };

            final Throwable ex2 = new TestException();
            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    pp2.onError(ex2);
                }
            };

            TestCommonHelper.race(r1, r2, Schedulers.single());

            to.assertFailure(CompositeException.class);

            List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

            TestCommonHelper.assertError(errors, 0, TestException.class);
            TestCommonHelper.assertError(errors, 1, TestException.class);
        }
    }

    @Test
    public void maxConcurrencyOne() {
        final CompletableSubject pp1 = CompletableSubject.create();
        final CompletableSubject pp2 = CompletableSubject.create();

        TestObserver<Void> to = Completable.merge(Observable.just(pp1, pp2), 1)
        .test();

        assertTrue(pp1.hasObservers());
        assertFalse(pp2.hasObservers());

        pp1.onComplete();

        assertTrue(pp2.hasObservers());

        pp2.onComplete();

        to.assertResult();
    }

    @Test
    public void maxConcurrencyOneDelayError() {
        final CompletableSubject pp1 = CompletableSubject.create();
        final CompletableSubject pp2 = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeDelayError(Observable.just(pp1, pp2), 1)
        .test();

        assertTrue(pp1.hasObservers());
        assertFalse(pp2.hasObservers());

        pp1.onComplete();

        assertTrue(pp2.hasObservers());

        pp2.onComplete();

        to.assertResult();
    }

    @Test
    public void maxConcurrencyOneDelayErrorFirst() {
        final CompletableSubject pp1 = CompletableSubject.create();
        final CompletableSubject pp2 = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeDelayError(Observable.just(pp1, pp2), 1)
        .test();

        assertTrue(pp1.hasObservers());
        assertFalse(pp2.hasObservers());

        pp1.onError(new TestException());

        assertTrue(pp2.hasObservers());

        pp2.onComplete();

        to.assertFailure(TestException.class);
    }

    @Test
    public void maxConcurrencyOneDelayMainErrors() {
        final PublishSubject<CompletableSubject> pp0 = PublishSubject.create();
        final CompletableSubject pp1 = CompletableSubject.create();
        final CompletableSubject pp2 = CompletableSubject.create();

        TestObserver<Void> to = Completable.mergeDelayError(
        pp0.map(new Function<Completable, Completable>() {
            @Override
            public Completable apply(Completable v) throws Exception {
                return v;
            }
        }), 1)
        .test();

        pp0.onNext(pp1);

        assertTrue(pp1.hasObservers());
        assertFalse(pp2.hasObservers());

        pp1.onComplete();

        pp0.onNext(pp2);
        pp0.onError(new TestException());

        assertTrue(pp2.hasObservers());

        pp2.onComplete();

        to.assertFailure(TestException.class);
    }

    @Test
    public void mainDoubleOnError() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Completable.mergeDelayError(new Observable<Completable>() {
                @Override
                protected void subscribeActual(Observer<? super Completable> s) {
                    s.onSubscribe(Disposables.empty());
                    s.onNext(Completable.complete());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            })
            .test()
            .assertFailureAndMessage(TestException.class, "First");

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void innerDoubleOnError() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final CompletableObserver[] o = { null };
            Completable.mergeDelayError(Observable.just(new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    s.onSubscribe(Disposables.empty());
                    s.onError(new TestException("First"));
                    o[0] = s;
                }
            }))
            .test()
            .assertFailureAndMessage(TestException.class, "First");

            o[0].onError(new TestException("Second"));

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void innerIsDisposed() {
        final TestObserver<Void> to = new TestObserver<Void>();

        Completable.mergeDelayError(Observable.just(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                s.onSubscribe(Disposables.empty());
                assertFalse(((Disposable)s).isDisposed());

                to.dispose();

                assertTrue(((Disposable)s).isDisposed());
            }
        }))
        .subscribe(to);
    }

    @Test
    public void mergeArrayInnerErrorRace() {
        for (int i = 0; i < 500; i++) {
            List<Throwable> errors = TestCommonHelper.trackPluginErrors();
            try {
                final CompletableSubject pp1 = CompletableSubject.create();
                final CompletableSubject pp2 = CompletableSubject.create();

                TestObserver<Void> to = Completable.mergeArray(pp1, pp2).test();

                final Throwable ex1 = new TestException();
                final Throwable ex2 = new TestException();

                Runnable r1 = new Runnable() {
                    @Override
                    public void run() {
                        pp1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        pp2.onError(ex2);
                    }
                };

                TestCommonHelper.race(r1, r2, Schedulers.single());

                to.assertFailure(TestException.class);

                if (!errors.isEmpty()) {
                    TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaCommonPlugins.reset();
            }
        }
    }

    @Test
    public void delayErrorIterableCancel() {
        Completable.mergeDelayError(Arrays.asList(Completable.complete()))
        .test(true)
        .assertEmpty();
    }

    @Test
    public void delayErrorIterableCancelAfterHasNext() {
        final TestObserver<Void> to = new TestObserver<Void>();

        Completable.mergeDelayError(new Iterable<Completable>() {
            @Override
            public Iterator<Completable> iterator() {
                return new Iterator<Completable>() {
                    @Override
                    public boolean hasNext() {
                        to.cancel();
                        return true;
                    }

                    @Override
                    public Completable next() {
                        return Completable.complete();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        })
        .subscribe(to);

        to.assertEmpty();
    }

    @Test
    public void delayErrorIterableCancelAfterNext() {
        final TestObserver<Void> to = new TestObserver<Void>();

        Completable.mergeDelayError(new Iterable<Completable>() {
            @Override
            public Iterator<Completable> iterator() {
                return new Iterator<Completable>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Completable next() {
                        to.cancel();
                        return Completable.complete();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        })
        .subscribe(to);

        to.assertEmpty();
    }
}
