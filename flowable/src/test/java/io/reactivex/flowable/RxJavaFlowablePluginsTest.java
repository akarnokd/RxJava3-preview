/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.flowable;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.*;
import org.reactivestreams.*;

import io.reactivex.common.TestCommonHelper;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.Functions;
import io.reactivex.flowable.internal.operators.*;
import io.reactivex.flowable.internal.subscriptions.ScalarSubscription;

public class RxJavaFlowablePluginsTest {

    @Test
    public void constructorShouldBePrivate() {
        TestCommonHelper.checkUtilityClass(RxJavaFlowablePlugins.class);
    }

    @Test
    @Ignore("Not part of 2.0")
    public void assemblyTrackingObservable() {
//        RxJavaFlowablePlugins.enableAssemblyTracking();
//        try {
//            TestSubscriber<Integer> ts = TestSubscriber.create();
//
//            createObservable().subscribe(ts);
//
//            ts.assertError(TestException.class);
//
//            Throwable ex = ts.getOnErrorEvents().get(0);
//
//            AssemblyStackTraceException aste = AssemblyStackTraceException.find(ex);
//
//            assertNotNull(aste);
//
//            assertTrue(aste.getMessage(), aste.getMessage().contains("createObservable"));
//
//            RxJavaFlowablePlugins.clearAssemblyTracking();
//
//            ts = TestSubscriber.create();
//
//            createObservable().subscribe(ts);
//
//            ts.assertError(TestException.class);
//
//            ex = ts.getOnErrorEvents().get(0);
//
//            aste = AssemblyStackTraceException.find(ex);
//
//            assertNull(aste);
//        } finally {
//            RxJavaFlowablePlugins.resetAssemblyTracking();
//        }
    }

//    static Single<Integer> createSingle() {
//        return Single.just(1).map(new Function<Integer, Integer>() {
//            @Override
//            public Integer apply(Integer t) {
//                throw new TestException();
//            }
//        });
//    }

    @Test
    @Ignore("Not part of 2.0")
    public void assemblyTrackingSingle() {
//        RxJavaFlowablePlugins.enableAssemblyTracking();
//        try {
//            TestSubscriber<Integer> ts = TestSubscriber.create();
//
//            createSingle().subscribe(ts);
//
//            ts.assertError(TestException.class);
//
//            Throwable ex = ts.getOnErrorEvents().get(0);
//
//            AssemblyStackTraceException aste = AssemblyStackTraceException.find(ex);
//
//            assertNotNull(aste);
//
//            assertTrue(aste.getMessage(), aste.getMessage().contains("createSingle"));
//
//            RxJavaFlowablePlugins.clearAssemblyTracking();
//
//            ts = TestSubscriber.create();
//
//            createSingle().subscribe(ts);
//
//            ts.assertError(TestException.class);
//
//            ex = ts.getOnErrorEvents().get(0);
//
//            aste = AssemblyStackTraceException.find(ex);
//
//            assertNull(aste);
//        } finally {
//            RxJavaFlowablePlugins.resetAssemblyTracking();
//        }
    }

//    static Completable createCompletable() {
//        return Completable.error(new Callable<Throwable>() {
//            @Override
//            public Throwable call() {
//                return new TestException();
//            }
//        });
//    }

