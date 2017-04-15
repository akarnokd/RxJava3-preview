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

package io.reactivex.observable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.*;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.utils.ExceptionHelper;
import io.reactivex.observable.extensions.*;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.*;

/**
 * Common methods for helping with tests from 1.x mostly.
 */
public enum TestHelper {
    ;

    /**
     * Mocks an Observer with the proper receiver type.
     * @param <T> the value type
     * @return the mocked observer
     */
    @SuppressWarnings("unchecked")
    public static <T> Observer<T> mockObserver() {
        return mock(Observer.class);
    }

    /**
     * Mocks an MaybeObserver with the proper receiver type.
     * @param <T> the value type
     * @return the mocked observer
     */
    @SuppressWarnings("unchecked")
    public static <T> MaybeObserver<T> mockMaybeObserver() {
        return mock(MaybeObserver.class);
    }

    /**
     * Mocks an SingleObserver with the proper receiver type.
     * @param <T> the value type
     * @return the mocked observer
     */
    @SuppressWarnings("unchecked")
    public static <T> SingleObserver<T> mockSingleObserver() {
        return mock(SingleObserver.class);
    }

    /**
     * Mocks an CompletableObserver.
     * @return the mocked observer
     */
    public static CompletableObserver mockCompletableObserver() {
        return mock(CompletableObserver.class);
    }

    public static void assertError(TestObserver<?> ts, int index, Class<? extends Throwable> clazz) {
        Throwable ex = ts.errors().get(0);
        try {
            if (ex instanceof CompositeException) {
                CompositeException ce = (CompositeException) ex;
                List<Throwable> cel = ce.getExceptions();
                assertTrue(cel.get(index).toString(), clazz.isInstance(cel.get(index)));
            } else {
                fail(ex.toString() + ": not a CompositeException");
            }
        } catch (AssertionError e) {
            ex.printStackTrace();
            throw e;
        }
    }


    public static void assertError(TestObserver<?> ts, int index, Class<? extends Throwable> clazz, String message) {
        Throwable ex = ts.errors().get(0);
        if (ex instanceof CompositeException) {
            CompositeException ce = (CompositeException) ex;
            List<Throwable> cel = ce.getExceptions();
            assertTrue(cel.get(index).toString(), clazz.isInstance(cel.get(index)));
            assertEquals(message, cel.get(index).getMessage());
        } else {
            fail(ex.toString() + ": not a CompositeException");
        }
    }

