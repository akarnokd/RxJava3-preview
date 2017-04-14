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

public class RxJavaPluginsTest {

    @Test
    public void constructorShouldBePrivate() {
        TestHelper.checkUtilityClass(RxJavaPlugins.class);
    }

    @Test
    @Ignore("Not part of 2.0")
    public void assemblyTrackingObservable() {
//        RxJavaPlugins.enableAssemblyTracking();
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
//            RxJavaPlugins.clearAssemblyTracking();
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
//            RxJavaPlugins.resetAssemblyTracking();
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
//        RxJavaPlugins.enableAssemblyTracking();
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
//            RxJavaPlugins.clearAssemblyTracking();
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
//            RxJavaPlugins.resetAssemblyTracking();
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
//        RxJavaPlugins.enableAssemblyTracking();
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
//            RxJavaPlugins.clearAssemblyTracking();
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
//            RxJavaPlugins.resetAssemblyTracking();
//        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void lockdown() throws Exception {
        RxJavaPlugins.reset();
        RxJavaPlugins.lockdown();
        try {
            assertTrue(RxJavaPlugins.isLockdown());
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

            for (Method m : RxJavaPlugins.class.getMethods()) {
                if (m.getName().startsWith("set")) {

                    Method getter;

                    Class<?> paramType = m.getParameterTypes()[0];

                    if (paramType == Boolean.TYPE) {
                        getter = RxJavaPlugins.class.getMethod("is" + m.getName().substring(3));
                    } else {
                        getter = RxJavaPlugins.class.getMethod("get" + m.getName().substring(3));
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

//            Object o1 = RxJavaPlugins.getOnObservableCreate();
//            Object o2 = RxJavaPlugins.getOnSingleCreate();
//            Object o3 = RxJavaPlugins.getOnCompletableCreate();
//
//            RxJavaPlugins.enableAssemblyTracking();
//            RxJavaPlugins.clearAssemblyTracking();
//            RxJavaPlugins.resetAssemblyTracking();
//
//
//            assertSame(o1, RxJavaPlugins.getOnObservableCreate());
//            assertSame(o2, RxJavaPlugins.getOnSingleCreate());
//            assertSame(o3, RxJavaPlugins.getOnCompletableCreate());

        } finally {
            RxJavaPlugins.unlock();
            RxJavaPlugins.reset();
            assertFalse(RxJavaPlugins.isLockdown());
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
            RxJavaPlugins.setSingleSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.single());
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.single());
    }

    @Test
    public void overrideComputationScheduler() {
        try {
            RxJavaPlugins.setComputationSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.computation());
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.computation());
    }

    @Test
    public void overrideIoScheduler() {
        try {
            RxJavaPlugins.setIoSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.io());
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.io());
    }

