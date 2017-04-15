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

package io.reactivex.flowable;

import static io.reactivex.common.TestCommonHelper.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.*;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.*;

import hu.akarnokd.reactivestreams.extensions.*;
import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.utils.ExceptionHelper;
import io.reactivex.flowable.internal.subscriptions.BooleanSubscription;
import io.reactivex.flowable.subscribers.TestSubscriber;

/**
 * Common methods for helping with tests from 1.x mostly.
 */
public enum TestHelper {
    ;
    /**
     * Mocks a subscriber and prepares it to request Long.MAX_VALUE.
     * @param <T> the value type
     * @return the mocked subscriber
     */
    @SuppressWarnings("unchecked")
    public static <T> RelaxedSubscriber<T> mockSubscriber() {
        RelaxedSubscriber<T> w = mock(RelaxedSubscriber.class);

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock a) throws Throwable {
                Subscription s = a.getArgument(0);
                s.request(Long.MAX_VALUE);
                return null;
            }
        }).when(w).onSubscribe((Subscription)any());

        return w;
    }

    /**
     * Assert that the offer methods throw UnsupportedOperationExcetpion.
     * @param q the queue implementation
     */
    public static void assertNoOffer(FusedQueue<?> q) {
        try {
            q.offer(null);
            fail("Should have thrown!");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    public static void assertError(TestSubscriber<?> ts, int index, Class<? extends Throwable> clazz) {
        Throwable ex = ts.errors().get(0);
        if (ex instanceof CompositeException) {
            CompositeException ce = (CompositeException) ex;
            List<Throwable> cel = ce.getExceptions();
            assertTrue(cel.get(index).toString(), clazz.isInstance(cel.get(index)));
        } else {
            fail(ex.toString() + ": not a CompositeException");
        }
    }

    public static void assertError(TestSubscriber<?> ts, int index, Class<? extends Throwable> clazz, String message) {
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
     * Assert that by consuming the Publisher with a bad request amount, it is
     * reported to the plugin error handler promptly.
     * @param source the source to consume
     */
    public static void assertBadRequestReported(Publisher<?> source) {
        List<Throwable> list = TestCommonHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl = new CountDownLatch(1);

            source.subscribe(new RelaxedSubscriber<Object>() {

                @Override
                public void onSubscribe(Subscription s) {
                    try {
                        s.request(-99);
                        s.cancel();
                        s.cancel();
                    } finally {
                        cdl.countDown();
                    }
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

            try {
                assertTrue(cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw new AssertionError(ex.getMessage());
            }

            assertTrue(list.toString(), list.get(0) instanceof IllegalArgumentException);
            assertEquals("n > 0 required but it was -99", list.get(0).getMessage());
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Returns an Consumer that asserts the TestSubscriber has exaclty one value + completed
     * normally and that single value is not the value specified.
     * @param <T> the value type
     * @param value the value not expected
     * @return the consumer
     */
    public static <T> Consumer<TestSubscriber<T>> subscriberSingleNot(final T value) {
        return new Consumer<TestSubscriber<T>>() {
            @Override
            public void accept(TestSubscriber<T> ts) throws Exception {
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
     * Calls onSubscribe twice and checks if it doesn't affect the first Subscription while
     * reporting it to plugin error handler.
     * @param subscriber the target
     */
    public static void doubleOnSubscribe(Subscriber<?> subscriber) {
        List<Throwable> errors = trackPluginErrors();
        try {
            BooleanSubscription s1 = new BooleanSubscription();

            subscriber.onSubscribe(s1);

            BooleanSubscription s2 = new BooleanSubscription();

            subscriber.onSubscribe(s2);

            assertFalse(s1.isCancelled());

            assertTrue(s2.isCancelled());

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Subscription already set!");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Checks if the upstream's Subscription sent through the onSubscribe reports
     * isCancelled properly before and after calling dispose.
     * @param <T> the input value type
     * @param source the source to test
     */
    public static <T> void checkDisposed(Flowable<T> source) {
        final TestSubscriber<Object> ts = new TestSubscriber<Object>(0L);
        source.subscribe(new RelaxedSubscriber<Object>() {
            @Override
            public void onSubscribe(Subscription s) {
                ts.onSubscribe(new BooleanSubscription());

                s.cancel();

                s.cancel();
            }

            @Override
            public void onNext(Object t) {
                ts.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                ts.onError(t);
            }

            @Override
            public void onComplete() {
                ts.onComplete();
            }
        });
        ts.assertEmpty();
    }

    /**
     * Consumer for all base reactive types.
     */
    enum NoOpConsumer implements RelaxedSubscriber<Object> {
        INSTANCE;

        @Override
        public void onError(Throwable e) {
            // deliberately no-op
        }

        @Override
        public void onComplete() {
            // deliberately no-op
        }

        @Override
        public void onSubscribe(Subscription s) {
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
    public static <T, R> void checkDoubleOnSubscribeFlowable(Function<Flowable<T>, ? extends Publisher<R>> transform) {
        List<Throwable> errors = trackPluginErrors();
        try {
            final Boolean[] b = { null, null };
            final CountDownLatch cdl = new CountDownLatch(1);

            Flowable<T> source = new Flowable<T>() {
                @Override
                protected void subscribeActual(Subscriber<? super T> subscriber) {
                    try {
                        BooleanSubscription d1 = new BooleanSubscription();

                        subscriber.onSubscribe(d1);

                        BooleanSubscription d2 = new BooleanSubscription();

                        subscriber.onSubscribe(d2);

                        b[0] = d1.isCancelled();
                        b[1] = d2.isCancelled();
                    } finally {
                        cdl.countDown();
                    }
                }
            };

            Publisher<R> out = transform.apply(source);

            out.subscribe(NoOpConsumer.INSTANCE);

            try {
                assertTrue("Timed out", cdl.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }

            assertEquals("First disposed?", false, b[0]);
            assertEquals("Second not disposed?", true, b[1]);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Subscription already set!");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    /**
     * Check if the TestSubscriber has a CompositeException with the specified class
     * of Throwables in the given order.
     * @param ts the TestSubscriber instance
     * @param classes the array of expected Throwables inside the Composite
     */
    public static void assertCompositeExceptions(TestSubscriber<?> ts, Class<? extends Throwable>... classes) {
        ts
        .assertSubscribed()
        .assertError(CompositeException.class)
        .assertNotComplete();

        List<Throwable> list = compositeList(ts.errors().get(0));

        assertEquals(classes.length, list.size());

        for (int i = 0; i < classes.length; i++) {
            TestCommonHelper.assertError(list, i, classes[i]);
        }
    }

    /**
     * Check if the TestSubscriber has a CompositeException with the specified class
     * of Throwables in the given order.
     * @param ts the TestSubscriber instance
     * @param classes the array of subsequent Class and String instances representing the
     * expected Throwable class and the expected error message
     */
    @SuppressWarnings("unchecked")
    public static void assertCompositeExceptions(TestSubscriber<?> ts, Object... classes) {
        ts
        .assertSubscribed()
        .assertError(CompositeException.class)
        .assertNotComplete();

        List<Throwable> list = compositeList(ts.errors().get(0));

        assertEquals(classes.length, list.size());

        for (int i = 0; i < classes.length; i += 2) {
            TestCommonHelper.assertError(list, i, (Class<Throwable>)classes[i], (String)classes[i + 1]);
        }
    }

    /**
     * Emit the given values and complete the Processor.
     * @param <T> the value type
     * @param p the target processor
     * @param values the values to emit
     */
    public static <T> void emit(Processor<T, ?> p, T... values) {
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
    public static <T> void checkFusedIsEmptyClear(Flowable<T> source) {
        final CountDownLatch cdl = new CountDownLatch(1);

        final Boolean[] state = { null, null, null, null };

        source.subscribe(new RelaxedSubscriber<T>() {
            @Override
            public void onSubscribe(Subscription d) {
                try {
                    if (d instanceof FusedQueueSubscription) {
                        @SuppressWarnings("unchecked")
                        FusedQueueSubscription<Object> qd = (FusedQueueSubscription<Object>) d;
                        state[0] = true;

                        int m = qd.requestFusion(FusedQueueSubscription.ANY);

                        if (m != FusedQueueSubscription.NONE) {
                            state[1] = true;

                            state[2] = qd.isEmpty();

                            qd.clear();

                            state[3] = qd.isEmpty();
                        }
                    }
                    cdl.countDown();
                } finally {
                    d.cancel();
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
    public static List<Throwable> errorList(TestSubscriber<?> to) {
        return compositeList(to.errors().get(0));
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
    public static <T> void checkBadSourceFlowable(Function<Flowable<T>, Object> mapper,
            final boolean error, final T goodValue, final T badValue, final Object... expected) {
        List<Throwable> errors = trackPluginErrors();
        try {
            Flowable<T> bad = new Flowable<T>() {
                @Override
                protected void subscribeActual(Subscriber<? super T> observer) {
                    observer.onSubscribe(new BooleanSubscription());

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

            if (o instanceof Publisher) {
                Publisher<?> os = (Publisher<?>) o;
                TestSubscriber<Object> to = new TestSubscriber<Object>();

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

            assertUndeliverable(errors, 0, TestException.class, "second");
        } catch (AssertionError ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    public static <T> void checkInvalidParallelSubscribers(ParallelFlowable<T> source) {
        int n = source.parallelism();

        @SuppressWarnings("unchecked")
        TestSubscriber<Object>[] tss = new TestSubscriber[n + 1];
        for (int i = 0; i <= n; i++) {
            tss[i] = new TestSubscriber<Object>().withTag("" + i);
        }

        source.subscribe(tss);

        for (int i = 0; i <= n; i++) {
            tss[i].assertFailure(IllegalArgumentException.class);
        }
    }
}
