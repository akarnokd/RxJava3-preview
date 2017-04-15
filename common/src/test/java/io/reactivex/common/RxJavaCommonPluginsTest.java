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

package io.reactivex.common;

import static org.junit.Assert.*;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import io.reactivex.common.Scheduler.Worker;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.Functions;
import io.reactivex.common.internal.schedulers.ImmediateThinScheduler;

public class RxJavaCommonPluginsTest {

    @Test
    public void constructorShouldBePrivate() {
        TestCommonHelper.checkUtilityClass(RxJavaCommonPlugins.class);
    }

    @Test
    @Ignore("Not part of 2.0")
    public void assemblyTrackingObservable() {
//        RxJavaCommonPlugins.enableAssemblyTracking();
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
//            RxJavaCommonPlugins.clearAssemblyTracking();
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
//            RxJavaCommonPlugins.resetAssemblyTracking();
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
//        RxJavaCommonPlugins.enableAssemblyTracking();
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
//            RxJavaCommonPlugins.clearAssemblyTracking();
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
//            RxJavaCommonPlugins.resetAssemblyTracking();
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
//        RxJavaCommonPlugins.enableAssemblyTracking();
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
//            RxJavaCommonPlugins.clearAssemblyTracking();
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
//            RxJavaCommonPlugins.resetAssemblyTracking();
//        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void lockdown() throws Exception {
        RxJavaCommonPlugins.reset();
        RxJavaCommonPlugins.lockdown();
        try {
            assertTrue(RxJavaCommonPlugins.isLockdown());
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

            for (Method m : RxJavaCommonPlugins.class.getMethods()) {
                if (m.getName().startsWith("set")) {

                    Method getter;

                    Class<?> paramType = m.getParameterTypes()[0];

                    if (paramType == Boolean.TYPE) {
                        getter = RxJavaCommonPlugins.class.getMethod("is" + m.getName().substring(3));
                    } else {
                        getter = RxJavaCommonPlugins.class.getMethod("get" + m.getName().substring(3));
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

//            Object o1 = RxJavaCommonPlugins.getOnObservableCreate();
//            Object o2 = RxJavaCommonPlugins.getOnSingleCreate();
//            Object o3 = RxJavaCommonPlugins.getOnCompletableCreate();
//
//            RxJavaCommonPlugins.enableAssemblyTracking();
//            RxJavaCommonPlugins.clearAssemblyTracking();
//            RxJavaCommonPlugins.resetAssemblyTracking();
//
//
//            assertSame(o1, RxJavaCommonPlugins.getOnObservableCreate());
//            assertSame(o2, RxJavaCommonPlugins.getOnSingleCreate());
//            assertSame(o3, RxJavaCommonPlugins.getOnCompletableCreate());

        } finally {
            RxJavaCommonPlugins.unlock();
            RxJavaCommonPlugins.reset();
            assertFalse(RxJavaCommonPlugins.isLockdown());
        }
    }

    Function<Scheduler, Scheduler> replaceWithImmediate = new Function<Scheduler, Scheduler>() {
        @Override
        public Scheduler apply(Scheduler t) {
            return ImmediateThinScheduler.INSTANCE;
        }
    };

    @Test
    public void overrideSingleScheduler() {
        try {
            RxJavaCommonPlugins.setSingleSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.single());
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.single());
    }

    @Test
    public void overrideComputationScheduler() {
        try {
            RxJavaCommonPlugins.setComputationSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.computation());
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.computation());
    }

    @Test
    public void overrideIoScheduler() {
        try {
            RxJavaCommonPlugins.setIoSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.io());
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.io());
    }

