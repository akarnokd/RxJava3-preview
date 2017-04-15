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

package io.reactivex.interop;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.common.*;
import io.reactivex.common.Scheduler.Worker;
import io.reactivex.common.annotations.*;
import io.reactivex.common.functions.*;
import io.reactivex.flowable.*;
import io.reactivex.interop.internal.operators.SchedulerWhen;
import io.reactivex.observable.*;

/**
 * The base utility class that hosts factory methods and
 * functions to be used with the
 * various base classes' {@code to(Function)} methods to
 * enable interoperation between the base reactive types
 * and some of their features.
 * @since 3.0.0
 */
public final class RxJava3Interop {

    private RxJava3Interop() {
        throw new IllegalStateException("No instances!");
    }

    // --------------------------------------------------------------------------------------------------
    // Base type conversions
    // --------------------------------------------------------------------------------------------------

    public static <T> Flowable<T> toFlowable(ObservableSource<T> source, BackpressureStrategy strategy) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Observable<T>, Flowable<T>> toFlowable(final BackpressureStrategy strategy) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Flowable<T> toFlowable(SingleSource<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Single<T>, Flowable<T>> singleToFlowable() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Flowable<T> toFlowable(MaybeSource<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Maybe<T>, Flowable<T>> maybeToFlowable() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Flowable<T> toFlowable(CompletableSource source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Completable, Flowable<Void>> completableToFlowable() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Observable<T> toObservable(Flowable<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Flowable<T>, Observable<T>> toObservable() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Single<T> toSingle(Flowable<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Flowable<T>, Single<T>> toSingle() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Maybe<T> toMaybe(Flowable<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Flowable<T>, Maybe<T>> toMaybe() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Completable toCompletable(Flowable<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Function<Flowable<T>, Completable> toCompletable() {
        // TODO implement
        throw new UnsupportedOperationException();
    }


    // --------------------------------------------------------------------------------------------------
    // Flowable operators that return a different basetype
    // --------------------------------------------------------------------------------------------------

    public static <T> Single<List<T>> toList(Flowable<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Completable ignoreElements(Flowable<T> source) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Maybe<T> reduce(Flowable<T> source, BiFunction<T, T, T> reducer) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Single<T> reduceWith(Flowable<T> source, Callable<R> seed, BiFunction<R, ? super T, R> reducer) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Single<T> reduce(Flowable<T> source, R seed, BiFunction<R, ? super T, R> reducer) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<T> flatMapSingle(Flowable<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<T> flatMapMaybe(Flowable<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Completable flatMapCompletable(Flowable<T> source, Function<? super T, ? extends CompletableSource> mapper) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Completable concatCompletable(Flowable<? extends CompletableSource> sources) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Completable mergeCompletable(Flowable<? extends CompletableSource> sources) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T> Completable mergeCompletable(Flowable<? extends CompletableSource> sources, int maxConcurrency) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<R> concatSingle(Flowable<? extends Single<? extends R>> sources) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<R> concatMaybe(Flowable<? extends Maybe<? extends R>> sources) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<R> mergeSingle(Flowable<? extends Single<? extends R>> sources) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<R> mergeSingle(Flowable<? extends Single<? extends R>> sources, int maxConcurrency) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<R> mergeMaybe(Flowable<? extends Maybe<? extends R>> sources) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    public static <T, R> Flowable<R> mergeMaybe(Flowable<? extends Maybe<? extends R>> sources, int maxConcurrency) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    /**
     * Allows the use of operators for controlling the timing around when
     * actions scheduled on workers are actually done. This makes it possible to
     * layer additional behavior on this {@link Scheduler}. The only parameter
     * is a function that flattens an {@link Flowable} of {@link Flowable}
     * of {@link Completable}s into just one {@link Completable}. There must be
     * a chain of operators connecting the returned value to the source
     * {@link Flowable} otherwise any work scheduled on the returned
     * {@link Scheduler} will not be executed.
     * <p>
     * When {@link Scheduler#createWorker()} is invoked a {@link Flowable} of
     * {@link Completable}s is onNext'd to the combinator to be flattened. If
     * the inner {@link Flowable} is not immediately subscribed to an calls to
     * {@link Worker#schedule} are buffered. Once the {@link Flowable} is
     * subscribed to actions are then onNext'd as {@link Completable}s.
     * <p>
     * Finally the actions scheduled on the parent {@link Scheduler} when the
     * inner most {@link Completable}s are subscribed to.
     * <p>
     * When the {@link Worker} is unsubscribed the {@link Completable} emits an
     * onComplete and triggers any behavior in the flattening operator. The
     * {@link Flowable} and all {@link Completable}s give to the flattening
     * function never onError.
     * <p>
     * Limit the amount concurrency two at a time without creating a new fix
     * size thread pool:
     * 
     * <pre>
     * Scheduler limitScheduler = Schedulers.computation().when(workers -> {
     *  // use merge max concurrent to limit the number of concurrent
     *  // callbacks two at a time
     *  return Completable.merge(Flowable.merge(workers), 2);
     * });
     * </pre>
     * <p>
     * This is a slightly different way to limit the concurrency but it has some
     * interesting benefits and drawbacks to the method above. It works by
     * limited the number of concurrent {@link Worker}s rather than individual
     * actions. Generally each {@link Flowable} uses its own {@link Worker}.
     * This means that this will essentially limit the number of concurrent
     * subscribes. The danger comes from using operators like
     * {@link Flowable#zip(org.reactivestreams.Publisher, org.reactivestreams.Publisher, io.reactivex.common.functions.BiFunction)} where
     * subscribing to the first {@link Flowable} could deadlock the
     * subscription to the second.
     * 
     * <pre>
     * Scheduler limitScheduler = Schedulers.computation().when(workers -> {
     *  // use merge max concurrent to limit the number of concurrent
     *  // Flowables two at a time
     *  return Completable.merge(Flowable.merge(workers, 2));
     * });
     * </pre>
     * 
     * Slowing down the rate to no more than than 1 a second. This suffers from
     * the same problem as the one above I could find an {@link Flowable}
     * operator that limits the rate without dropping the values (aka leaky
     * bucket algorithm).
     * 
     * <pre>
     * Scheduler slowScheduler = Schedulers.computation().when(workers -> {
     *  // use concatenate to make each worker happen one at a time.
     *  return Completable.concat(workers.map(actions -> {
     *      // delay the starting of the next worker by 1 second.
     *      return Completable.merge(actions.delaySubscription(1, TimeUnit.SECONDS));
     *  }));
     * });
     * </pre>
     * 
     * @param <S> a Scheduler and a Subscription
     * @param scheduler the target scheduler to wrap
     * @param combine the function that takes a two-level nested Flowable sequence of a Completable and returns
     * the Completable that will be subscribed to and should trigger the execution of the scheduled Actions.
     * @return the Scheduler with the customized execution behavior
     */
    @SuppressWarnings("unchecked")
    @Experimental
    @NonNull
    public static <S extends Scheduler & Disposable> S when(Scheduler scheduler, @NonNull Function<Flowable<Flowable<Completable>>, Completable> combine) {
        return (S) new SchedulerWhen(combine, scheduler);
    }


}
