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
import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.Function;
import io.reactivex.common.internal.functions.Functions;
import io.reactivex.common.internal.utils.CrashingMappedIterable;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.PublishSubject;

public class SingleZipIterableTest {

    final Function<Object[], Object> addString = new Function<Object[], Object>() {
        @Override
        public Object apply(Object[] a) throws Exception {
            return Arrays.toString(a);
        }
    };

    @SuppressWarnings("unchecked")
    @Test
    public void firstError() {
        Single.zip(Arrays.asList(Single.error(new TestException()), Single.just(1)), addString)
        .test()
        .assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void secondError() {
        Single.zip(Arrays.asList(Single.just(1), Single.<Integer>error(new TestException())), addString)
        .test()
        .assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void dispose() {
        PublishSubject<Integer> pp = PublishSubject.create();

        TestObserver<Object> to = Single.zip(Arrays.asList(pp.single(0), pp.single(0)), addString)
        .test();

        assertTrue(pp.hasObservers());

        to.cancel();

        assertFalse(pp.hasObservers());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zipperThrows() {
        Single.zip(Arrays.asList(Single.just(1), Single.just(2)), new Function<Object[], Object>() {
            @Override
            public Object apply(Object[] b) throws Exception {
                throw new TestException();
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zipperReturnsNull() {
        Single.zip(Arrays.asList(Single.just(1), Single.just(2)), new Function<Object[], Object>() {
            @Override
            public Object apply(Object[] a) throws Exception {
                return null;
            }
        })
        .test()
        .assertFailure(NullPointerException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void middleError() {
        PublishSubject<Integer> pp0 = PublishSubject.create();
        PublishSubject<Integer> pp1 = PublishSubject.create();

        TestObserver<Object> to = Single.zip(
                Arrays.asList(pp0.single(0), pp1.single(0), pp0.single(0)), addString)
        .test();

        pp1.onError(new TestException());

        assertFalse(pp0.hasObservers());

        to.assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void innerErrorRace() {
        for (int i = 0; i < 500; i++) {
            List<Throwable> errors = TestCommonHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> pp0 = PublishSubject.create();
                final PublishSubject<Integer> pp1 = PublishSubject.create();

                final TestObserver<Object> to = Single.zip(
                        Arrays.asList(pp0.single(0), pp1.single(0)), addString)
                .test();

                final TestException ex = new TestException();

                Runnable r1 = new Runnable() {
                    @Override
                    public void run() {
                        pp0.onError(ex);
                    }
                };

                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        pp1.onError(ex);
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
    public void iteratorThrows() {
        Single.zip(new CrashingMappedIterable<Single<Integer>>(1, 100, 100, new Function<Integer, Single<Integer>>() {
            @Override
            public Single<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }), addString)
        .test()
        .assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextThrows() {
        Single.zip(new CrashingMappedIterable<Single<Integer>>(100, 20, 100, new Function<Integer, Single<Integer>>() {
            @Override
            public Single<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }), addString)
        .test()
        .assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextThrows() {
        Single.zip(new CrashingMappedIterable<Single<Integer>>(100, 100, 5, new Function<Integer, Single<Integer>>() {
            @Override
            public Single<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }), addString)
        .test()
        .assertFailureAndMessage(TestException.class, "next()");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void zipIterableOneIsNull() {
        Single.zip(Arrays.asList(null, Single.just(1)), new Function<Object[], Object>() {
            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        })
        .blockingGet();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = NullPointerException.class)
    public void zipIterableTwoIsNull() {
        Single.zip(Arrays.asList(Single.just(1), null), new Function<Object[], Object>() {
            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        })
        .blockingGet();
    }

    @Test
    public void emptyIterable() {
        Single.zip(Collections.<SingleSource<Integer>>emptyList(), new Function<Object[], Object[]>() {
            @Override
            public Object[] apply(Object[] a) throws Exception {
                return a;
            }
        })
        .test()
        .assertFailure(NoSuchElementException.class);
    }

    @Test
    public void oneIterable() {
        Single.zip(Collections.singleton(Single.just(1)), new Function<Object[], Object>() {
            @Override
            public Object apply(Object[] a) throws Exception {
                return (Integer)a[0] + 1;
            }
        })
        .test()
        .assertResult(2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void singleSourceZipperReturnsNull() {
        Single.zip(Arrays.asList(Single.just(1)), Functions.justFunction(null))
        .test()
        .assertFailureAndMessage(NullPointerException.class, "The zipper returned a null value");
    }
}