    @Test
    public void overrideNewThreadScheduler() {
        try {
            RxJavaCommonPlugins.setNewThreadSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.newThread());
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.newThread());
    }

    Function<Callable<Scheduler>, Scheduler> initReplaceWithImmediate = new Function<Callable<Scheduler>, Scheduler>() {
        @Override
        public Scheduler apply(Callable<Scheduler> t) {
            return ImmediateThinScheduler.INSTANCE;
        }
    };

    @Test
    public void overrideInitSingleScheduler() {
        final Scheduler s = Schedulers.single(); // make sure the Schedulers is initialized
        Callable<Scheduler> c = new Callable<Scheduler>() {
            @Override
            public Scheduler call() throws Exception {
                return s;
            }
        };
        try {
            RxJavaCommonPlugins.setInitSingleSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaCommonPlugins.initSingleScheduler(c));
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaCommonPlugins.initSingleScheduler(c));
    }

    @Test
    public void overrideInitComputationScheduler() {
        final Scheduler s = Schedulers.computation(); // make sure the Schedulers is initialized
        Callable<Scheduler> c = new Callable<Scheduler>() {
            @Override
            public Scheduler call() throws Exception {
                return s;
            }
        };
        try {
            RxJavaCommonPlugins.setInitComputationSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaCommonPlugins.initComputationScheduler(c));
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaCommonPlugins.initComputationScheduler(c));
    }

    @Test
    public void overrideInitIoScheduler() {
        final Scheduler s = Schedulers.io(); // make sure the Schedulers is initialized;
        Callable<Scheduler> c = new Callable<Scheduler>() {
            @Override
            public Scheduler call() throws Exception {
                return s;
            }
        };
        try {
            RxJavaCommonPlugins.setInitIoSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaCommonPlugins.initIoScheduler(c));
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaCommonPlugins.initIoScheduler(c));
    }

    @Test
    public void overrideInitNewThreadScheduler() {
        final Scheduler s = Schedulers.newThread(); // make sure the Schedulers is initialized;
        Callable<Scheduler> c = new Callable<Scheduler>() {
            @Override
            public Scheduler call() throws Exception {
                return s;
            }
        };
        try {
            RxJavaCommonPlugins.setInitNewThreadSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaCommonPlugins.initNewThreadScheduler(c));
        } finally {
            RxJavaCommonPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaCommonPlugins.initNewThreadScheduler(c));
    }

    Callable<Scheduler> nullResultCallable = new Callable<Scheduler>() {
        @Override
        public Scheduler call() throws Exception {
            return null;
        }
    };

    @Test
    public void overrideInitSingleSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaCommonPlugins.initSingleScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaCommonPlugins.initSingleScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    @Test
    public void overrideInitComputationSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaCommonPlugins.initComputationScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaCommonPlugins.initComputationScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    @Test
    public void overrideInitIoSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaCommonPlugins.initIoScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaCommonPlugins.initIoScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    @Test
    public void overrideInitNewThreadSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaCommonPlugins.initNewThreadScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            // expected
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaCommonPlugins.initNewThreadScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    Callable<Scheduler> unsafeDefault = new Callable<Scheduler>() {
        @Override
        public Scheduler call() throws Exception {
            throw new AssertionError("Default Scheduler instance should not have been evaluated");
        }
    };

    @Test
    public void testDefaultSingleSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaCommonPlugins.setInitSingleSchedulerHandler(initReplaceWithImmediate);
            RxJavaCommonPlugins.initSingleScheduler(unsafeDefault);
        } finally {
            RxJavaCommonPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.single());
    }

    @Test
    public void testDefaultIoSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaCommonPlugins.setInitIoSchedulerHandler(initReplaceWithImmediate);
            RxJavaCommonPlugins.initIoScheduler(unsafeDefault);
        } finally {
            RxJavaCommonPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.io());
    }

    @Test
    public void testDefaultComputationSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaCommonPlugins.setInitComputationSchedulerHandler(initReplaceWithImmediate);
            RxJavaCommonPlugins.initComputationScheduler(unsafeDefault);
        } finally {
            RxJavaCommonPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.computation());
    }

    @Test
    public void testDefaultNewThreadSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaCommonPlugins.setInitNewThreadSchedulerHandler(initReplaceWithImmediate);
            RxJavaCommonPlugins.initNewThreadScheduler(unsafeDefault);
        } finally {
            RxJavaCommonPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.newThread());
    }

    @Test
    @Ignore("Different architecture, no longer supported")
    public void observableReturn() {
//        try {
//            final Subscription s = Subscriptions.empty();
//
//            RxJavaCommonPlugins.setOnObservableReturn(new Function<Subscription, Subscription>() {
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
//            RxJavaCommonPlugins.reset();
//        }
    }

    @Test
    @Ignore("Different architecture, no longer supported")
    public void singleReturn() {
//        try {
//            final Subscription s = Subscriptions.empty();
//
//            RxJavaCommonPlugins.setOnSingleReturn(new Function<Subscription, Subscription>() {
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
//            RxJavaCommonPlugins.reset();
//        }
    }

    void onSchedule(Worker w) throws InterruptedException {
        try {
            try {
                final AtomicInteger value = new AtomicInteger();
                final CountDownLatch cdl = new CountDownLatch(1);

                RxJavaCommonPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
                    @Override
                    public Runnable apply(Runnable t) {
                        return new Runnable() {
                            @Override
                            public void run() {
                                value.set(10);
                                cdl.countDown();
                            }
                        };
                    }
                });

                w.schedule(new Runnable() {
                    @Override
                    public void run() {
                        value.set(1);
                        cdl.countDown();
                    }
                });

                cdl.await();

                assertEquals(10, value.get());

            } finally {

                RxJavaCommonPlugins.reset();
            }

            // make sure the reset worked
            final AtomicInteger value = new AtomicInteger();
            final CountDownLatch cdl = new CountDownLatch(1);

            w.schedule(new Runnable() {
                @Override
                public void run() {
                    value.set(1);
                    cdl.countDown();
                }
            });

            cdl.await();

            assertEquals(1, value.get());
        } finally {
            w.dispose();
        }
    }

    @Test
    public void onScheduleComputation() throws InterruptedException {
        onSchedule(Schedulers.computation().createWorker());
    }

    @Test
    public void onScheduleIO() throws InterruptedException {
        onSchedule(Schedulers.io().createWorker());
    }

    @Test
    public void onScheduleNewThread() throws InterruptedException {
        onSchedule(Schedulers.newThread().createWorker());
    }

    @Test
    public void onError() {
        try {
            final List<Throwable> list = new ArrayList<Throwable>();

            RxJavaCommonPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable t) {
                    list.add(t);
                }
            });

            RxJavaCommonPlugins.onError(new TestException("Forced failure"));

            assertEquals(1, list.size());
            assertUndeliverableTestException(list, 0, "Forced failure");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    @Ignore("No (need for) clear() method in 2.0")
    public void clear() throws Exception {
//        RxJavaCommonPlugins.reset();
//        try {
//            RxJavaCommonPlugins.clear();
//            for (Method m : RxJavaCommonPlugins.class.getMethods()) {
//                if (m.getName().startsWith("getOn")) {
//                    assertNull(m.toString(), m.invoke(null));
//                }
//            }
//
//        } finally {
//            RxJavaCommonPlugins.reset();
//        }
//
//        for (Method m : RxJavaCommonPlugins.class.getMethods()) {
//            if (m.getName().startsWith("getOn")
//                    && !m.getName().endsWith("Scheduler")
//                    && !m.getName().contains("GenericScheduledExecutorService")) {
//                assertNotNull(m.toString(), m.invoke(null));
//            }
//        }
    }

    @Test
    public void onErrorNoHandler() {
        try {
            final List<Throwable> list = new ArrayList<Throwable>();

            RxJavaCommonPlugins.setErrorHandler(null);

            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    list.add(e);

                }
            });

            RxJavaCommonPlugins.onError(new TestException("Forced failure"));

            Thread.currentThread().setUncaughtExceptionHandler(null);

            // this will be printed on the console and should not crash
            RxJavaCommonPlugins.onError(new TestException("Forced failure 3"));

            assertEquals(1, list.size());
            assertUndeliverableTestException(list, 0, "Forced failure");
        } finally {
            RxJavaCommonPlugins.reset();
            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
    }

    @Test
    public void onErrorCrashes() {
        try {
            final List<Throwable> list = new ArrayList<Throwable>();

            RxJavaCommonPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable t) {
                    throw new TestException("Forced failure 2");
                }
            });

            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    list.add(e);

                }
            });

            RxJavaCommonPlugins.onError(new TestException("Forced failure"));

            assertEquals(2, list.size());
            assertTestException(list, 0, "Forced failure 2");
            assertUndeliverableTestException(list, 1, "Forced failure");

            Thread.currentThread().setUncaughtExceptionHandler(null);

        } finally {
            RxJavaCommonPlugins.reset();
            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
    }

    @Test
    public void onErrorWithNull() {
        try {
            final List<Throwable> list = new ArrayList<Throwable>();

            RxJavaCommonPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable t) {
                    throw new TestException("Forced failure 2");
                }
            });

            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    list.add(e);

                }
            });

            RxJavaCommonPlugins.onError(null);

            assertEquals(2, list.size());
            assertTestException(list, 0, "Forced failure 2");
            assertNPE(list, 1);

            RxJavaCommonPlugins.reset();

            RxJavaCommonPlugins.onError(null);

            assertNPE(list, 2);

        } finally {
            RxJavaCommonPlugins.reset();

            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
    }

    /**
     * Ensure set*() accepts a consumers/functions with wider bounds.
     * @throws Exception on error
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void onErrorWithSuper() throws Exception {
        try {
            Consumer<? super Throwable> errorHandler = new Consumer<Throwable>() {
                @Override
                public void accept(Throwable t) {
                    throw new TestException("Forced failure 2");
                }
            };
            RxJavaCommonPlugins.setErrorHandler(errorHandler);

            Consumer<? super Throwable> errorHandler1 = RxJavaCommonPlugins.getErrorHandler();
            assertSame(errorHandler, errorHandler1);

            Function<? super Scheduler, ? extends Scheduler> scheduler2scheduler = new Function<Scheduler, Scheduler>() {
                @Override
                public Scheduler apply(Scheduler scheduler) throws Exception {
                    return scheduler;
                }
            };
            Function<? super Callable<Scheduler>, ? extends Scheduler> callable2scheduler = new Function<Callable<Scheduler>, Scheduler>() {
                @Override
                public Scheduler apply(Callable<Scheduler> schedulerCallable) throws Exception {
                    return schedulerCallable.call();
                }
            };
            Function<? super Runnable, ? extends Runnable> runnable2runnable = new Function<Runnable, Runnable>() {
                @Override
                public Runnable apply(Runnable runnable) throws Exception {
                    return runnable;
                }
            };

            RxJavaCommonPlugins.setInitComputationSchedulerHandler(callable2scheduler);
            RxJavaCommonPlugins.setComputationSchedulerHandler(scheduler2scheduler);
            RxJavaCommonPlugins.setIoSchedulerHandler(scheduler2scheduler);
            RxJavaCommonPlugins.setNewThreadSchedulerHandler(scheduler2scheduler);
            RxJavaCommonPlugins.setScheduleHandler(runnable2runnable);
            RxJavaCommonPlugins.setSingleSchedulerHandler(scheduler2scheduler);
            RxJavaCommonPlugins.setInitSingleSchedulerHandler(callable2scheduler);
            RxJavaCommonPlugins.setInitNewThreadSchedulerHandler(callable2scheduler);
            RxJavaCommonPlugins.setInitIoSchedulerHandler(callable2scheduler);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    @Test
    public void clearIsPassthrough() {
        try {
            RxJavaCommonPlugins.reset();

            assertNull(RxJavaCommonPlugins.onSchedule(null));

            assertNull(RxJavaCommonPlugins.onSchedule(null));

            Runnable action = Functions.EMPTY_RUNNABLE;

            assertSame(action, RxJavaCommonPlugins.onSchedule(action));

            // These hooks don't exist in 2.0
//            Subscription subscription = Subscriptions.empty();
//
//            assertNull(RxJavaCommonPlugins.onObservableReturn(null));
//
//            assertSame(subscription, RxJavaCommonPlugins.onObservableReturn(subscription));
//
//            assertNull(RxJavaCommonPlugins.onSingleReturn(null));
//
//            assertSame(subscription, RxJavaCommonPlugins.onSingleReturn(subscription));
//
//            TestException ex = new TestException();
//
//            assertNull(RxJavaCommonPlugins.onObservableError(null));
//
//            assertSame(ex, RxJavaCommonPlugins.onObservableError(ex));
//
//            assertNull(RxJavaCommonPlugins.onSingleError(null));
//
//            assertSame(ex, RxJavaCommonPlugins.onSingleError(ex));
//
//            assertNull(RxJavaCommonPlugins.onCompletableError(null));
//
//            assertSame(ex, RxJavaCommonPlugins.onCompletableError(ex));
//
//            Observable.Operator oop = new Observable.Operator() {
//                @Override
//                public Object call(Object t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaCommonPlugins.onObservableLift(null));
//
//            assertSame(oop, RxJavaCommonPlugins.onObservableLift(oop));
//
//            assertNull(RxJavaCommonPlugins.onSingleLift(null));
//
//            assertSame(oop, RxJavaCommonPlugins.onSingleLift(oop));
//
//            Completable.CompletableOperator cop = new Completable.CompletableOperator() {
//                @Override
//                public CompletableSubscriber call(CompletableSubscriber t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaCommonPlugins.onCompletableLift(null));
//
//            assertSame(cop, RxJavaCommonPlugins.onCompletableLift(cop));

            final Scheduler s = ImmediateThinScheduler.INSTANCE;
            Callable<Scheduler> c = new Callable<Scheduler>() {
                @Override
                public Scheduler call() throws Exception {
                    return s;
                }
            };
            assertSame(s, RxJavaCommonPlugins.onComputationScheduler(s));

            assertSame(s, RxJavaCommonPlugins.onIoScheduler(s));

            assertSame(s, RxJavaCommonPlugins.onNewThreadScheduler(s));

            assertSame(s, RxJavaCommonPlugins.onSingleScheduler(s));

            assertSame(s, RxJavaCommonPlugins.initComputationScheduler(c));

            assertSame(s, RxJavaCommonPlugins.initIoScheduler(c));

            assertSame(s, RxJavaCommonPlugins.initNewThreadScheduler(c));

            assertSame(s, RxJavaCommonPlugins.initSingleScheduler(c));

        } finally {
            RxJavaCommonPlugins.reset();
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
//            RxJavaCommonPlugins.setOnObservableSubscribeError(errorHandler);
//
//            RxJavaCommonPlugins.setOnSingleSubscribeError(errorHandler);
//
//            RxJavaCommonPlugins.setOnCompletableSubscribeError(errorHandler);
//
//            assertSame(ex, RxJavaCommonPlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaCommonPlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaCommonPlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaCommonPlugins.reset();
//        }
    }

//    @SuppressWarnings("deprecation")
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXError() {
//        try {
//            RxJavaCommonPlugins.reset();
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
//            RxJavaCommonPlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaCommonPlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaCommonPlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            assertSame(ex, RxJavaCommonPlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaCommonPlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaCommonPlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaCommonPlugins.getInstance().reset();
//            RxJavaCommonPlugins.reset();
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
//            RxJavaCommonPlugins.setOnObservableLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaCommonPlugins.setOnSingleLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaCommonPlugins.setOnCompletableLift(new Function<CompletableOperator, CompletableOperator>() {
//                @Override
//                public CompletableOperator call(CompletableOperator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            assertSame(oop, RxJavaCommonPlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaCommonPlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaCommonPlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaCommonPlugins.reset();
//        }
    }

//    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXLift() {
//        try {
//
//            RxJavaCommonPlugins.getInstance().reset();
//            RxJavaCommonPlugins.reset();
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
//            RxJavaCommonPlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaCommonPlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaCommonPlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public CompletableOperator onLift(CompletableOperator lift) {
//                    return onCompletableLift.call(lift);
//                }
//            });
//
//            assertSame(oop, RxJavaCommonPlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaCommonPlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaCommonPlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaCommonPlugins.reset();
//        }
    }

    @Test
    public void onErrorNull() {
        try {
            final AtomicReference<Throwable> t = new AtomicReference<Throwable>();

            RxJavaCommonPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(final Throwable throwable) throws Exception {
                    t.set(throwable);
                }
            });

            RxJavaCommonPlugins.onError(null);

            final Throwable throwable = t.get();
            assertEquals("onError called with null. Null values are generally not allowed in 2.x operators and sources.", throwable.getMessage());
            assertTrue(throwable instanceof NullPointerException);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    private static void verifyThread(Scheduler scheduler, String expectedThreadName)
            throws AssertionError {
        assertNotNull(scheduler);
        Worker w = scheduler.createWorker();
        try {
            final AtomicReference<Thread> value = new AtomicReference<Thread>();
            final CountDownLatch cdl = new CountDownLatch(1);

            w.schedule(new Runnable() {
                @Override
                public void run() {
                    value.set(Thread.currentThread());
                    cdl.countDown();
                }
            });

            cdl.await();

            Thread t = value.get();
            assertNotNull(t);
            assertTrue(expectedThreadName.equals(t.getName()));
        } catch (Exception e) {
            fail();
        } finally {
            w.dispose();
        }
    }

    @Test
    public void createComputationScheduler() {
        final String name = "ComputationSchedulerTest";
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        };

        final Scheduler customScheduler = RxJavaCommonPlugins.createComputationScheduler(factory);
        RxJavaCommonPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.computation(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void createIoScheduler() {
        final String name = "IoSchedulerTest";
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        };

        final Scheduler customScheduler = RxJavaCommonPlugins.createIoScheduler(factory);
        RxJavaCommonPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.io(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void createNewThreadScheduler() {
        final String name = "NewThreadSchedulerTest";
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        };

        final Scheduler customScheduler = RxJavaCommonPlugins.createNewThreadScheduler(factory);
        RxJavaCommonPlugins.setNewThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.newThread(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void createSingleScheduler() {
        final String name = "SingleSchedulerTest";
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, name);
            }
        };

        final Scheduler customScheduler = RxJavaCommonPlugins.createSingleScheduler(factory);

        RxJavaCommonPlugins.setSingleSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.single(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void onBeforeBlocking() {
        try {
            RxJavaCommonPlugins.setOnBeforeBlocking(new BooleanSupplier() {
                @Override
                public boolean getAsBoolean() throws Exception {
                    throw new IllegalArgumentException();
                }
            });

            try {
                RxJavaCommonPlugins.onBeforeBlocking();
                fail("Should have thrown");
            } catch (IllegalArgumentException ex) {
                // expected
            }
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void isBug() {
        assertFalse(RxJavaCommonPlugins.isBug(new RuntimeException()));
        assertFalse(RxJavaCommonPlugins.isBug(new IOException()));
        assertFalse(RxJavaCommonPlugins.isBug(new InterruptedException()));
        assertFalse(RxJavaCommonPlugins.isBug(new InterruptedIOException()));

        assertTrue(RxJavaCommonPlugins.isBug(new NullPointerException()));
        assertTrue(RxJavaCommonPlugins.isBug(new IllegalArgumentException()));
        assertTrue(RxJavaCommonPlugins.isBug(new IllegalStateException()));
        assertTrue(RxJavaCommonPlugins.isBug(new MissingBackpressureException()));
        assertTrue(RxJavaCommonPlugins.isBug(new ProtocolViolationException("")));
        assertTrue(RxJavaCommonPlugins.isBug(new UndeliverableException(new TestException())));
        assertTrue(RxJavaCommonPlugins.isBug(new CompositeException(new TestException())));
        assertTrue(RxJavaCommonPlugins.isBug(new OnErrorNotImplementedException(new TestException())));
    }
}
