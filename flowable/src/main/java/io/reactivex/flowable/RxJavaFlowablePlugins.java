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

import java.util.concurrent.*;

import org.reactivestreams.Subscriber;

import io.reactivex.common.*;
import io.reactivex.common.annotations.*;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.common.internal.schedulers.*;
import io.reactivex.common.internal.utils.ExceptionHelper;
/**
 * Utility class to inject handlers to certain standard RxJava operations.
 */
public final class RxJavaFlowablePlugins {

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Flowable, ? extends Flowable> onFlowableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super ConnectableFlowable, ? extends ConnectableFlowable> onConnectableFlowableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super ParallelFlowable, ? extends ParallelFlowable> onParallelAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super Flowable, ? super Subscriber, ? extends Subscriber> onFlowableSubscribe;

    /** Prevents changing the plugins. */
    static volatile boolean lockdown;

    /**
     * Prevents changing the plugins from then on.
     * <p>This allows container-like environments to prevent clients
     * messing with plugins.
     */
    public static void lockdown() {
        lockdown = true;
    }

    /**
     * Returns true if the plugins were locked down.
     * @return true if the plugins were locked down
     */
    public static boolean isLockdown() {
        return lockdown;
    }

    /**
     * Removes all handlers and resets to default behavior.
     */
    public static void reset() {
        setOnFlowableAssembly(null);
        setOnFlowableSubscribe(null);

        setOnConnectableFlowableAssembly(null);

        setOnParallelAssembly(null);
    }