    /**
     * Assert that the offer methods throw UnsupportedOperationExcetpion.
     * @param q the queue implementation
     */
    public static void assertNoOffer(SimpleQueue<?> q) {
        try {
            q.offer(null);
            fail("Should have thrown!");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
        try {
            q.offer(null, null);
            fail("Should have thrown!");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    /**
     * Returns an Consumer that asserts the TestObserver has exaclty one value + completed
     * normally and that single value is not the value specified.
     * @param <T> the value type
     * @param value the value not expected
     * @return the consumer
     */
    public static <T> Consumer<TestObserver<T>> observerSingleNot(final T value) {
        return new Consumer<TestObserver<T>>() {
            @Override
            public void accept(TestObserver<T> ts) throws Exception {
                ts
                .assertSubscribed()
                .assertValueCount(1)
                .assertNoErrors()
                .assertComplete();

                T v = ts.values().get(0);
                assertNotEquals(value, v);
            }
        };
    }

    /**
     * Calls onSubscribe twice and checks if it doesn't affect the first Disposable while
     * reporting it to plugin error handler.
     * @param subscriber the target
     */
    public static void doubleOnSubscribe(Observer<?> subscriber) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Disposable d1 = Disposables.empty();

            subscriber.onSubscribe(d1);

            Disposable d2 = Disposables.empty();

            subscriber.onSubscribe(d2);

            assertFalse(d1.isDisposed());

            assertTrue(d2.isDisposed());

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Calls onSubscribe twice and checks if it doesn't affect the first Disposable while
     * reporting it to plugin error handler.
     * @param subscriber the target
     */
    public static void doubleOnSubscribe(SingleObserver<?> subscriber) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Disposable d1 = Disposables.empty();

            subscriber.onSubscribe(d1);

            Disposable d2 = Disposables.empty();

            subscriber.onSubscribe(d2);

            assertFalse(d1.isDisposed());

            assertTrue(d2.isDisposed());

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Calls onSubscribe twice and checks if it doesn't affect the first Disposable while
     * reporting it to plugin error handler.
     * @param subscriber the target
     */
    public static void doubleOnSubscribe(CompletableObserver subscriber) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Disposable d1 = Disposables.empty();

            subscriber.onSubscribe(d1);

            Disposable d2 = Disposables.empty();

            subscriber.onSubscribe(d2);

            assertFalse(d1.isDisposed());

            assertTrue(d2.isDisposed());

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Calls onSubscribe twice and checks if it doesn't affect the first Disposable while
     * reporting it to plugin error handler.
     * @param subscriber the target
     */
    public static void doubleOnSubscribe(MaybeObserver<?> subscriber) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Disposable d1 = Disposables.empty();

            subscriber.onSubscribe(d1);

            Disposable d2 = Disposables.empty();

            subscriber.onSubscribe(d2);

            assertFalse(d1.isDisposed());

            assertTrue(d2.isDisposed());

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Checks if the upstream's Disposable sent through the onSubscribe reports
     * isDisposed properly before and after calling dispose.
     * @param source the source to test
     */
    public static void checkDisposed(Maybe<?> source) {
        final Boolean[] b = { null, null };
        final CountDownLatch cdl = new CountDownLatch(1);
        source.subscribe(new MaybeObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
                try {
                    b[0] = d.isDisposed();

                    d.dispose();

                    b[1] = d.isDisposed();

                    d.dispose();
                } finally {
                    cdl.countDown();
                }
            }

            @Override
            public void onSuccess(Object value) {
                // ignored
            }

            @Override
            public void onError(Throwable e) {
                // ignored
            }

            @Override
            public void onComplete() {
                // ignored
            }
        });

        try {
            assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertEquals("Reports disposed upfront?", false, b[0]);
        assertEquals("Didn't report disposed after?", true, b[1]);
    }

    /**
     * Checks if the upstream's Disposable sent through the onSubscribe reports
     * isDisposed properly before and after calling dispose.
     * @param source the source to test
     */
    public static void checkDisposed(Observable<?> source) {
        final Boolean[] b = { null, null };
        final CountDownLatch cdl = new CountDownLatch(1);
        source.subscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
                try {
                    b[0] = d.isDisposed();

                    d.dispose();

                    b[1] = d.isDisposed();

                    d.dispose();
                } finally {
                    cdl.countDown();
                }
            }

            @Override
            public void onNext(Object value) {
                // ignored
            }

            @Override
            public void onError(Throwable e) {
                // ignored
            }

            @Override
            public void onComplete() {
                // ignored
            }
        });

        try {
            assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertEquals("Reports disposed upfront?", false, b[0]);
        assertEquals("Didn't report disposed after?", true, b[1]);
    }