    @Test
    public void overrideNewThreadScheduler() {
        try {
            RxJavaPlugins.setNewThreadSchedulerHandler(replaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, Schedulers.newThread());
        } finally {
            RxJavaPlugins.reset();
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
            RxJavaPlugins.setInitSingleSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaPlugins.initSingleScheduler(c));
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaPlugins.initSingleScheduler(c));
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
            RxJavaPlugins.setInitComputationSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaPlugins.initComputationScheduler(c));
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaPlugins.initComputationScheduler(c));
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
            RxJavaPlugins.setInitIoSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaPlugins.initIoScheduler(c));
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaPlugins.initIoScheduler(c));
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
            RxJavaPlugins.setInitNewThreadSchedulerHandler(initReplaceWithImmediate);

            assertSame(ImmediateThinScheduler.INSTANCE, RxJavaPlugins.initNewThreadScheduler(c));
        } finally {
            RxJavaPlugins.reset();
        }
        // make sure the reset worked
        assertSame(s, RxJavaPlugins.initNewThreadScheduler(c));
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
            RxJavaPlugins.initSingleScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaPlugins.initSingleScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    @Test
    public void overrideInitComputationSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaPlugins.initComputationScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaPlugins.initComputationScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    @Test
    public void overrideInitIoSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaPlugins.initIoScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaPlugins.initIoScheduler(nullResultCallable);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            assertEquals("Scheduler Callable result can't be null", npe.getMessage());
        }
    }

    @Test
    public void overrideInitNewThreadSchedulerCrashes() {
        // fail when Callable is null
        try {
            RxJavaPlugins.initNewThreadScheduler(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException npe) {
            // expected
            assertEquals("Scheduler Callable can't be null", npe.getMessage());
        }

        // fail when Callable result is null
        try {
            RxJavaPlugins.initNewThreadScheduler(nullResultCallable);
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
            RxJavaPlugins.setInitSingleSchedulerHandler(initReplaceWithImmediate);
            RxJavaPlugins.initSingleScheduler(unsafeDefault);
        } finally {
            RxJavaPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.single());
    }

    @Test
    public void testDefaultIoSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaPlugins.setInitIoSchedulerHandler(initReplaceWithImmediate);
            RxJavaPlugins.initIoScheduler(unsafeDefault);
        } finally {
            RxJavaPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.io());
    }

    @Test
    public void testDefaultComputationSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaPlugins.setInitComputationSchedulerHandler(initReplaceWithImmediate);
            RxJavaPlugins.initComputationScheduler(unsafeDefault);
        } finally {
            RxJavaPlugins.reset();
        }

        // make sure the reset worked
        assertNotSame(ImmediateThinScheduler.INSTANCE, Schedulers.computation());
    }

    @Test
    public void testDefaultNewThreadSchedulerIsInitializedLazily() {
        // unsafe default Scheduler Callable should not be evaluated
        try {
            RxJavaPlugins.setInitNewThreadSchedulerHandler(initReplaceWithImmediate);
            RxJavaPlugins.initNewThreadScheduler(unsafeDefault);
        } finally {
            RxJavaPlugins.reset();
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
//            RxJavaPlugins.setOnObservableReturn(new Function<Subscription, Subscription>() {
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
//            RxJavaPlugins.reset();
//        }
    }

    @Test
    @Ignore("Different architecture, no longer supported")
    public void singleReturn() {
//        try {
//            final Subscription s = Subscriptions.empty();
//
//            RxJavaPlugins.setOnSingleReturn(new Function<Subscription, Subscription>() {
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
//            RxJavaPlugins.reset();
//        }
    }

    void onSchedule(Worker w) throws InterruptedException {
        try {
            try {
                final AtomicInteger value = new AtomicInteger();
                final CountDownLatch cdl = new CountDownLatch(1);

                RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
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

                RxJavaPlugins.reset();
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

            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable t) {
                    list.add(t);
                }
            });

            RxJavaPlugins.onError(new TestException("Forced failure"));

            assertEquals(1, list.size());
            assertUndeliverableTestException(list, 0, "Forced failure");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    @Ignore("No (need for) clear() method in 2.0")
    public void clear() throws Exception {
//        RxJavaPlugins.reset();
//        try {
//            RxJavaPlugins.clear();
//            for (Method m : RxJavaPlugins.class.getMethods()) {
//                if (m.getName().startsWith("getOn")) {
//                    assertNull(m.toString(), m.invoke(null));
//                }
//            }
//
//        } finally {
//            RxJavaPlugins.reset();
//        }
//
//        for (Method m : RxJavaPlugins.class.getMethods()) {
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

            RxJavaPlugins.setErrorHandler(null);

            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    list.add(e);

                }
            });

            RxJavaPlugins.onError(new TestException("Forced failure"));

            Thread.currentThread().setUncaughtExceptionHandler(null);

            // this will be printed on the console and should not crash
            RxJavaPlugins.onError(new TestException("Forced failure 3"));

            assertEquals(1, list.size());
            assertUndeliverableTestException(list, 0, "Forced failure");
        } finally {
            RxJavaPlugins.reset();
            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
    }

    @Test
    public void onErrorCrashes() {
        try {
            final List<Throwable> list = new ArrayList<Throwable>();

            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
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

            RxJavaPlugins.onError(new TestException("Forced failure"));

            assertEquals(2, list.size());
            assertTestException(list, 0, "Forced failure 2");
            assertUndeliverableTestException(list, 1, "Forced failure");

            Thread.currentThread().setUncaughtExceptionHandler(null);

        } finally {
            RxJavaPlugins.reset();
            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
    }

    @Test
    public void onErrorWithNull() {
        try {
            final List<Throwable> list = new ArrayList<Throwable>();

            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
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

            RxJavaPlugins.onError(null);

            assertEquals(2, list.size());
            assertTestException(list, 0, "Forced failure 2");
            assertNPE(list, 1);

            RxJavaPlugins.reset();

            RxJavaPlugins.onError(null);

            assertNPE(list, 2);

        } finally {
            RxJavaPlugins.reset();

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
            RxJavaPlugins.setErrorHandler(errorHandler);

            Consumer<? super Throwable> errorHandler1 = RxJavaPlugins.getErrorHandler();
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

            RxJavaPlugins.setInitComputationSchedulerHandler(callable2scheduler);
            RxJavaPlugins.setComputationSchedulerHandler(scheduler2scheduler);
            RxJavaPlugins.setIoSchedulerHandler(scheduler2scheduler);
            RxJavaPlugins.setNewThreadSchedulerHandler(scheduler2scheduler);
            RxJavaPlugins.setScheduleHandler(runnable2runnable);
            RxJavaPlugins.setSingleSchedulerHandler(scheduler2scheduler);
            RxJavaPlugins.setInitSingleSchedulerHandler(callable2scheduler);
            RxJavaPlugins.setInitNewThreadSchedulerHandler(callable2scheduler);
            RxJavaPlugins.setInitIoSchedulerHandler(callable2scheduler);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    @Test
    public void clearIsPassthrough() {
        try {
            RxJavaPlugins.reset();

            assertNull(RxJavaPlugins.onSchedule(null));

            assertNull(RxJavaPlugins.onSchedule(null));

            Runnable action = Functions.EMPTY_RUNNABLE;

            assertSame(action, RxJavaPlugins.onSchedule(action));

            // These hooks don't exist in 2.0
//            Subscription subscription = Subscriptions.empty();
//
//            assertNull(RxJavaPlugins.onObservableReturn(null));
//
//            assertSame(subscription, RxJavaPlugins.onObservableReturn(subscription));
//
//            assertNull(RxJavaPlugins.onSingleReturn(null));
//
//            assertSame(subscription, RxJavaPlugins.onSingleReturn(subscription));
//
//            TestException ex = new TestException();
//
//            assertNull(RxJavaPlugins.onObservableError(null));
//
//            assertSame(ex, RxJavaPlugins.onObservableError(ex));
//
//            assertNull(RxJavaPlugins.onSingleError(null));
//
//            assertSame(ex, RxJavaPlugins.onSingleError(ex));
//
//            assertNull(RxJavaPlugins.onCompletableError(null));
//
//            assertSame(ex, RxJavaPlugins.onCompletableError(ex));
//
//            Observable.Operator oop = new Observable.Operator() {
//                @Override
//                public Object call(Object t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaPlugins.onObservableLift(null));
//
//            assertSame(oop, RxJavaPlugins.onObservableLift(oop));
//
//            assertNull(RxJavaPlugins.onSingleLift(null));
//
//            assertSame(oop, RxJavaPlugins.onSingleLift(oop));
//
//            Completable.CompletableOperator cop = new Completable.CompletableOperator() {
//                @Override
//                public CompletableSubscriber call(CompletableSubscriber t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaPlugins.onCompletableLift(null));
//
//            assertSame(cop, RxJavaPlugins.onCompletableLift(cop));

            final Scheduler s = ImmediateThinScheduler.INSTANCE;
            Callable<Scheduler> c = new Callable<Scheduler>() {
                @Override
                public Scheduler call() throws Exception {
                    return s;
                }
            };
            assertSame(s, RxJavaPlugins.onComputationScheduler(s));

            assertSame(s, RxJavaPlugins.onIoScheduler(s));

            assertSame(s, RxJavaPlugins.onNewThreadScheduler(s));

            assertSame(s, RxJavaPlugins.onSingleScheduler(s));

            assertSame(s, RxJavaPlugins.initComputationScheduler(c));

            assertSame(s, RxJavaPlugins.initIoScheduler(c));

            assertSame(s, RxJavaPlugins.initNewThreadScheduler(c));

            assertSame(s, RxJavaPlugins.initSingleScheduler(c));

        } finally {
            RxJavaPlugins.reset();
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
//            RxJavaPlugins.setOnObservableSubscribeError(errorHandler);
//
//            RxJavaPlugins.setOnSingleSubscribeError(errorHandler);
//
//            RxJavaPlugins.setOnCompletableSubscribeError(errorHandler);
//
//            assertSame(ex, RxJavaPlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaPlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaPlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaPlugins.reset();
//        }
    }

//    @SuppressWarnings("deprecation")
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXError() {
//        try {
//            RxJavaPlugins.reset();
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
//            RxJavaPlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaPlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaPlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            assertSame(ex, RxJavaPlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaPlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaPlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaPlugins.getInstance().reset();
//            RxJavaPlugins.reset();
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
//            RxJavaPlugins.setOnObservableLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaPlugins.setOnSingleLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaPlugins.setOnCompletableLift(new Function<CompletableOperator, CompletableOperator>() {
//                @Override
//                public CompletableOperator call(CompletableOperator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            assertSame(oop, RxJavaPlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaPlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaPlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaPlugins.reset();
//        }
    }

//    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXLift() {
//        try {
//
//            RxJavaPlugins.getInstance().reset();
//            RxJavaPlugins.reset();
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
//            RxJavaPlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaPlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaPlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public CompletableOperator onLift(CompletableOperator lift) {
//                    return onCompletableLift.call(lift);
//                }
//            });
//
//            assertSame(oop, RxJavaPlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaPlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaPlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaPlugins.reset();
//        }
    }

    @Test
    public void onErrorNull() {
        try {
            final AtomicReference<Throwable> t = new AtomicReference<Throwable>();

            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(final Throwable throwable) throws Exception {
                    t.set(throwable);
                }
            });

            RxJavaPlugins.onError(null);

            final Throwable throwable = t.get();
            assertEquals("onError called with null. Null values are generally not allowed in 2.x operators and sources.", throwable.getMessage());
            assertTrue(throwable instanceof NullPointerException);
        } finally {
            RxJavaPlugins.reset();
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

        final Scheduler customScheduler = RxJavaPlugins.createComputationScheduler(factory);
        RxJavaPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.computation(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaPlugins.reset();
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

        final Scheduler customScheduler = RxJavaPlugins.createIoScheduler(factory);
        RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.io(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaPlugins.reset();
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

        final Scheduler customScheduler = RxJavaPlugins.createNewThreadScheduler(factory);
        RxJavaPlugins.setNewThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.newThread(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaPlugins.reset();
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

        final Scheduler customScheduler = RxJavaPlugins.createSingleScheduler(factory);

        RxJavaPlugins.setSingleSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(Scheduler scheduler) throws Exception {
                return customScheduler;
            }
        });

        try {
            verifyThread(Schedulers.single(), name);
        } finally {
            customScheduler.shutdown();
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onBeforeBlocking() {
        try {
            RxJavaPlugins.setOnBeforeBlocking(new BooleanSupplier() {
                @Override
                public boolean getAsBoolean() throws Exception {
                    throw new IllegalArgumentException();
                }
            });

            try {
                RxJavaPlugins.onBeforeBlocking();
                fail("Should have thrown");
            } catch (IllegalArgumentException ex) {
                // expected
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void isBug() {
        assertFalse(RxJavaPlugins.isBug(new RuntimeException()));
        assertFalse(RxJavaPlugins.isBug(new IOException()));
        assertFalse(RxJavaPlugins.isBug(new InterruptedException()));
        assertFalse(RxJavaPlugins.isBug(new InterruptedIOException()));

        assertTrue(RxJavaPlugins.isBug(new NullPointerException()));
        assertTrue(RxJavaPlugins.isBug(new IllegalArgumentException()));
        assertTrue(RxJavaPlugins.isBug(new IllegalStateException()));
        assertTrue(RxJavaPlugins.isBug(new MissingBackpressureException()));
        assertTrue(RxJavaPlugins.isBug(new ProtocolViolationException("")));
        assertTrue(RxJavaPlugins.isBug(new UndeliverableException(new TestException())));
        assertTrue(RxJavaPlugins.isBug(new CompositeException(new TestException())));
        assertTrue(RxJavaPlugins.isBug(new OnErrorNotImplementedException(new TestException())));
    }
}
