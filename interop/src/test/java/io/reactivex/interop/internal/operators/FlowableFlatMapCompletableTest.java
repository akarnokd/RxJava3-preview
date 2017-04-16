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
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.reactivestreams.Subscription;

import hu.akarnokd.reactivestreams.extensions.*;
import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.Function;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.*;
import io.reactivex.flowable.*;
import io.reactivex.flowable.processors.PublishProcessor;
import io.reactivex.flowable.subscribers.*;
import io.reactivex.interop.TestHelper;
import io.reactivex.interop.RxJava3Interop;

public class FlowableFlatMapCompletableTest {

    @Test
    public void normalFlowable() {
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        })
        .test()
        .assertResult();
    }

    @Test
    public void mapperThrowsFlowable() {
        PublishProcessor<Integer> ps = PublishProcessor.create();

        TestSubscriber<Integer> to =
        RxJava3Interop.<Integer>toFlowable(
        flatMapCompletable(ps, new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }))
        .test();

        assertTrue(ps.hasSubscribers());

        ps.onNext(1);

        to.assertFailure(TestException.class);

        assertFalse(ps.hasSubscribers());
    }

    @Test
    public void mapperReturnsNullFlowable() {
        PublishProcessor<Integer> ps = PublishProcessor.create();

        TestSubscriber<Integer> to =
            RxJava3Interop.<Integer>toFlowable(
                flatMapCompletable(ps, new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }))
        .test();

        assertTrue(ps.hasSubscribers());

        ps.onNext(1);

        to.assertFailure(NullPointerException.class);

        assertFalse(ps.hasSubscribers());
    }

    @Test
    public void normalDelayErrorFlowable() {
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true, Integer.MAX_VALUE)
        .test()
        .assertResult();
    }

    @Test
    public void normalAsyncFlowable() {
        flatMapCompletable(Flowable.range(1, 1000), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return ignoreElements(Flowable.range(1, 100).subscribeOn(Schedulers.computation()));
            }
        })
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult();
    }

    @Test
    public void normalAsyncFlowableMaxConcurrency() {
        flatMapCompletable(Flowable.range(1, 1000), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return ignoreElements(Flowable.range(1, 100).subscribeOn(Schedulers.computation()));
            }
        }, false, 3)
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult();
    }

    @Test
    public void normalDelayErrorAllFlowable() {
        TestSubscriber<Integer> to =
        RxJava3Interop.<Integer>toFlowable(
        flatMapCompletable(Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())),
        new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE))
        .test()
        .assertFailure(CompositeException.class);

        List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

        for (int i = 0; i < 11; i++) {
            TestCommonHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAllFlowable() {
        TestSubscriber<Integer> to =
        RxJava3Interop.<Integer>toFlowable(
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE))
        .test()
        .assertFailure(CompositeException.class);

        List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

        for (int i = 0; i < 10; i++) {
            TestCommonHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuterFlowable() {
        flatMapCompletable(Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())),
        new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false, Integer.MAX_VALUE)
        .test()
        .assertFailure(TestException.class);
    }


    @Test
    public void fusedFlowable() {
        TestSubscriber<Integer> to = SubscriberFusion.newTest(FusedQueueSubscription.ANY);

        RxJava3Interop.<Integer>toFlowable(
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }))
        .subscribe(to);

        to
        .assertOf(SubscriberFusion.<Integer>assertFuseable())
        .assertOf(SubscriberFusion.<Integer>assertFusionMode(FusedQueueSubscription.ASYNC))
        .assertResult();
    }

    @Test
    public void normal() {
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        })
        .test()
        .assertResult();
    }

    @Test
    public void mapperThrows() {
        PublishProcessor<Integer> ps = PublishProcessor.create();

        TestObserver<Void> to =
        flatMapCompletable(ps, new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        })
        .test();

        assertTrue(ps.hasSubscribers());

        ps.onNext(1);

        to.assertFailure(TestException.class);

        assertFalse(ps.hasSubscribers());
    }

    @Test
    public void mapperReturnsNull() {
        PublishProcessor<Integer> ps = PublishProcessor.create();

        TestObserver<Void> to =
        flatMapCompletable(ps, new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        })
        .test();

        assertTrue(ps.hasSubscribers());

        ps.onNext(1);

        to.assertFailure(NullPointerException.class);

        assertFalse(ps.hasSubscribers());
    }

    @Test
    public void normalDelayError() {
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true, Integer.MAX_VALUE)
        .test()
        .assertResult();
    }

    @Test
    public void normalAsync() {
        flatMapCompletable(Flowable.range(1, 1000), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return ignoreElements(Flowable.range(1, 100).subscribeOn(Schedulers.computation()));
            }
        })
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult();
    }

    @Test
    public void normalDelayErrorAll() {
        TestObserver<Void> to =
        flatMapCompletable(Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())),
        new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE)
        .test()
        .assertFailure(CompositeException.class);

        List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

        for (int i = 0; i < 11; i++) {
            TestCommonHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAll() {
        TestObserver<Void> to =
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE)
        .test()
        .assertFailure(CompositeException.class);

        List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

        for (int i = 0; i < 10; i++) {
            TestCommonHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuter() {
        flatMapCompletable(Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())),
        new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false, Integer.MAX_VALUE)
        .test()
        .assertFailure(TestException.class);
    }


    @Test
    public void fused() {
        TestSubscriber<Integer> ts = SubscriberFusion.newTest(FusedQueueSubscription.ANY);

        RxJava3Interop.<Integer>toFlowable(
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        })
        )
        .subscribe(ts);

        ts
        .assertOf(SubscriberFusion.<Integer>assertFuseable())
        .assertOf(SubscriberFusion.<Integer>assertFusionMode(FusedQueueSubscription.ASYNC))
        .assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void normalAsyncMaxConcurrency() {
        flatMapCompletable(Flowable.range(1, 1000), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return ignoreElements(Flowable.range(1, 100).subscribeOn(Schedulers.computation()));
            }
        }, false, 3)
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult();
    }

    @Test
    public void disposedFlowable() {
        TestHelper.checkDisposed(
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {
            @Override
            public Object apply(Flowable<Integer> o) throws Exception {
                return flatMapCompletable(o, new Function<Integer, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                });
            }
        }, false, 1, null);
    }

    @Test
    public void fusedInternalsFlowable() {
        RxJava3Interop.toFlowable(
        flatMapCompletable(Flowable.range(1, 10), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        })
        )
        .subscribe(new RelaxedSubscriber<Object>() {
            @Override
            public void onSubscribe(Subscription d) {
                FusedQueueSubscription<?> qd = (FusedQueueSubscription<?>)d;
                try {
                    assertNull(qd.poll());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertTrue(qd.isEmpty());
                qd.clear();
            }

            @Override
            public void onNext(Object t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void innerObserverFlowable() {
        RxJava3Interop.toFlowable(
        flatMapCompletable(Flowable.range(1, 3), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {
                    @Override
                    protected void subscribeActual(CompletableObserver s) {
                        s.onSubscribe(Disposables.empty());

                        assertFalse(((Disposable)s).isDisposed());

                        ((Disposable)s).dispose();

                        assertTrue(((Disposable)s).isDisposed());
                    }
                };
            }
        })
        )
        .test();
    }

    @Test
    public void badSourceFlowable() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {
            @Override
            public Object apply(Flowable<Integer> o) throws Exception {
                return flatMapCompletable(o, new Function<Integer, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                });
            }
        }, false, 1, null);
    }

    @Test
    public void innerObserver() {
        flatMapCompletable(Flowable.range(1, 3), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {
                    @Override
                    protected void subscribeActual(CompletableObserver s) {
                        s.onSubscribe(Disposables.empty());

                        assertFalse(((Disposable)s).isDisposed());

                        ((Disposable)s).dispose();

                        assertTrue(((Disposable)s).isDisposed());
                    }
                };
            }
        })
        .test();
    }

    @Test
    public void delayErrorMaxConcurrency() {
        flatMapCompletable(Flowable.range(1, 3), new Function<Integer, CompletableSource>() {
            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 2) {
                    return Completable.error(new TestException());
                }
                return Completable.complete();
            }
        }, true, 1)
        .test()
        .assertFailure(TestException.class);
    }
}