    /**
     * Checks if the upstream's Disposable sent through the onSubscribe reports
     * isDisposed properly before and after calling dispose.
     * @param source the source to test
     */
    public static void checkDisposed(Single<?> source) {
        final Boolean[] b = { null, null };
        final CountDownLatch cdl = new CountDownLatch(1);
        source.subscribe(new SingleObserver<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
                try {
                    b[0] = d.isDisposed();

                    d.dispose();

                    b[1] = d.isDisposed();

                    d.dispose();
                } finally {
                    cdl.countDown();
                }
            }

            @Override
            public void onSuccess(Object value) {
                // ignored
            }

            @Override
            public void onError(Throwable e) {
                // ignored
            }
        });

        try {
            assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertEquals("Reports disposed upfront?", false, b[0]);
        assertEquals("Didn't report disposed after?", true, b[1]);
    }

    /**
     * Checks if the upstream's Disposable sent through the onSubscribe reports
     * isDisposed properly before and after calling dispose.
     * @param source the source to test
     */
    public static void checkDisposed(Completable source) {
        final Boolean[] b = { null, null };
        final CountDownLatch cdl = new CountDownLatch(1);
        source.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                try {
                    b[0] = d.isDisposed();

                    d.dispose();

                    b[1] = d.isDisposed();

                    d.dispose();
                } finally {
                    cdl.countDown();
                }
            }

            @Override
            public void onError(Throwable e) {
                // ignored
            }

            @Override
            public void onComplete() {
                // ignored
            }
        });

        try {
            assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertEquals("Reports disposed upfront?", false, b[0]);
        assertEquals("Didn't report disposed after?", true, b[1]);
    }

    /**
     * Consumer for all base reactive types.
     */
    enum NoOpConsumer implements Observer<Object>, MaybeObserver<Object>, SingleObserver<Object>, CompletableObserver {
        INSTANCE;

        @Override
        public void onSubscribe(Disposable d) {
            // deliberately no-op
        }

        @Override
        public void onSuccess(Object value) {
            // deliberately no-op
        }

        @Override
        public void onError(Throwable e) {
            // deliberately no-op
        }

        @Override
        public void onComplete() {
            // deliberately no-op
        }

        @Override
        public void onNext(Object t) {
            // deliberately no-op
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeMaybe(Function<Maybe<T>, ? extends MaybeSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Maybe<T> source = new Maybe<T>() {
                @Override
                protected void subscribeActual(MaybeObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            MaybeSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeMaybeToSingle(Function<Maybe<T>, ? extends SingleSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Maybe<T> source = new Maybe<T>() {
                @Override
                protected void subscribeActual(MaybeObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            SingleSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeMaybeToObservable(Function<Maybe<T>, ? extends ObservableSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Maybe<T> source = new Maybe<T>() {
                @Override
                protected void subscribeActual(MaybeObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            ObservableSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeSingleToMaybe(Function<Single<T>, ? extends MaybeSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Single<T> source = new Single<T>() {
                @Override
                protected void subscribeActual(SingleObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            MaybeSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeSingleToObservable(Function<Single<T>, ? extends ObservableSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Single<T> source = new Single<T>() {
                @Override
                protected void subscribeActual(SingleObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            ObservableSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param transform the transform to drive an operator
     */
    public static <T> void checkDoubleOnSubscribeMaybeToCompletable(Function<Maybe<T>, ? extends CompletableSource> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Maybe<T> source = new Maybe<T>() {
                @Override
                protected void subscribeActual(MaybeObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            CompletableSource out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeSingle(Function<Single<T>, ? extends SingleSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Single<T> source = new Single<T>() {
                @Override
                protected void subscribeActual(SingleObserver<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            SingleSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeObservable(Function<Observable<T>, ? extends ObservableSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Observable<T> source = new Observable<T>() {
                @Override
                protected void subscribeActual(Observer<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            ObservableSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeObservableToSingle(Function<Observable<T>, ? extends SingleSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Observable<T> source = new Observable<T>() {
                @Override
                protected void subscribeActual(Observer<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            SingleSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param <R> the output value type
     * @param transform the transform to drive an operator
     */
    public static <T, R> void checkDoubleOnSubscribeObservableToMaybe(Function<Observable<T>, ? extends MaybeSource<R>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Observable<T> source = new Observable<T>() {
                @Override
                protected void subscribeActual(Observer<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            MaybeSource<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the input value type
     * @param transform the transform to drive an operator
     */
    public static <T> void checkDoubleOnSubscribeObservableToCompletable(Function<Observable<T>, ? extends CompletableSource> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Observable<T> source = new Observable<T>() {
                @Override
                protected void subscribeActual(Observer<? super T> observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            CompletableSource out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param transform the transform to drive an operator
     */
    public static void checkDoubleOnSubscribeCompletable(Function<Completable, ? extends CompletableSource> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Completable source = new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            CompletableSource out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the output value tye
     * @param transform the transform to drive an operator
     */
    public static <T> void checkDoubleOnSubscribeCompletableToMaybe(Function<Completable, ? extends MaybeSource<T>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Completable source = new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            MaybeSource<T> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the given transformed reactive type reports multiple onSubscribe calls to
     * RxJavaPlugins.
     * @param <T> the output value tye
     * @param transform the transform to drive an operator
     */
    public static <T> void checkDoubleOnSubscribeCompletableToSingle(Function<Completable, ? extends SingleSource<T>> transform) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Completable source = new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    try {
                        Disposable d1 = Disposables.empty();

                        observer.onSubscribe(d1);

                        Disposable d2 = Disposables.empty();

                        observer.onSubscribe(d2);

                        b[0] = d1.isDisposed();
                        b[1] = d2.isDisposed();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            SingleSource<T> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the operator applied to a Maybe source propagates dispose properly.
     * @param <T> the source value type
     * @param <U> the output value type
     * @param composer the function to apply an operator to the provided Maybe source
     */
    public static <T, U> void checkDisposedMaybe(Function<Maybe<T>, ? extends MaybeSource<U>> composer) {
        MaybeSubject<T> pp = MaybeSubject.create();

        TestObserver<U> ts = new TestObserver<U>();

        try {
            composer.apply(pp).subscribe(ts);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertTrue("Not subscribed to source!", pp.hasObservers());

        ts.cancel();

        assertFalse("Dispose not propagated!", pp.hasObservers());
    }

    /**
     * Check if the operator applied to a Completable source propagates dispose properly.
     * @param composer the function to apply an operator to the provided Completable source
     */
    public static void checkDisposedCompletable(Function<Completable, ? extends CompletableSource> composer) {
        CompletableSubject pp = CompletableSubject.create();

        TestObserver<Integer> ts = new TestObserver<Integer>();

        try {
            composer.apply(pp).subscribe(ts);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertTrue("Not subscribed to source!", pp.hasObservers());

        ts.cancel();

        assertFalse("Dispose not propagated!", pp.hasObservers());
    }

    /**
     * Check if the operator applied to a Maybe source propagates dispose properly.
     * @param <T> the source value type
     * @param <U> the output value type
     * @param composer the function to apply an operator to the provided Maybe source
     */
    public static <T, U> void checkDisposedMaybeToSingle(Function<Maybe<T>, ? extends SingleSource<U>> composer) {
        MaybeSubject<T> pp = MaybeSubject.create();

        TestObserver<U> ts = new TestObserver<U>();

        try {
            composer.apply(pp).subscribe(ts);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }

        assertTrue(pp.hasObservers());

        ts.cancel();

        assertFalse(pp.hasObservers());
    }

    /**
     * Check if the TestObserver has a CompositeException with the specified class
     * of Throwables in the given order.
     * @param ts the TestObserver instance
     * @param classes the array of expected Throwables inside the Composite
     */
    public static void assertCompositeExceptions(TestObserver<?> ts, Class<? extends Throwable>... classes) {
        ts
        .assertSubscribed()
        .assertError(CompositeException.class)
        .assertNotComplete();

        List<Throwable> list = TestCommonHelper.compositeList(ts.errors().get(0));

        assertEquals(classes.length, list.size());

        for (int i = 0; i < classes.length; i++) {
            TestCommonHelper.assertError(list, i, classes[i]);
        }
    }

    /**
     * Check if the TestObserver has a CompositeException with the specified class
     * of Throwables in the given order.
     * @param ts the TestObserver instance
     * @param classes the array of subsequent Class and String instances representing the
     * expected Throwable class and the expected error message
     */
    @SuppressWarnings("unchecked")
    public static void assertCompositeExceptions(TestObserver<?> ts, Object... classes) {
        ts
        .assertSubscribed()
        .assertError(CompositeException.class)
        .assertNotComplete();

        List<Throwable> list = TestCommonHelper.compositeList(ts.errors().get(0));

        assertEquals(classes.length, list.size());

        for (int i = 0; i < classes.length; i += 2) {
            TestCommonHelper.assertError(list, i, (Class<Throwable>)classes[i], (String)classes[i + 1]);
        }
    }

    /**
     * Emit the given values and complete the Subject.
     * @param <T> the value type
     * @param p the target subject
     * @param values the values to emit
     */
    public static <T> void emit(Subject<T> p, T... values) {
        for (T v : values) {
            p.onNext(v);
        }
        p.onComplete();
    }

    /**
     * Checks if the source is fuseable and its isEmpty/clear works properly.
     * @param <T> the value type
     * @param source the source sequence
     */
    public static <T> void checkFusedIsEmptyClear(Observable<T> source) {
        final CountDownLatch cdl = new CountDownLatch(1);

        final Boolean[] state = { null, null, null, null };

        source.subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {
                try {
                    if (d instanceof QueueDisposable) {
                        @SuppressWarnings("unchecked")
                        QueueDisposable<Object> qd = (QueueDisposable<Object>) d;
                        state[0] = true;

                        int m = qd.requestFusion(QueueDisposable.ANY);

                        if (m != QueueDisposable.NONE) {
                            state[1] = true;

                            state[2] = qd.isEmpty();

                            qd.clear();

                            state[3] = qd.isEmpty();
                        }
                    }
                    cdl.countDown();
                } finally {
                    d.dispose();
                }
            }

            @Override
            public void onNext(T value) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        try {
            assertTrue(cdl.await(5, TimeUnit.SECONDS));

            assertTrue("Not fuseable", state[0]);
            assertTrue("Fusion rejected", state[1]);

            assertNotNull(state[2]);
            assertTrue("Did not empty", state[3]);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns an expanded error list of the given test consumer.
     * @param to the test consumer instance
     * @return the list
     */
    public static List<Throwable> errorList(TestObserver<?> to) {
        return TestCommonHelper.compositeList(to.errors().get(0));
    }

    /**
     * Tests the given mapping of a bad Observable by emitting the good values, then an error/completion and then
     * a bad value followed by a TestException and and a completion.
     * @param <T> the value type
     * @param mapper the mapper that receives a bad Observable and returns a reactive base type (detected via reflection).
     * @param error if true, the good value emission is followed by a TestException("error"), if false then onComplete is called
     * @param badValue the bad value to emit if not null
     * @param goodValue the good value to emit before turning bad, if not null
     * @param expected the expected resulting values, null to ignore values received
     */
    public static <T> void checkBadSourceObservable(Function<Observable<T>, Object> mapper,
            final boolean error, final T goodValue, final T badValue, final Object... expected) {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Observable<T> bad = new Observable<T>() {
                boolean once;
                @Override
                protected void subscribeActual(Observer<? super T> observer) {
                    observer.onSubscribe(Disposables.empty());

                    if (once) {
                        return;
                    }
                    once = true;

                    if (goodValue != null) {
                        observer.onNext(goodValue);
                    }

                    if (error) {
                        observer.onError(new TestException("error"));
                    } else {
                        observer.onComplete();
                    }

                    if (badValue != null) {
                        observer.onNext(badValue);
                    }
                    observer.onError(new TestException("second"));
                    observer.onComplete();
                }
            };

            Object o = mapper.apply(bad);

            if (o instanceof ObservableSource) {
                ObservableSource<?> os = (ObservableSource<?>) o;
                TestObserver<Object> to = new TestObserver<Object>();

                os.subscribe(to);

                to.awaitDone(5, TimeUnit.SECONDS);

                to.assertSubscribed();

                if (expected != null) {
                    to.assertValues(expected);
                }
                if (error) {
                    to.assertError(TestException.class)
                    .assertErrorMessage("error")
                    .assertNotComplete();
                } else {
                    to.assertNoErrors().assertComplete();
                }
            }

            if (o instanceof SingleSource) {
                SingleSource<?> os = (SingleSource<?>) o;
                TestObserver<Object> to = new TestObserver<Object>();

                os.subscribe(to);

                to.awaitDone(5, TimeUnit.SECONDS);

                to.assertSubscribed();

                if (expected != null) {
                    to.assertValues(expected);
                }
                if (error) {
                    to.assertError(TestException.class)
                    .assertErrorMessage("error")
                    .assertNotComplete();
                } else {
                    to.assertNoErrors().assertComplete();
                }
            }

            if (o instanceof MaybeSource) {
                MaybeSource<?> os = (MaybeSource<?>) o;
                TestObserver<Object> to = new TestObserver<Object>();

                os.subscribe(to);

                to.awaitDone(5, TimeUnit.SECONDS);

                to.assertSubscribed();

                if (expected != null) {
                    to.assertValues(expected);
                }
                if (error) {
                    to.assertError(TestException.class)
                    .assertErrorMessage("error")
                    .assertNotComplete();
                } else {
                    to.assertNoErrors().assertComplete();
                }
            }

            if (o instanceof CompletableSource) {
                CompletableSource os = (CompletableSource) o;
                TestObserver<Object> to = new TestObserver<Object>();

                os.subscribe(to);

                to.awaitDone(5, TimeUnit.SECONDS);

                to.assertSubscribed();

                if (expected != null) {
                    to.assertValues(expected);
                }
                if (error) {
                    to.assertError(TestException.class)
                    .assertErrorMessage("error")
                    .assertNotComplete();
                } else {
                    to.assertNoErrors().assertComplete();
                }
            }

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } catch (AssertionError ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

}
