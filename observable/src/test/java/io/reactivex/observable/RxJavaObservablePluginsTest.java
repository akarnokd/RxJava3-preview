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

package io.reactivex.observable;

import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.*;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.Functions;
import io.reactivex.observable.internal.operators.*;

public class RxJavaObservablePluginsTest {

//    static Observable<Integer> createObservable() {
//        return Observable.range(1, 5).map(new Function<Integer, Integer>() {
//            @Override
//            public Integer apply(Integer t) {
//                throw new TestException();
//            }
//        });
//    }
//
//    static Flowable<Integer> createFlowable() {
//        return Flowable.range(1, 5).map(new Function<Integer, Integer>() {
//            @Override
//            public Integer apply(Integer t) {
//                throw new TestException();
//            }
//        });
//    }

    @Test
    public void constructorShouldBePrivate() {
        TestCommonHelper.checkUtilityClass(RxJavaObservablePlugins.class);
    }

    @Test
    @Ignore("Not part of 2.0")
    public void assemblyTrackingObservable() {
//        RxJavaObservablePlugins.enableAssemblyTracking();
//        try {
//            TestObserver<Integer> ts = TestObserver.create();
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
//            RxJavaObservablePlugins.clearAssemblyTracking();
//
//            ts = TestObserver.create();
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
//            RxJavaObservablePlugins.resetAssemblyTracking();
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
//        RxJavaObservablePlugins.enableAssemblyTracking();
//        try {
//            TestObserver<Integer> ts = TestObserver.create();
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
//            RxJavaObservablePlugins.clearAssemblyTracking();
//
//            ts = TestObserver.create();
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
//            RxJavaObservablePlugins.resetAssemblyTracking();
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
//        RxJavaObservablePlugins.enableAssemblyTracking();
//        try {
//            TestObserver<Integer> ts = TestObserver.create();
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
//            RxJavaObservablePlugins.clearAssemblyTracking();
//
//            ts = TestObserver.create();
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
//            RxJavaObservablePlugins.resetAssemblyTracking();
//        }
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void lockdown() throws Exception {
        RxJavaObservablePlugins.reset();
        RxJavaObservablePlugins.lockdown();
        try {
            assertTrue(RxJavaObservablePlugins.isLockdown());
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

            for (Method m : RxJavaObservablePlugins.class.getMethods()) {
                if (m.getName().startsWith("set")) {

                    Method getter;

                    Class<?> paramType = m.getParameterTypes()[0];

                    if (paramType == Boolean.TYPE) {
                        getter = RxJavaObservablePlugins.class.getMethod("is" + m.getName().substring(3));
                    } else {
                        getter = RxJavaObservablePlugins.class.getMethod("get" + m.getName().substring(3));
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

//            Object o1 = RxJavaObservablePlugins.getOnObservableCreate();
//            Object o2 = RxJavaObservablePlugins.getOnSingleCreate();
//            Object o3 = RxJavaObservablePlugins.getOnCompletableCreate();
//
//            RxJavaObservablePlugins.enableAssemblyTracking();
//            RxJavaObservablePlugins.clearAssemblyTracking();
//            RxJavaObservablePlugins.resetAssemblyTracking();
//
//
//            assertSame(o1, RxJavaObservablePlugins.getOnObservableCreate());
//            assertSame(o2, RxJavaObservablePlugins.getOnSingleCreate());
//            assertSame(o3, RxJavaObservablePlugins.getOnCompletableCreate());

        } finally {
            RxJavaObservablePlugins.unlock();
            RxJavaObservablePlugins.reset();
            assertFalse(RxJavaObservablePlugins.isLockdown());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void observableCreate() {
        try {
            RxJavaObservablePlugins.setOnObservableAssembly(new Function<Observable, Observable>() {
                @Override
                public Observable apply(Observable t) {
                    return new ObservableRange(1, 2);
                }
            });

            Observable.range(10, 3)
            .test()
            .assertValues(1, 2)
            .assertNoErrors()
            .assertComplete();
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked
        Observable.range(10, 3)
        .test()
        .assertValues(10, 11, 12)
        .assertNoErrors()
        .assertComplete();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void observableStart() {
        try {
            RxJavaObservablePlugins.setOnObservableSubscribe(new BiFunction<Observable, Observer, Observer>() {
                @Override
                public Observer apply(Observable o, final Observer t) {
                    return new Observer() {

                        @Override
                        public void onSubscribe(Disposable d) {
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

            Observable.range(10, 3)
            .test()
            .assertValues(1, 2, 3)
            .assertNoErrors()
            .assertComplete();
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked
        Observable.range(10, 3)
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
//            RxJavaObservablePlugins.setOnObservableReturn(new Function<Subscription, Subscription>() {
//                @Override
//                public Subscription call(Subscription t) {
//                    return s;
//                }
//            });
//
//            TestObserver<Integer> ts = TestObserver.create();
//
//            Subscription u = Observable.range(10, 3).subscribe(ts);
//
//            ts.assertValues(10, 11, 12);
//            ts.assertNoErrors();
//            ts.assertComplete();
//
//            assertSame(s, u);
//        } finally {
//            RxJavaObservablePlugins.reset();
//        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void singleCreate() {
        try {
            RxJavaObservablePlugins.setOnSingleAssembly(new Function<Single, Single>() {
                @Override
                public Single apply(Single t) {
                    return new SingleJust<Integer>(10);
                }
            });

            Single.just(1)
            .test()
            .assertValue(10)
            .assertNoErrors()
            .assertComplete();
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked
        Single.just(1)
        .test()
        .assertValue(1)
        .assertNoErrors()
        .assertComplete();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void singleStart() {
        try {
            RxJavaObservablePlugins.setOnSingleSubscribe(new BiFunction<Single, SingleObserver, SingleObserver>() {
                @Override
                public SingleObserver apply(Single o, final SingleObserver t) {
                    return new SingleObserver<Object>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            t.onSubscribe(d);
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void onSuccess(Object value) {
                            t.onSuccess(10);
                        }

                        @Override
                        public void onError(Throwable e) {
                            t.onError(e);
                        }

                    };
                }
            });

            Single.just(1)
            .test()
            .assertValue(10)
            .assertNoErrors()
            .assertComplete();
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked
        Single.just(1)
        .test()
        .assertValue(1)
        .assertNoErrors()
        .assertComplete();
    }

    @Test
    @Ignore("Different architecture, no longer supported")
    public void singleReturn() {
//        try {
//            final Subscription s = Subscriptions.empty();
//
//            RxJavaObservablePlugins.setOnSingleReturn(new Function<Subscription, Subscription>() {
//                @Override
//                public Subscription call(Subscription t) {
//                    return s;
//                }
//            });
//
//            TestObserver<Integer> ts = TestObserver.create();
//
//            Subscription u = Single.just(1).subscribe(ts);
//
//            ts.assertValue(1);
//            ts.assertNoErrors();
//            ts.assertComplete();
//
//            assertSame(s, u);
//        } finally {
//            RxJavaObservablePlugins.reset();
//        }
    }

    @Test
    public void completableCreate() {
        try {
            RxJavaObservablePlugins.setOnCompletableAssembly(new Function<Completable, Completable>() {
                @Override
                public Completable apply(Completable t) {
                    return new CompletableError(new TestException());
                }
            });

            Completable.complete()
            .test()
            .assertNoValues()
            .assertNotComplete()
            .assertError(TestException.class);
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked
        Completable.complete()
        .test()
        .assertNoValues()
        .assertNoErrors()
        .assertComplete();
    }

    @Test
    public void completableStart() {
        try {
            RxJavaObservablePlugins.setOnCompletableSubscribe(new BiFunction<Completable, CompletableObserver, CompletableObserver>() {
                @Override
                public CompletableObserver apply(Completable o, final CompletableObserver t) {
                    return new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            t.onSubscribe(d);
                        }

                        @Override
                        public void onError(Throwable e) {
                            t.onError(e);
                        }

                        @Override
                        public void onComplete() {
                            t.onError(new TestException());
                        }
                    };
                }
            });

            Completable.complete()
            .test()
            .assertNoValues()
            .assertNotComplete()
            .assertError(TestException.class);
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked

        Completable.complete()
        .test()
        .assertNoValues()
        .assertNoErrors()
        .assertComplete();
    }

    @Test
    @Ignore("No (need for) clear() method in 2.0")
    public void clear() throws Exception {
//        RxJavaObservablePlugins.reset();
//        try {
//            RxJavaObservablePlugins.clear();
//            for (Method m : RxJavaObservablePlugins.class.getMethods()) {
//                if (m.getName().startsWith("getOn")) {
//                    assertNull(m.toString(), m.invoke(null));
//                }
//            }
//
//        } finally {
//            RxJavaObservablePlugins.reset();
//        }
//
//        for (Method m : RxJavaObservablePlugins.class.getMethods()) {
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
            Function<? super ConnectableObservable, ? extends ConnectableObservable> connectableObservable2ConnectableObservable = new Function<ConnectableObservable, ConnectableObservable>() {
                @Override
                public ConnectableObservable apply(ConnectableObservable connectableObservable) throws Exception {
                    return connectableObservable;
                }
            };
            Function<Maybe, Maybe> maybe2maybe = new Function<Maybe, Maybe>() {
                @Override
                public Maybe apply(Maybe maybe) throws Exception {
                    return maybe;
                }
            };
            BiFunction<Maybe, MaybeObserver, MaybeObserver> maybe2observer = new BiFunction<Maybe, MaybeObserver, MaybeObserver>() {
                @Override
                public MaybeObserver apply(Maybe maybe, MaybeObserver maybeObserver) throws Exception {
                    return maybeObserver;
                }
            };
            Function<Observable, Observable> observable2observable = new Function<Observable, Observable>() {
                @Override
                public Observable apply(Observable observable) throws Exception {
                    return observable;
                }
            };
            BiFunction<? super Observable, ? super Observer, ? extends Observer> observable2observer = new BiFunction<Observable, Observer, Observer>() {
                @Override
                public Observer apply(Observable observable, Observer observer) throws Exception {
                    return observer;
                }
            };
            Function<Single, Single> single2single = new Function<Single, Single>() {
                @Override
                public Single apply(Single single) throws Exception {
                    return single;
                }
            };
            BiFunction<? super Single, ? super SingleObserver, ? extends SingleObserver> single2observer = new BiFunction<Single, SingleObserver, SingleObserver>() {
                @Override
                public SingleObserver apply(Single single, SingleObserver singleObserver) throws Exception {
                    return singleObserver;
                }
            };
            BiFunction<? super Completable, ? super CompletableObserver, ? extends CompletableObserver> completableObserver2completableObserver = new BiFunction<Completable, CompletableObserver, CompletableObserver>() {
                @Override
                public CompletableObserver apply(Completable completable, CompletableObserver completableObserver) throws Exception {
                    return completableObserver;
                }
            };
            Function<? super Completable, ? extends Completable> completable2completable = new Function<Completable, Completable>() {
                @Override
                public Completable apply(Completable completable) throws Exception {
                    return completable;
                }
            };


            RxJavaObservablePlugins.setOnConnectableObservableAssembly(connectableObservable2ConnectableObservable);
            RxJavaObservablePlugins.setOnMaybeAssembly(maybe2maybe);
            RxJavaObservablePlugins.setOnMaybeSubscribe(maybe2observer);
            RxJavaObservablePlugins.setOnObservableAssembly(observable2observable);
            RxJavaObservablePlugins.setOnObservableSubscribe(observable2observer);
            RxJavaObservablePlugins.setOnSingleAssembly(single2single);
            RxJavaObservablePlugins.setOnSingleSubscribe(single2observer);
            RxJavaObservablePlugins.setOnCompletableSubscribe(completableObserver2completableObserver);
            RxJavaObservablePlugins.setOnCompletableAssembly(completable2completable);
        } finally {
            RxJavaObservablePlugins.reset();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    @Test
    public void clearIsPassthrough() {
        try {
            RxJavaObservablePlugins.reset();

            assertNull(RxJavaObservablePlugins.onAssembly((Observable)null));

            assertNull(RxJavaObservablePlugins.onAssembly((ConnectableObservable)null));

            Observable oos = new Observable() {
                @Override
                public void subscribeActual(Observer t) {

                }
            };

            assertSame(oos, RxJavaObservablePlugins.onAssembly(oos));

            assertNull(RxJavaObservablePlugins.onAssembly((Single)null));

            Single sos = new Single() {
                @Override
                public void subscribeActual(SingleObserver t) {

                }
            };

            assertSame(sos, RxJavaObservablePlugins.onAssembly(sos));

            assertNull(RxJavaObservablePlugins.onAssembly((Completable)null));

            Completable cos = new Completable() {
                @Override
                public void subscribeActual(CompletableObserver t) {

                }
            };

            assertSame(cos, RxJavaObservablePlugins.onAssembly(cos));

            assertNull(RxJavaObservablePlugins.onAssembly((Maybe)null));

            Maybe myb = new Maybe() {
                @Override
                public void subscribeActual(MaybeObserver t) {

                }
            };

            assertSame(myb, RxJavaObservablePlugins.onAssembly(myb));


            class AllSubscriber implements Observer, SingleObserver, CompletableObserver, MaybeObserver {

                @Override
                public void onSuccess(Object value) {

                }

                @Override
                public void onSubscribe(Disposable d) {

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

            assertNull(RxJavaObservablePlugins.onSubscribe(Observable.never(), null));

            assertSame(all, RxJavaObservablePlugins.onSubscribe(Observable.never(), all));

            assertNull(RxJavaObservablePlugins.onSubscribe(Single.just(1), null));

            assertSame(all, RxJavaObservablePlugins.onSubscribe(Single.just(1), all));

            assertNull(RxJavaObservablePlugins.onSubscribe(Completable.never(), null));

            assertSame(all, RxJavaObservablePlugins.onSubscribe(Completable.never(), all));

            assertNull(RxJavaObservablePlugins.onSubscribe(Maybe.never(), null));

            assertSame(all, RxJavaObservablePlugins.onSubscribe(Maybe.never(), all));

            // These hooks don't exist in 2.0
//            Subscription subscription = Subscriptions.empty();
//
//            assertNull(RxJavaObservablePlugins.onObservableReturn(null));
//
//            assertSame(subscription, RxJavaObservablePlugins.onObservableReturn(subscription));
//
//            assertNull(RxJavaObservablePlugins.onSingleReturn(null));
//
//            assertSame(subscription, RxJavaObservablePlugins.onSingleReturn(subscription));
//
//            TestException ex = new TestException();
//
//            assertNull(RxJavaObservablePlugins.onObservableError(null));
//
//            assertSame(ex, RxJavaObservablePlugins.onObservableError(ex));
//
//            assertNull(RxJavaObservablePlugins.onSingleError(null));
//
//            assertSame(ex, RxJavaObservablePlugins.onSingleError(ex));
//
//            assertNull(RxJavaObservablePlugins.onCompletableError(null));
//
//            assertSame(ex, RxJavaObservablePlugins.onCompletableError(ex));
//
//            Observable.Operator oop = new Observable.Operator() {
//                @Override
//                public Object call(Object t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaObservablePlugins.onObservableLift(null));
//
//            assertSame(oop, RxJavaObservablePlugins.onObservableLift(oop));
//
//            assertNull(RxJavaObservablePlugins.onSingleLift(null));
//
//            assertSame(oop, RxJavaObservablePlugins.onSingleLift(oop));
//
//            Completable.CompletableOperator cop = new Completable.CompletableOperator() {
//                @Override
//                public CompletableSubscriber call(CompletableSubscriber t) {
//                    return t;
//                }
//            };
//
//            assertNull(RxJavaObservablePlugins.onCompletableLift(null));
//
//            assertSame(cop, RxJavaObservablePlugins.onCompletableLift(cop));
        } finally {
            RxJavaObservablePlugins.reset();
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
//            RxJavaObservablePlugins.setOnObservableSubscribeError(errorHandler);
//
//            RxJavaObservablePlugins.setOnSingleSubscribeError(errorHandler);
//
//            RxJavaObservablePlugins.setOnCompletableSubscribeError(errorHandler);
//
//            assertSame(ex, RxJavaObservablePlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaObservablePlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaObservablePlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaObservablePlugins.reset();
//        }
    }

//    @SuppressWarnings("deprecation")
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXError() {
//        try {
//            RxJavaObservablePlugins.reset();
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
//            RxJavaObservablePlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaObservablePlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T> Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            RxJavaObservablePlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public Throwable onSubscribeError(Throwable e) {
//                    return errorHandler.call(e);
//                }
//            });
//
//            assertSame(ex, RxJavaObservablePlugins.onObservableError(new TestException("Forced failure 1")));
//
//            assertSame(ex, RxJavaObservablePlugins.onSingleError(new TestException("Forced failure 2")));
//
//            assertSame(ex, RxJavaObservablePlugins.onCompletableError(new TestException("Forced failure 3")));
//
//            assertTestException(list, 0, "Forced failure 1");
//
//            assertTestException(list, 1, "Forced failure 2");
//
//            assertTestException(list, 2, "Forced failure 3");
//        } finally {
//            RxJavaObservablePlugins.getInstance().reset();
//            RxJavaObservablePlugins.reset();
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
//            RxJavaObservablePlugins.setOnObservableLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaObservablePlugins.setOnSingleLift(new Function<Operator, Operator>() {
//                @Override
//                public Operator call(Operator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            RxJavaObservablePlugins.setOnCompletableLift(new Function<CompletableOperator, CompletableOperator>() {
//                @Override
//                public CompletableOperator call(CompletableOperator t) {
//                    counter[0]++;
//                    return t;
//                }
//            });
//
//            assertSame(oop, RxJavaObservablePlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaObservablePlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaObservablePlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaObservablePlugins.reset();
//        }
    }

//    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    @Test
    @Ignore("Not present in 2.0")
    public void onPluginsXLift() {
//        try {
//
//            RxJavaObservablePlugins.getInstance().reset();
//            RxJavaObservablePlugins.reset();
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
//            RxJavaObservablePlugins.getInstance().registerObservableExecutionHook(new RxJavaObservableExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaObservablePlugins.getInstance().registerSingleExecutionHook(new RxJavaSingleExecutionHook() {
//                @Override
//                public <T, R> Operator<? extends R, ? super T> onLift(Operator<? extends R, ? super T> lift) {
//                    return onObservableLift.call(lift);
//                }
//            });
//
//            RxJavaObservablePlugins.getInstance().registerCompletableExecutionHook(new RxJavaCompletableExecutionHook() {
//                @Override
//                public CompletableOperator onLift(CompletableOperator lift) {
//                    return onCompletableLift.call(lift);
//                }
//            });
//
//            assertSame(oop, RxJavaObservablePlugins.onObservableLift(oop));
//
//            assertSame(oop, RxJavaObservablePlugins.onSingleLift(oop));
//
//            assertSame(cop, RxJavaObservablePlugins.onCompletableLift(cop));
//
//            assertEquals(3, counter[0]);
//
//        } finally {
//            RxJavaObservablePlugins.reset();
//        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void overrideConnectableObservable() {
        try {
            RxJavaObservablePlugins.setOnConnectableObservableAssembly(new Function<ConnectableObservable, ConnectableObservable>() {
                @Override
                public ConnectableObservable apply(ConnectableObservable co) throws Exception {
                    return new ConnectableObservable() {

                        @Override
                        public void connect(Consumer connection) {

                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        protected void subscribeActual(Observer observer) {
                            observer.onSubscribe(Disposables.empty());
                            observer.onNext(10);
                            observer.onComplete();
                        }
                    };
                }
            });

            Observable
            .just(1)
            .publish()
            .autoConnect()
            .test()
            .assertResult(10);

        } finally {
            RxJavaObservablePlugins.reset();
        }

        Observable
        .just(1)
        .publish()
        .autoConnect()
        .test()
        .assertResult(1);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void maybeCreate() {
        try {
            RxJavaObservablePlugins.setOnMaybeAssembly(new Function<Maybe, Maybe>() {
                @Override
                public Maybe apply(Maybe t) {
                    return new MaybeError(new TestException());
                }
            });

            Maybe.empty()
            .test()
            .assertNoValues()
            .assertNotComplete()
            .assertError(TestException.class);
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked
        Maybe.empty()
        .test()
        .assertNoValues()
        .assertNoErrors()
        .assertComplete();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void maybeStart() {
        try {
            RxJavaObservablePlugins.setOnMaybeSubscribe(new BiFunction<Maybe, MaybeObserver, MaybeObserver>() {
                @Override
                public MaybeObserver apply(Maybe o, final MaybeObserver t) {
                    return new MaybeObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            t.onSubscribe(d);
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void onSuccess(Object value) {
                            t.onSuccess(value);
                        }

                        @Override
                        public void onError(Throwable e) {
                            t.onError(e);
                        }

                        @Override
                        public void onComplete() {
                            t.onError(new TestException());
                        }
                    };
                }
            });

            Maybe.empty()
            .test()
            .assertNoValues()
            .assertNotComplete()
            .assertError(TestException.class);
        } finally {
            RxJavaObservablePlugins.reset();
        }
        // make sure the reset worked

        Maybe.empty()
        .test()
        .assertNoValues()
        .assertNoErrors()
        .assertComplete();
    }

}