    @Test
    @Ignore("Not part of 2.0")
    public void assemblyTrackingCompletable() {
//        RxJavaFlowablePlugins.enableAssemblyTracking();
//        try {
//            TestSubscriber<Integer> ts = TestSubscriber.create();
//
//            createCompletable().subscribe(ts);
//
//            ts.assertError(TestException.class);
//
//            Throwable ex = ts.getOnErrorEvents().get(0);
//
//            AssemblyStackTraceException aste = AssemblyStackTraceException.find(ex);
//
//            assertNotNull(aste);
//
//            assertTrue(aste.getMessage(), aste.getMessage().contains("createCompletable"));
//
//            RxJavaFlowablePlugins.clearAssemblyTracking();
//
//            ts = TestSubscriber.create();
//
//            createCompletable().subscribe(ts);
//
//            ts.assertError(TestException.class);
//
//            ex = ts.getOnErrorEvents().get(0);
//
//            aste = AssemblyStackTraceException.find(ex);
//
//            assertNull(aste);
//
//        } finally {
//            RxJavaFlowablePlugins.resetAssemblyTracking();
//        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void lockdown() throws Exception {
        RxJavaFlowablePlugins.reset();
        RxJavaFlowablePlugins.lockdown();
        try {
            assertTrue(RxJavaFlowablePlugins.isLockdown());
            Consumer a1 = Functions.emptyConsumer();
            Callable f0 = new Callable() {
                @Override
                public Object call() {
                    return null;
                }
            };
            Function f1 = Functions.identity();
            BiFunction f2 = new BiFunction() {
                @Override
                public Object apply(Object t1, Object t2) {
                    return t2;
                }
            };

            BooleanSupplier bs = new BooleanSupplier() {
                @Override
                public boolean getAsBoolean() throws Exception {
                    return true;
                }
            };

            for (Method m : RxJavaFlowablePlugins.class.getMethods()) {
                if (m.getName().startsWith("set")) {

                    Method getter;

                    Class<?> paramType = m.getParameterTypes()[0];

                    if (paramType == Boolean.TYPE) {
                        getter = RxJavaFlowablePlugins.class.getMethod("is" + m.getName().substring(3));
                    } else {
                        getter = RxJavaFlowablePlugins.class.getMethod("get" + m.getName().substring(3));
                    }

                    Object before = getter.invoke(null);

                    try {
                        if (paramType.isAssignableFrom(Boolean.TYPE)) {
                            m.invoke(null, true);
                        } else
                        if (paramType.isAssignableFrom(Callable.class)) {
                            m.invoke(null, f0);
                        } else
                        if (paramType.isAssignableFrom(Function.class)) {
                            m.invoke(null, f1);
                        } else
                        if (paramType.isAssignableFrom(Consumer.class)) {
                            m.invoke(null, a1);
                        } else
                        if (paramType.isAssignableFrom(BooleanSupplier.class)) {
                            m.invoke(null, bs);
                        } else {
                            m.invoke(null, f2);
                        }
                        fail("Should have thrown InvocationTargetException(IllegalStateException)");
                    } catch (InvocationTargetException ex) {
                        if (ex.getCause() instanceof IllegalStateException) {
                            assertEquals("Plugins can't be changed anymore",ex.getCause().getMessage());
                        } else {
                            fail("Should have thrown InvocationTargetException(IllegalStateException)");
                        }
                    }

                    Object after = getter.invoke(null);

                    if (paramType.isPrimitive()) {
                        assertEquals(m.toString(), before, after);
                    } else {
                        assertSame(m.toString(), before, after);
                    }
                }
            }

//            Object o1 = RxJavaFlowablePlugins.getOnObservableCreate();
//            Object o2 = RxJavaFlowablePlugins.getOnSingleCreate();
//            Object o3 = RxJavaFlowablePlugins.getOnCompletableCreate();
//
//            RxJavaFlowablePlugins.enableAssemblyTracking();
//            RxJavaFlowablePlugins.clearAssemblyTracking();
//            RxJavaFlowablePlugins.resetAssemblyTracking();
//
//
//            assertSame(o1, RxJavaFlowablePlugins.getOnObservableCreate());
//            assertSame(o2, RxJavaFlowablePlugins.getOnSingleCreate());
//            assertSame(o3, RxJavaFlowablePlugins.getOnCompletableCreate());

        } finally {
            RxJavaFlowablePlugins.unlock();
            RxJavaFlowablePlugins.reset();
            assertFalse(RxJavaFlowablePlugins.isLockdown());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void flowableCreate() {
        try {
            RxJavaFlowablePlugins.setOnFlowableAssembly(new Function<Flowable, Flowable>() {
                @Override
                public Flowable apply(Flowable t) {
                    return new FlowableRange(1, 2);
                }
            });

            Flowable.range(10, 3)
            .test()
            .assertValues(1, 2)
            .assertNoErrors()
            .assertComplete();
        } finally {
            RxJavaFlowablePlugins.reset();
        }
        // make sure the reset worked
        Flowable.range(10, 3)
        .test()
        .assertValues(10, 11, 12)
        .assertNoErrors()
        .assertComplete();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void flowableStart() {
        try {
            RxJavaFlowablePlugins.setOnFlowableSubscribe(new BiFunction<Flowable, Subscriber, Subscriber>() {
                @Override
                public Subscriber apply(Flowable o, final Subscriber t) {
                    return new Subscriber() {

                        @Override
                        public void onSubscribe(Subscription d) {
                            t.onSubscribe(d);
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void onNext(Object value) {
                            t.onNext((Integer)value - 9);
                        }

                        @Override
                        public void onError(Throwable e) {
                            t.onError(e);
                        }

                        @Override
                        public void onComplete() {
                            t.onComplete();
                        }

                    };
                }
            });

            Flowable.range(10, 3)
            .test()
            .assertValues(1, 2, 3)
            .assertNoErrors()
            .assertComplete();
        } finally {
            RxJavaFlowablePlugins.reset();
        }
        // make sure the reset worked
        Flowable.range(10, 3)
        .test()
        .assertValues(10, 11, 12)
        .assertNoErrors()
        .assertComplete();
    }

    @Test
    @Ignore("Different architecture, no longer supported")
    public void observableReturn() {
//        try {
//            final Subscription s = Subscriptions.empty();
//
//            RxJavaFlowablePlugins.setOnObservableReturn(new Function<Subscription, Subscription>() {
//                @Override
//                public Subscription call(Subscription t) {
//                    return s;
//                }
//            });
//
//            TestSubscriber<Integer> ts = TestSubscriber.create();
//
//            Subscription u = Observable.range(10, 3).subscribe(ts);
//
//            ts.assertValues(10, 11, 12);
//            ts.assertNoErrors();
//            ts.assertComplete();
//
//            assertSame(s, u);
//        } finally {
//            RxJavaFlowablePlugins.reset();
//        }
    }

    @Test
    @Ignore("Different architecture, no longer supported")
    public void singleReturn() {
//        try {
//            final Subscription s = Subscriptions.empty();
//
//            RxJavaFlowablePlugins.setOnSingleReturn(new Function<Subscription, Subscription>() {
//                @Override
//                public Subscription call(Subscription t) {
//                    return s;
//                }
//            });
//
//            TestSubscriber<Integer> ts = TestSubscriber.create();
//
//            Subscription u = Single.just(1).subscribe(ts);
//
//            ts.assertValue(1);
//            ts.assertNoErrors();
//            ts.assertComplete();
//
//            assertSame(s, u);
//        } finally {
//            RxJavaFlowablePlugins.reset();
//        }
    }

    @Test
    @Ignore("No (need for) clear() method in 2.0")
    public void clear() throws Exception {
//        RxJavaFlowablePlugins.reset();
//        try {
//            RxJavaFlowablePlugins.clear();
//            for (Method m : RxJavaFlowablePlugins.class.getMethods()) {
//                if (m.getName().startsWith("getOn")) {
//                    assertNull(m.toString(), m.invoke(null));
//                }
//            }
//
//        } finally {
//            RxJavaFlowablePlugins.reset();
//        }
//
//        for (Method m : RxJavaFlowablePlugins.class.getMethods()) {
//            if (m.getName().startsWith("getOn")
//                    && !m.getName().endsWith("Scheduler")
//                    && !m.getName().contains("GenericScheduledExecutorService")) {
//                assertNotNull(m.toString(), m.invoke(null));
//            }
//        }
    }

    /**
     * Ensure set*() accepts a consumers/functions with wider bounds.
     * @throws Exception on error
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void onErrorWithSuper() throws Exception {
        try {

            Function<? super ConnectableFlowable, ? extends ConnectableFlowable> connectableFlowable2ConnectableFlowable = new Function<ConnectableFlowable, ConnectableFlowable>() {
                @Override
                public ConnectableFlowable apply(ConnectableFlowable connectableFlowable) throws Exception {
                    return connectableFlowable;
                }
            };
            Function<? super Flowable, ? extends Flowable> flowable2Flowable = new Function<Flowable, Flowable>() {
                @Override
                public Flowable apply(Flowable flowable) throws Exception {
                    return flowable;
                }
            };
            BiFunction<? super Flowable, ? super Subscriber, ? extends Subscriber> flowable2subscriber = new BiFunction<Flowable, Subscriber, Subscriber>() {
                @Override
                public Subscriber apply(Flowable flowable, Subscriber subscriber) throws Exception {
                    return subscriber;
                }
            };
            Function<? super ParallelFlowable, ? extends ParallelFlowable> parallelFlowable2parallelFlowable = new Function<ParallelFlowable, ParallelFlowable>() {
                @Override
                public ParallelFlowable apply(ParallelFlowable parallelFlowable) throws Exception {
                    return parallelFlowable;
                }
            };

            RxJavaFlowablePlugins.setOnConnectableFlowableAssembly(connectableFlowable2ConnectableFlowable);
            RxJavaFlowablePlugins.setOnFlowableAssembly(flowable2Flowable);
            RxJavaFlowablePlugins.setOnFlowableSubscribe(flowable2subscriber);
            RxJavaFlowablePlugins.setOnParallelAssembly(parallelFlowable2parallelFlowable);
        } finally {
            RxJavaFlowablePlugins.reset();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    @Test
    public void clearIsPassthrough() {
        try {
            RxJavaFlowablePlugins.reset();

            assertNull(RxJavaFlowablePlugins.onAssembly((Flowable)null));

            assertNull(RxJavaFlowablePlugins.onAssembly((ConnectableFlowable)null));

            Flowable fos = new Flowable() {
                @Override
                public void subscribeActual(Subscriber t) {

                }
            };

            assertSame(fos, RxJavaFlowablePlugins.onAssembly(fos));

            class AllSubscriber implements Subscriber {

                @Override
                public void onSubscribe(Subscription s) {

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

            }

            AllSubscriber all = new AllSubscriber();

            assertNull(RxJavaFlowablePlugins.onSubscribe(Flowable.never(), null));

            assertSame(all, RxJavaFlowablePlugins.onSubscribe(Flowable.never(), all));

            // These hooks don't exist in 2.0
//            Subscription subscription = Subscriptions.empty();
//
//            assertNull(RxJavaFlowablePlugins.onObservableReturn(null));
//
//            assertSame(subscription, RxJavaFlowablePlugins.onObservableReturn(subscription));
//
//            assertNull(RxJavaFlowablePlugins.onSingleReturn(null));
//
//            assertSame(subscription, RxJavaFlowablePlugins.onSingleReturn(subscription));
//
//            TestException ex = new TestException();
//
//            assertNull(RxJavaFlowablePlugins.onObservableError(null));
//
//            assertSame(ex, RxJavaFlowablePlugins.onObservableError(ex));
//
//            assertNull(RxJavaFlowablePlugins.onSingleError(null));
//
//            assertSame(ex, RxJavaFlowablePlugins.onSingleError(ex));
//
//            assertNull(RxJavaFlowablePlugins.onCompletableError(null));
//
//            assertSame(ex, RxJavaFlowablePlugins.onCompletableError(ex));
//
//            Observable.Operator oop = new Observable.Operator() {
//                @Override
//                public Object call(Object t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaFlowablePlugins.onObservableLift(null));
//
//            assertSame(oop, RxJavaFlowablePlugins.onObservableLift(oop));
//
//            assertNull(RxJavaFlowablePlugins.onSingleLift(null));
//
//            assertSame(oop, RxJavaFlowablePlugins.onSingleLift(oop));
//
//            Completable.CompletableOperator cop = new Completable.CompletableOperator() {
//                @Override
//                public CompletableSubscriber call(CompletableSubscriber t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaFlowablePlugins.onCompletableLift(null));
//
//            assertSame(cop, RxJavaFlowablePlugins.onCompletableLift(cop));

        } finally {
            RxJavaFlowablePlugins.reset();
        }
    }

    static void assertTestException(List<Throwable> list, int index, String message) {
        assertTrue(list.get(index).toString(), list.get(index) instanceof TestException);
        assertEquals(message, list.get(index).getMessage());
    }

    static void assertUndeliverableTestException(List<Throwable> list, int index, String message) {
        assertTrue(list.get(index).toString(), list.get(index).getCause() instanceof TestException);
        assertEquals(message, list.get(index).getCause().getMessage());
    }

    static void assertNPE(List<Throwable> list, int index) {
        assertTrue(list.get(index).toString(), list.get(index) instanceof NullPointerException);
    }

    @Test
    @Ignore("Not present in 2.0")
    public void onXError() {
//        try {
//            final List<Throwable> list = new ArrayList<Throwable>();
//
//            final TestException ex = new TestException();
//
//            Function<Throwable, Throwable> errorHandler = new Function<Throwable, Throwable>() {
//                @Override
//                public Throwable a(Throwable t) {
//                    list.add(t);
//                    return ex;
//                }
//            };
//
//            RxJavaFlowablePlugins.setOnObservableSubscribeError(errorHandler);
//
//            RxJavaFlowablePlugins.setOnSingleSubscribeError(errorHandler);
//
//            RxJavaFlowablePlugins.setOnCompletableSubscribeError(errorHandler);
//
//            assertSame(ex, RxJavaFlowablePlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaFlowablePlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaFlowablePlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaFlowablePlugins.reset();
//        }
    }

//    @SuppressWarnings("deprecation")
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXError() {
//        try {
//            RxJavaFlowablePlugins.reset();
//
//            final List<Throwable> list = new ArrayList<Throwable>();
//
//            final TestException ex = new TestException();
//
//            final Function<Throwable, Throwable> errorHandler = new Function<Throwable, Throwable>() {
//                @Override
//                public Throwable apply(Throwable t) {
//                    list.add(t);
//                    return ex;
//                }
//            };
//
//            RxJavaFlowablePlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaFlowablePlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaFlowablePlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            assertSame(ex, RxJavaFlowablePlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaFlowablePlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaFlowablePlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaFlowablePlugins.getInstance().reset();
//            RxJavaFlowablePlugins.reset();
//        }
    }

//    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @Ignore("Not present in 2.0")
    public void onXLift() {
//        try {
//            Completable.CompletableOperator cop = new Completable.CompletableOperator() {
//                @Override
//                public CompletableSubscriber call(CompletableSubscriber t) {
//                    return t;
//                }
//            };
//
//            Observable.Operator oop = new Observable.Operator() {
//                @Override
//                public Object call(Object t) {
//                    return t;
//                }
//            };
//
//            final int[] counter = { 0 };
//
//            RxJavaFlowablePlugins.setOnObservableLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaFlowablePlugins.setOnSingleLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaFlowablePlugins.setOnCompletableLift(new Function<CompletableOperator, CompletableOperator>() {
//                @Override
//                public CompletableOperator call(CompletableOperator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            assertSame(oop, RxJavaFlowablePlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaFlowablePlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaFlowablePlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaFlowablePlugins.reset();
//        }
    }

//    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXLift() {
//        try {
//
//            RxJavaFlowablePlugins.getInstance().reset();
//            RxJavaFlowablePlugins.reset();
//
//            Completable.CompletableOperator cop = new Completable.CompletableOperator() {
//                @Override
//                public CompletableSubscriber call(CompletableSubscriber t) {
//                    return t;
//                }
//            };
//
//            Observable.Operator oop = new Observable.Operator() {
//                @Override
//                public Object call(Object t) {
//                    return t;
//                }
//            };
//
//            final int[] counter = { 0 };
//
//            final Function<Operator, Operator> onObservableLift = new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            };
//
//            final Function<CompletableOperator, CompletableOperator> onCompletableLift = new Function<CompletableOperator, CompletableOperator>() {
//                @Override
//                public CompletableOperator call(CompletableOperator t) {
//                    counter[0]++;
//                    return t;
//                }
//            };
//
//            RxJavaFlowablePlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaFlowablePlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaFlowablePlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public CompletableOperator onLift(CompletableOperator lift) {
//                    return onCompletableLift.call(lift);
//                }
//            });
//
//            assertSame(oop, RxJavaFlowablePlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaFlowablePlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaFlowablePlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaFlowablePlugins.reset();
//        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void overrideConnectableFlowable() {
        try {
            RxJavaFlowablePlugins.setOnConnectableFlowableAssembly(new Function<ConnectableFlowable, ConnectableFlowable>() {
                @Override
                public ConnectableFlowable apply(ConnectableFlowable co) throws Exception {
                    return new ConnectableFlowable() {

                        @Override
                        public void connect(Consumer connection) {

                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        protected void subscribeActual(Subscriber subscriber) {
                            subscriber.onSubscribe(new ScalarSubscription(subscriber, 10));
                        }
                    };
                }
            });

            Flowable
            .just(1)
            .publish()
            .autoConnect()
            .test()
            .assertResult(10);

        } finally {
            RxJavaFlowablePlugins.reset();
        }

        Flowable
        .just(1)
        .publish()
        .autoConnect()
        .test()
        .assertResult(1);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void assemblyHookCrashes() {
        try {
            RxJavaFlowablePlugins.setOnFlowableAssembly(new Function<Flowable, Flowable>() {
                @Override
                public Flowable apply(Flowable f) throws Exception {
                    throw new IllegalArgumentException();
                }
            });

            try {
                Flowable.empty();
                fail("Should have thrown!");
            } catch (IllegalArgumentException ex) {
                // expected
            }

            RxJavaFlowablePlugins.setOnFlowableAssembly(new Function<Flowable, Flowable>() {
                @Override
                public Flowable apply(Flowable f) throws Exception {
                    throw new InternalError();
                }
            });

            try {
                Flowable.empty();
                fail("Should have thrown!");
            } catch (InternalError ex) {
                // expected
            }

            RxJavaFlowablePlugins.setOnFlowableAssembly(new Function<Flowable, Flowable>() {
                @Override
                public Flowable apply(Flowable f) throws Exception {
                    throw new IOException();
                }
            });

            try {
                Flowable.empty();
                fail("Should have thrown!");
            } catch (RuntimeException ex) {
                if (!(ex.getCause() instanceof IOException)) {
                    fail(ex.getCause().toString() + ": Should have thrown RuntimeException(IOException)");
                }
            }
        } finally {
            RxJavaFlowablePlugins.reset();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void subscribeHookCrashes() {
        try {
            RxJavaFlowablePlugins.setOnFlowableSubscribe(new BiFunction<Flowable, Subscriber, Subscriber>() {
                @Override
                public Subscriber apply(Flowable f, Subscriber s) throws Exception {
                    throw new IllegalArgumentException();
                }
            });

            try {
                Flowable.empty().test();
                fail("Should have thrown!");
            } catch (NullPointerException ex) {
                if (!(ex.getCause() instanceof IllegalArgumentException)) {
                    fail(ex.getCause().toString() + ": Should have thrown NullPointerException(IllegalArgumentException)");
                }
            }

            RxJavaFlowablePlugins.setOnFlowableSubscribe(new BiFunction<Flowable, Subscriber, Subscriber>() {
                @Override
                public Subscriber apply(Flowable f, Subscriber s) throws Exception {
                    throw new InternalError();
                }
            });

            try {
                Flowable.empty().test();
                fail("Should have thrown!");
            } catch (InternalError ex) {
                // expected
            }

            RxJavaFlowablePlugins.setOnFlowableSubscribe(new BiFunction<Flowable, Subscriber, Subscriber>() {
                @Override
                public Subscriber apply(Flowable f, Subscriber s) throws Exception {
                    throw new IOException();
                }
            });

            try {
                Flowable.empty().test();
                fail("Should have thrown!");
            } catch (NullPointerException ex) {
                if (!(ex.getCause() instanceof RuntimeException)) {
                    fail(ex.getCause().toString() + ": Should have thrown NullPointerException(RuntimeException(IOException))");
                }
                if (!(ex.getCause().getCause() instanceof IOException)) {
                    fail(ex.getCause().toString() + ": Should have thrown NullPointerException(RuntimeException(IOException))");
                }
            }
        } finally {
            RxJavaFlowablePlugins.reset();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void onParallelAssembly() {
        try {
            RxJavaFlowablePlugins.setOnParallelAssembly(new Function<ParallelFlowable, ParallelFlowable>() {
                @Override
                public ParallelFlowable apply(ParallelFlowable pf) throws Exception {
                    return new ParallelFromPublisher<Integer>(Flowable.just(2), 2, 2);
                }
            });

            Flowable.just(1)
            .parallel()
            .sequential()
            .test()
            .assertResult(2);
        } finally {
            RxJavaFlowablePlugins.reset();
        }

        Flowable.just(1)
        .parallel()
        .sequential()
        .test()
        .assertResult(1);
    }
}