    /**
     * Revokes the lockdown, only for testing purposes.
     */
    /* test. */static void unlock() {
        lockdown = false;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public static Function<? super Flowable, ? extends Flowable> getOnFlowableAssembly() {
        return onFlowableAssembly;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public static Function<? super ConnectableFlowable, ? extends ConnectableFlowable> getOnConnectableFlowableAssembly() {
        return onConnectableFlowableAssembly;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    public static BiFunction<? super Flowable, ? super Subscriber, ? extends Subscriber> getOnFlowableSubscribe() {
        return onFlowableSubscribe;
    }

    /**
     * Sets the specific hook function.
     * @param onFlowableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    public static void setOnFlowableAssembly(@Nullable Function<? super Flowable, ? extends Flowable> onFlowableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaFlowablePlugins.onFlowableAssembly = onFlowableAssembly;
    }

    /**
     * Sets the specific hook function.
     * @param onConnectableFlowableAssembly the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    public static void setOnConnectableFlowableAssembly(@Nullable Function<? super ConnectableFlowable, ? extends ConnectableFlowable> onConnectableFlowableAssembly) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaFlowablePlugins.onConnectableFlowableAssembly = onConnectableFlowableAssembly;
    }

    /**
     * Sets the specific hook function.
     * @param onFlowableSubscribe the hook function to set, null allowed
     */
    @SuppressWarnings("rawtypes")
    public static void setOnFlowableSubscribe(@Nullable BiFunction<? super Flowable, ? super Subscriber, ? extends Subscriber> onFlowableSubscribe) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        RxJavaFlowablePlugins.onFlowableSubscribe = onFlowableSubscribe;
    }

    /**
     * Calls the associated hook function.
     * @param <T> the value type
     * @param source the hook's input value
     * @param subscriber the subscriber
     * @return the value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <T> Subscriber<? super T> onSubscribe(@NonNull Flowable<T> source, @NonNull Subscriber<? super T> subscriber) {
        BiFunction<? super Flowable, ? super Subscriber, ? extends Subscriber> f = onFlowableSubscribe;
        if (f != null) {
            return apply(f, source, subscriber);
        }
        return subscriber;
    }

    /**
     * Calls the associated hook function.
     * @param <T> the value type
     * @param source the hook's input value
     * @return the value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <T> Flowable<T> onAssembly(@NonNull Flowable<T> source) {
        Function<? super Flowable, ? extends Flowable> f = onFlowableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * Calls the associated hook function.
     * @param <T> the value type
     * @param source the hook's input value
     * @return the value returned by the hook
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <T> ConnectableFlowable<T> onAssembly(@NonNull ConnectableFlowable<T> source) {
        Function<? super ConnectableFlowable, ? extends ConnectableFlowable> f = onConnectableFlowableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * Sets the specific hook function.
     * @param handler the hook function to set, null allowed
     * @since 2.0.6 - experimental
     */
    @Experimental
    @SuppressWarnings("rawtypes")
    public static void setOnParallelAssembly(@Nullable Function<? super ParallelFlowable, ? extends ParallelFlowable> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onParallelAssembly = handler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     * @since 2.0.6 - experimental
     */
    @Experimental
    @SuppressWarnings("rawtypes")
    @Nullable
    public static Function<? super ParallelFlowable, ? extends ParallelFlowable> getOnParallelAssembly() {
        return onParallelAssembly;
    }

    /**
     * Calls the associated hook function.
     * @param <T> the value type of the source
     * @param source the hook's input value
     * @return the value returned by the hook
     * @since 2.0.6 - experimental
     */
    @Experimental
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @NonNull
    public static <T> ParallelFlowable<T> onAssembly(@NonNull ParallelFlowable<T> source) {
        Function<? super ParallelFlowable, ? extends ParallelFlowable> f = onParallelAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#computation()}
     * except using {@code threadFactory} for thread creation.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return the created Scheduler instance
     * @since 2.0.5 - experimental
     */
    @Experimental
    @NonNull
    public static Scheduler createComputationScheduler(@NonNull ThreadFactory threadFactory) {
        return new ComputationScheduler(ObjectHelper.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#io()}
     * except using {@code threadFactory} for thread creation.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return the created Scheduler instance
     * @since 2.0.5 - experimental
     */
    @Experimental
    @NonNull
    public static Scheduler createIoScheduler(@NonNull ThreadFactory threadFactory) {
        return new IoScheduler(ObjectHelper.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#newThread()}
     * except using {@code threadFactory} for thread creation.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return the created Scheduler instance
     * @since 2.0.5 - experimental
     */
    @Experimental
    @NonNull
    public static Scheduler createNewThreadScheduler(@NonNull ThreadFactory threadFactory) {
        return new NewThreadScheduler(ObjectHelper.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#single()}
     * except using {@code threadFactory} for thread creation.
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return the created Scheduler instance
     * @since 2.0.5 - experimental
     */
    @Experimental
    @NonNull
    public static Scheduler createSingleScheduler(@NonNull ThreadFactory threadFactory) {
        return new SingleScheduler(ObjectHelper.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
     * Wraps the call to the function in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     * @param <T> the input type
     * @param <R> the output type
     * @param f the function to call, not null (not verified)
     * @param t the parameter value to the function
     * @return the result of the function call
     */
    @NonNull
    static <T, R> R apply(@NonNull Function<T, R> f, @NonNull T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the function in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     * @param <T> the first input type
     * @param <U> the second input type
     * @param <R> the output type
     * @param f the function to call, not null (not verified)
     * @param t the first parameter value to the function
     * @param u the second parameter value to the function
     * @return the result of the function call
     */
    @NonNull
    static <T, U, R> R apply(@NonNull BiFunction<T, U, R> f, @NonNull T t, @NonNull U u) {
        try {
            return f.apply(t, u);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the Scheduler creation callable in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     * @param s the {@link Callable} which returns a {@link Scheduler}, not null (not verified). Cannot return null
     * @return the result of the callable call, not null
     * @throws NullPointerException if the callable parameter returns null
     */
    @NonNull
    static Scheduler callRequireNonNull(@NonNull Callable<Scheduler> s) {
        try {
            return ObjectHelper.requireNonNull(s.call(), "Scheduler Callable result can't be null");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the Scheduler creation function in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     * @param f the function to call, not null (not verified). Cannot return null
     * @param s the parameter value to the function
     * @return the result of the function call, not null
     * @throws NullPointerException if the function parameter returns null
     */
    @NonNull
    static Scheduler applyRequireNonNull(@NonNull Function<? super Callable<Scheduler>, ? extends Scheduler> f, Callable<Scheduler> s) {
        return ObjectHelper.requireNonNull(apply(f, s), "Scheduler Callable result can't be null");
    }

    /** Helper class, no instances. */
    private RxJavaFlowablePlugins() {
        throw new IllegalStateException("No instances!");
    }
}
