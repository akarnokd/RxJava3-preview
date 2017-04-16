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

package io.reactivex.interop.internal.operators;

import static io.reactivex.interop.RxJava3Interop.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.reactivestreams.Subscriber;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.Function;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.internal.subscriptions.BooleanSubscription;
import io.reactivex.flowable.processors.PublishProcessor;
import io.reactivex.interop.TestHelper;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;

public class CompletableMergeTest {
    @Test
    public void invalidPrefetch() {
        try {
            mergeCompletable(Flowable.just(Completable.complete()), -99);
            fail("Should have thrown IllegalArgumentExceptio");
        } catch (IllegalArgumentException ex) {
            assertEquals("maxConcurrency > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(mergeCompletable(Flowable.just(Completable.complete())));
    }

    @Test
    public void disposePropagates() {

        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletable(Flowable.just(ignoreElements(pp))).test();

        assertTrue(pp.hasSubscribers());

        to.cancel();

        assertFalse(pp.hasSubscribers());

        to.assertEmpty();
    }

    @Test
    public void innerComplete() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletable(Flowable.just(ignoreElements(pp))).test();

        pp.onComplete();

        to.assertResult();
    }

    @Test
    public void innerError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletable(Flowable.just(ignoreElements(pp))).test();

        pp.onError(new TestException());

        to.assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletableDelayError(Flowable.just(ignoreElements(pp))).test();

        pp.onError(new TestException());

        to.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorInnerErrorRace() {
        for (int i = 0; i < 500; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();

                TestObserver<Void> to = mergeCompletable(pp1.map(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer v) throws Exception {
                        return ignoreElements(pp2);
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

                TestHelper.race(r1, r2, Schedulers.single());

                Throwable ex = to.errors().get(0);
                if (ex instanceof CompositeException) {
                    to.assertSubscribed().assertNoValues().assertNotComplete();

                    errors = TestHelper.compositeList(ex);
                    TestHelper.assertError(errors, 0, TestException.class);
                    TestHelper.assertError(errors, 1, TestException.class);
                } else {
                    to.assertFailure(TestException.class);

                    if (!errors.isEmpty()) {
                        TestHelper.assertUndeliverable(errors, 0, TestException.class);
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
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();

            TestObserver<Void> to = mergeCompletableDelayError(pp1.map(new Function<Integer, Completable>() {
                @Override
                public Completable apply(Integer v) throws Exception {
                    return ignoreElements(pp2);
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

            TestHelper.race(r1, r2, Schedulers.single());

            to.assertFailure(CompositeException.class);

            List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));

            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, TestException.class);
        }
    }

    @Test
    public void maxConcurrencyOne() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletable(Flowable.just(ignoreElements(pp1), ignoreElements(pp2)), 1)
        .test();

        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());

        pp1.onComplete();

        assertTrue(pp2.hasSubscribers());

        pp2.onComplete();

        to.assertResult();
    }

    @Test
    public void maxConcurrencyOneDelayError() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletableDelayError(Flowable.just(ignoreElements(pp1), ignoreElements(pp2)), 1)
        .test();

        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());

        pp1.onComplete();

        assertTrue(pp2.hasSubscribers());

        pp2.onComplete();

        to.assertResult();
    }

    @Test
    public void maxConcurrencyOneDelayErrorFirst() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletableDelayError(Flowable.just(ignoreElements(pp1), ignoreElements(pp2)), 1)
        .test();

        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());

        pp1.onError(new TestException());

        assertTrue(pp2.hasSubscribers());

        pp2.onComplete();

        to.assertFailure(TestException.class);
    }

    @Test
    public void maxConcurrencyOneDelayMainErrors() {
        final PublishProcessor<PublishProcessor<Integer>> pp0 = PublishProcessor.create();
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();

        TestObserver<Void> to = mergeCompletableDelayError(
        pp0.map(new Function<PublishProcessor<Integer>, Completable>() {
            @Override
            public Completable apply(PublishProcessor<Integer> v) throws Exception {
                return ignoreElements(v);
            }
        }), 1)
        .test();

        pp0.onNext(pp1);

        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());

        pp1.onComplete();

        pp0.onNext(pp2);
        pp0.onError(new TestException());

        assertTrue(pp2.hasSubscribers());

        pp2.onComplete();

        to.assertFailure(TestException.class);
    }

    @Test
    public void mainDoubleOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            mergeCompletableDelayError(new Flowable<Completable>() {
                @Override
                protected void subscribeActual(Subscriber<? super Completable> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(Completable.complete());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            })
            .test()
            .assertFailureAndMessage(TestException.class, "First");

            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void innerDoubleOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CompletableObserver[] o = { null };
            mergeCompletableDelayError(Flowable.just(new Completable() {
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

            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void innerIsDisposed() {
        final TestObserver<Void> to = new TestObserver<Void>();

        mergeCompletableDelayError(Flowable.just(new Completable() {
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
}
