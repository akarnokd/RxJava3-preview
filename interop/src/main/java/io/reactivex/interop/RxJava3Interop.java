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

import java.util.*;
import java.util.concurrent.Callable;

import org.reactivestreams.Publisher;

import io.reactivex.common.*;
import io.reactivex.common.Scheduler.Worker;
import io.reactivex.common.annotations.*;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.*;
import io.reactivex.common.internal.utils.*;
import io.reactivex.flowable.*;
import io.reactivex.flowable.internal.operators.*;
import io.reactivex.interop.internal.operators.*;
import io.reactivex.observable.*;
import io.reactivex.observable.Observable;

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
        ObjectHelper.requireNonNull(source, "source is null");
        Flowable<T> flowable = new FlowableFromObservable<T>(source);
        switch (strategy) {
        case BUFFER:
            flowable = flowable.onBackpressureBuffer();
            break;
        case DROP:
            flowable = flowable.onBackpressureDrop();
            break;
        case ERROR:
            flowable = new FlowableOnBackpressureError<T>(flowable);
            break;
        case LATEST:
            flowable = flowable.onBackpressureLatest();
            break;
        default:
        }
        return flowable;
    }

    public static <T> Flowable<T> toFlowable(SingleSource<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaFlowablePlugins.onAssembly(new SingleToFlowable<T>(source));
    }

    public static <T> Flowable<T> toFlowable(MaybeSource<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaFlowablePlugins.onAssembly(new MaybeToFlowable<T>(source));
    }

    public static <T> Flowable<T> toFlowable(CompletableSource source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaFlowablePlugins.onAssembly(new CompletableToFlowable<T>(source));
    }

    public static <T> Observable<T> toObservable(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new ObservableFromPublisher<T>(source));
    }

    // --------------------------------------------------------------------------------------------------
    // Flowable operators that return a different basetype
    // --------------------------------------------------------------------------------------------------

    public static <T> Single<List<T>> toList(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableToListSingle<T, List<T>>(source));
    }

    public static <T> Single<List<T>> toList(Flowable<T> source, int capacityHint) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.verifyPositive(capacityHint, "capacityHint");
        return RxJavaObservablePlugins.onAssembly(new FlowableToListSingle<T, List<T>>(source, Functions.<T>createArrayList(capacityHint)));
    }

    public static <T, C extends Collection<? super T>> Single<C> toList(Flowable<T> source, Callable<C> collectionSupplier) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(collectionSupplier, "collectionSupplier is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableToListSingle<T, C>(source, collectionSupplier));
    }

    public static <T> Completable ignoreElements(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableIgnoreElementsCompletable<T>(source));
    }

    public static <T> Maybe<T> reduce(Flowable<T> source, BiFunction<T, T, T> reducer) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(reducer, "reducer is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableReduceMaybe<T>(source, reducer));
    }

    public static <T, R> Single<R> reduceWith(Flowable<T> source, Callable<R> seed, BiFunction<R, ? super T, R> reducer) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(seed, "seed is null");
        ObjectHelper.requireNonNull(reducer, "reducer is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableReduceWithSingle<T, R>(source, seed, reducer));
    }

    public static <T, R> Single<R> reduce(Flowable<T> source, R seed, BiFunction<R, ? super T, R> reducer) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(seed, "seed is null");
        ObjectHelper.requireNonNull(reducer, "reducer is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableReduceSeedSingle<T, R>(source, seed, reducer));
    }

    public static <T, R> Flowable<R> flatMapSingle(Flowable<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {
        return flatMapSingle(source, mapper, false, Flowable.bufferSize());
    }

    public static <T, R> Flowable<R> flatMapSingle(Flowable<T> source,
            Function<? super T, ? extends SingleSource<? extends R>> mapper, boolean delayError, int maxConcurrency) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        return RxJavaFlowablePlugins.onAssembly(new FlowableFlatMapSingle<T, R>(source, mapper, delayError, maxConcurrency));
    }

    public static <T, R> Flowable<R> flatMapMaybe(Flowable<T> source,
            Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        return flatMapMaybe(source, mapper, false, Flowable.bufferSize());
    }

    public static <T, R> Flowable<R> flatMapMaybe(Flowable<T> source,
            Function<? super T, ? extends MaybeSource<? extends R>> mapper, boolean delayError, int maxConcurrency) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        return RxJavaFlowablePlugins.onAssembly(new FlowableFlatMapMaybe<T, R>(source, mapper, delayError, maxConcurrency));
    }

    public static <T> Completable flatMapCompletable(Flowable<T> source,
            Function<? super T, ? extends CompletableSource> mapper) {
        return flatMapCompletable(source, mapper, false, Flowable.bufferSize());
    }

    public static <T> Completable flatMapCompletable(Flowable<T> source,
            Function<? super T, ? extends CompletableSource> mapper, boolean delayError, int prefetch) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        ObjectHelper.verifyPositive(prefetch, "prefetch");
        return RxJavaObservablePlugins.onAssembly(new FlowableFlatMapCompletableCompletable<T>(source, mapper, delayError, prefetch));
    }

    public static <T, R> Flowable<R> flatMapPublisher(Single<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return toFlowable(source).flatMap(mapper);
    }

    public static <T, R> Flowable<R> flatMapPublisher(Maybe<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return toFlowable(source).flatMap(mapper);
    }

    public static <T, R> Flowable<R> flattenAsFlowable(Single<T> source,
            Function<? super T, ? extends Iterable<? extends R>> mapper) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        return RxJavaFlowablePlugins.onAssembly(new SingleFlatMapIterableFlowable<T, R>(source, mapper));
    }

    public static <T, R> Flowable<R> flattenAsFlowable(Maybe<T> source,
            Function<? super T, ? extends Iterable<? extends R>> mapper) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        return RxJavaFlowablePlugins.onAssembly(new MaybeFlatMapIterableFlowable<T, R>(source, mapper));
    }

    public static <T> Completable concatCompletable(Flowable<? extends CompletableSource> sources) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        return concatCompletable(sources, 2);
    }

    public static <T> Completable concatCompletable(Flowable<? extends CompletableSource> sources, int prefetch) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        ObjectHelper.verifyPositive(prefetch, "prefetch");
        return RxJavaObservablePlugins.onAssembly(new CompletableConcat(sources, prefetch));
    }

    public static <T> Completable mergeCompletable(Flowable<? extends CompletableSource> sources) {
        return mergeCompletable(sources, Integer.MAX_VALUE);
    }

    public static <T> Completable mergeCompletable(Flowable<? extends CompletableSource> sources, int maxConcurrency) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        return RxJavaObservablePlugins.onAssembly(new CompletableMerge(sources, maxConcurrency, false));
    }

    public static <T> Completable mergeCompletableDelayError(Flowable<? extends CompletableSource> sources) {
        return mergeCompletableDelayError(sources, Integer.MAX_VALUE);
    }

    public static <T> Completable mergeCompletableDelayError(Flowable<? extends CompletableSource> sources, int maxConcurrency) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        return RxJavaObservablePlugins.onAssembly(new CompletableMerge(sources, maxConcurrency, true));
    }

    public static <T> Flowable<T> concatSingle(Flowable<? extends Single<? extends T>> sources) {
        return concatSingle(sources, 2);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Flowable<T> concatSingle(Flowable<? extends Single<? extends T>> sources, int prefetch) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        ObjectHelper.verifyPositive(prefetch, "prefetch");
        return RxJavaFlowablePlugins.onAssembly(new FlowableConcatMapPublisher(sources, InteropInternalHelper.toFlowable(), prefetch, ErrorMode.IMMEDIATE));
    }

    public static <T> Flowable<T> concatMaybe(Flowable<? extends Maybe<? extends T>> sources) {
        return concatMaybe(sources, 2);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Flowable<T> concatMaybe(Flowable<? extends Maybe<? extends T>> sources, int prefetch) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        ObjectHelper.verifyPositive(prefetch, "prefetch");
        return RxJavaFlowablePlugins.onAssembly(new FlowableConcatMapPublisher(sources, MaybeToPublisher.instance(), prefetch, ErrorMode.IMMEDIATE));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, R> Flowable<R> mergeSingle(Flowable<? extends Single<? extends R>> sources) {
        ObjectHelper.requireNonNull(sources, "sources is null");
        return RxJavaFlowablePlugins.onAssembly(new FlowableFlatMapPublisher(sources, InteropInternalHelper.toFlowable(), false, Integer.MAX_VALUE, Flowable.bufferSize()));
    }

    public static <T, R> Flowable<R> mergeMaybe(Flowable<? extends Maybe<? extends R>> sources) {
        return mergeMaybe(sources, Integer.MAX_VALUE);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, R> Flowable<R> mergeMaybe(Flowable<? extends Maybe<? extends R>> sources, int maxConcurrency) {
        ObjectHelper.requireNonNull(sources, "source is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        return RxJavaFlowablePlugins.onAssembly(new FlowableFlatMapPublisher(sources, MaybeToPublisher.instance(), false, maxConcurrency, Flowable.bufferSize()));
    }

    public static <T> Single<T> first(Flowable<T> source, T defaultItem) {
        return elementAt(source, 0, defaultItem);
    }

    public static <T> Single<T> firstOrError(Flowable<T> source) {
        return elementAtOrError(source, 0);
    }

    public static <T> Maybe<T> firstElement(Flowable<T> source) {
        return elementAt(source, 0);
    }

    public static <T> Single<T> last(Flowable<T> source, T defaultItem) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(defaultItem, "defaultItem is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableLastSingle<T>(source, defaultItem));
    }

    public static <T> Single<T> lastOrError(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableLastSingle<T>(source, null));
    }

    public static <T> Maybe<T> lastElement(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableLastMaybe<T>(source));
    }

    public static <T> Single<T> single(Flowable<T> source, T defaultItem) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(defaultItem, "defaultItem is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableSingleSingle<T>(source, defaultItem));
    }

    public static <T> Single<T> singleOrError(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableSingleSingle<T>(source, null));
    }

    public static <T> Maybe<T> singleElement(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableSingleMaybe<T>(source));
    }

    public static <T> Maybe<T> elementAt(Flowable<T> source, long index) {
        ObjectHelper.requireNonNull(source, "source is null");
        if (index < 0L) {
            throw new IndexOutOfBoundsException("index >= 0 required but it was " + index);
        }
        return RxJavaObservablePlugins.onAssembly(new FlowableElementAtMaybe<T>(source, index));
    }

    public static <T> Single<T> elementAt(Flowable<T> source, long index, T defaultItem) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(defaultItem, "defaultItem is null");
        if (index < 0L) {
            throw new IndexOutOfBoundsException("index >= 0 required but it was " + index);
        }
        return RxJavaObservablePlugins.onAssembly(new FlowableElementAtSingle<T>(source, index, defaultItem));
    }

    public static <T> Single<T> elementAtOrError(Flowable<T> source, long index) {
        ObjectHelper.requireNonNull(source, "source is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("index >= 0 required but it was " + index);
        }
        return RxJavaObservablePlugins.onAssembly(new FlowableElementAtSingle<T>(source, index, null));
    }

    public static <T, C> Single<C> collect(Flowable<T> source, Callable<C> collectionSupplier, BiConsumer<? super C, ? super T> collector) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(collectionSupplier, "collectionSupplier is null");
        ObjectHelper.requireNonNull(collector, "collector is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableCollectSingle<T, C>(source, collectionSupplier, collector));
    }

    public static <T> Single<Boolean> any(Flowable<T> source, Predicate<? super T> predicate) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(predicate, "predicate is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableAnySingle<T>(source, predicate));
    }

    public static <T> Single<Boolean> all(Flowable<T> source, Predicate<? super T> predicate) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(predicate, "predicate is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableAllSingle<T>(source, predicate));
    }

    public static <T> Single<Boolean> isEmpty(Flowable<T> source) {
        return all(source, Functions.alwaysFalse());
    }

    public static <T> Single<Long> count(Flowable<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaObservablePlugins.onAssembly(new FlowableCountSingle<T>(source));
    }

    /**
     * Returns a Single that emits a single HashMap containing all items emitted by the source Publisher,
     * mapped by the keys returned by a specified {@code keySelector} function.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMap.png" alt="">
     * <p>
     * If more than one source item maps to the same key, the HashMap will contain the latest of those items.
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts the key from a source item to be used in the HashMap
     * @return a Single that emits a single item: a HashMap containing the mapped items from the source
     *         Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K> Single<Map<K, T>> toMap(Flowable<T> source, final Function<? super T, ? extends K> keySelector) {
        ObjectHelper.requireNonNull(keySelector, "keySelector is null");
        return collect(source, HashMapSupplier.<K, T>asCallable(), Functions.toMapKeySelector(keySelector));
    }

    /**
     * Returns a Single that emits a single HashMap containing values corresponding to items emitted by the
     * source Publisher, mapped by the keys returned by a specified {@code keySelector} function.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMap.png" alt="">
     * <p>
     * If more than one source item maps to the same key, the HashMap will contain a single entry that
     * corresponds to the latest of those items.
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <V> the value type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts the key from a source item to be used in the HashMap
     * @param valueSelector
     *            the function that extracts the value from a source item to be used in the HashMap
     * @return a Single that emits a single item: a HashMap containing the mapped items from the source
     *         Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K, V> Single<Map<K, V>> toMap(Flowable<T> source, final Function<? super T, ? extends K> keySelector, final Function<? super T, ? extends V> valueSelector) {
        ObjectHelper.requireNonNull(keySelector, "keySelector is null");
        ObjectHelper.requireNonNull(valueSelector, "valueSelector is null");
        return collect(source, HashMapSupplier.<K, V>asCallable(), Functions.toMapKeyValueSelector(keySelector, valueSelector));
    }

    /**
     * Returns a Single that emits a single Map, returned by a specified {@code mapFactory} function, that
     * contains keys and values extracted from the items emitted by the source Publisher.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMap.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <V> the value type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts the key from a source item to be used in the Map
     * @param valueSelector
     *            the function that extracts the value from the source items to be used as value in the Map
     * @param mapSupplier
     *            the function that returns a Map instance to be used
     * @return a Flowable that emits a single item: a Map that contains the mapped items emitted by the
     *         source Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @SuppressWarnings("unchecked")
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K, V> Single<Map<K, V>> toMap(Flowable<T> source, final Function<? super T, ? extends K> keySelector,
            final Function<? super T, ? extends V> valueSelector,
            final Callable<? extends Map<K, V>> mapSupplier) {
        ObjectHelper.requireNonNull(keySelector, "keySelector is null");
        ObjectHelper.requireNonNull(valueSelector, "valueSelector is null");
        return (Single<Map<K, V>>)collect(source, mapSupplier, Functions.toMapKeyValueSelector(keySelector, valueSelector));
    }

    /**
     * Returns a Single that emits a single HashMap that contains an ArrayList of items emitted by the
     * source Publisher keyed by a specified {@code keySelector} function.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMultiMap.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>This operator does not support backpressure as by intent it is requesting and buffering everything.</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMultimap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts the key from the source items to be used as key in the HashMap
     * @return a Single that emits a single item: a HashMap that contains an ArrayList of items mapped from
     *         the source Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K> Single<Map<K, Collection<T>>> toMultimap(Flowable<T> source, Function<? super T, ? extends K> keySelector) {
        Function<T, T> valueSelector = Functions.identity();
        Callable<Map<K, Collection<T>>> mapSupplier = HashMapSupplier.asCallable();
        Function<K, List<T>> collectionFactory = ArrayListSupplier.asFunction();
        return toMultimap(source, keySelector, valueSelector, mapSupplier, collectionFactory);
    }

    /**
     * Returns a Single that emits a single HashMap that contains an ArrayList of values extracted by a
     * specified {@code valueSelector} function from items emitted by the source Publisher, keyed by a
     * specified {@code keySelector} function.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMultiMap.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMultimap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <V> the value type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts a key from the source items to be used as key in the HashMap
     * @param valueSelector
     *            the function that extracts a value from the source items to be used as value in the HashMap
     * @return a Single that emits a single item: a HashMap that contains an ArrayList of items mapped from
     *         the source Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K, V> Single<Map<K, Collection<V>>> toMultimap(Flowable<T> source, Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector) {
        Callable<Map<K, Collection<V>>> mapSupplier = HashMapSupplier.asCallable();
        Function<K, List<V>> collectionFactory = ArrayListSupplier.asFunction();
        return toMultimap(source, keySelector, valueSelector, mapSupplier, collectionFactory);
    }

    /**
     * Returns a Single that emits a single Map, returned by a specified {@code mapFactory} function, that
     * contains a custom collection of values, extracted by a specified {@code valueSelector} function from
     * items emitted by the source Publisher, and keyed by the {@code keySelector} function.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMultiMap.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMultimap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <V> the value type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts a key from the source items to be used as the key in the Map
     * @param valueSelector
     *            the function that extracts a value from the source items to be used as the value in the Map
     * @param mapSupplier
     *            the function that returns a Map instance to be used
     * @param collectionFactory
     *            the function that returns a Collection instance for a particular key to be used in the Map
     * @return a Single that emits a single item: a Map that contains the collection of mapped items from
     *         the source Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @SuppressWarnings("unchecked")
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K, V> Single<Map<K, Collection<V>>> toMultimap(
            Flowable<T> source,
            final Function<? super T, ? extends K> keySelector,
            final Function<? super T, ? extends V> valueSelector,
            final Callable<? extends Map<K, Collection<V>>> mapSupplier,
            final Function<? super K, ? extends Collection<? super V>> collectionFactory) {
        ObjectHelper.requireNonNull(keySelector, "keySelector is null");
        ObjectHelper.requireNonNull(valueSelector, "valueSelector is null");
        ObjectHelper.requireNonNull(mapSupplier, "mapSupplier is null");
        ObjectHelper.requireNonNull(collectionFactory, "collectionFactory is null");
        return (Single<Map<K, Collection<V>>>)collect(source, mapSupplier, Functions.toMultimapKeyValueSelector(keySelector, valueSelector, collectionFactory));
    }

    /**
     * Returns a Single that emits a single Map, returned by a specified {@code mapFactory} function, that
     * contains an ArrayList of values, extracted by a specified {@code valueSelector} function from items
     * emitted by the source Publisher and keyed by the {@code keySelector} function.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toMultiMap.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toMultimap} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <K> the key type of the Map
     * @param <V> the value type of the Map
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param keySelector
     *            the function that extracts a key from the source items to be used as the key in the Map
     * @param valueSelector
     *            the function that extracts a value from the source items to be used as the value in the Map
     * @param mapSupplier
     *            the function that returns a Map instance to be used
     * @return a Single that emits a single item: a Map that contains a list items mapped from the source
     *         Publisher
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T, K, V> Single<Map<K, Collection<V>>> toMultimap(
            Flowable<T> source,
            Function<? super T, ? extends K> keySelector,
            Function<? super T, ? extends V> valueSelector,
            Callable<Map<K, Collection<V>>> mapSupplier
            ) {
        return toMultimap(source, keySelector, valueSelector, mapSupplier, ArrayListSupplier.<V, K>asFunction());
    }

    /**
     * Returns a Single that emits a list that contains the items emitted by the source Publisher, in a
     * sorted order. Each item emitted by the Publisher must implement {@link Comparable} with respect to all
     * other items in the sequence.
     *
     * <p>If any item emitted by this Flowable does not implement {@link Comparable} with respect to
     *             all other items emitted by this Flowable, no items will be emitted and the
     *             sequence is terminated with a {@link ClassCastException}.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toSortedList.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toSortedList} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @return a Single that emits a list that contains the items emitted by the source Publisher in
     *         sorted order
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> Single<List<T>> toSortedList(Flowable<T> source) {
        return toSortedList(source, Functions.naturalComparator());
    }

    /**
     * Returns a Single that emits a list that contains the items emitted by the source Publisher, in a
     * sorted order based on a specified comparison function.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toSortedList.f.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toSortedList} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param comparator
     *            a function that compares two items emitted by the source Publisher and returns an Integer
     *            that indicates their sort order
     * @return a Single that emits a list that contains the items emitted by the source Publisher in
     *         sorted order
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> Single<List<T>> toSortedList(Flowable<T> source, final Comparator<? super T> comparator) {
        ObjectHelper.requireNonNull(comparator, "comparator is null");
        return toList(source).map(Functions.listSorter(comparator));
    }

    /**
     * Returns a Single that emits a list that contains the items emitted by the source Publisher, in a
     * sorted order based on a specified comparison function.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toSortedList.f.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toSortedList} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param comparator
     *            a function that compares two items emitted by the source Publisher and returns an Integer
     *            that indicates their sort order
     * @param capacityHint
     *             the initial capacity of the ArrayList used to accumulate items before sorting
     * @return a Single that emits a list that contains the items emitted by the source Publisher in
     *         sorted order
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     * @since 2.0
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> Single<List<T>> toSortedList(Flowable<T> source, final Comparator<? super T> comparator, int capacityHint) {
        ObjectHelper.requireNonNull(comparator, "comparator is null");
        return toList(source, capacityHint).map(Functions.listSorter(comparator));
    }

    /**
     * Returns a Flowable that emits a list that contains the items emitted by the source Publisher, in a
     * sorted order. Each item emitted by the Publisher must implement {@link Comparable} with respect to all
     * other items in the sequence.
     *
     * <p>If any item emitted by this Flowable does not implement {@link Comparable} with respect to
     *             all other items emitted by this Flowable, no items will be emitted and the
     *             sequence is terminated with a {@link ClassCastException}.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/toSortedList.png" alt="">
     * <dl>
     *  <dt><b>Backpressure:</b></dt>
     *  <dd>The operator honors backpressure from downstream and consumes the source {@code Publisher} in an
     *  unbounded manner (i.e., without applying backpressure to it).</dd>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code toSortedList} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <T> the source value type
     * @param source the source Flowable instance
     * @param capacityHint
     *             the initial capacity of the ArrayList used to accumulate items before sorting
     * @return a Flowable that emits a list that contains the items emitted by the source Publisher in
     *         sorted order
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     * @since 2.0
     */
    @CheckReturnValue
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> Single<List<T>> toSortedList(Flowable<T> source, int capacityHint) {
        return toSortedList(source, Functions.naturalComparator(), capacityHint);
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
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        ObjectHelper.requireNonNull(combine, "combine is null");
        return (S) new SchedulerWhen(combine, scheduler);
    }

    public static <T, U> Single<T> takeUntil(Single<T> source, Publisher<U> other) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(other, "other is null");
        return RxJavaObservablePlugins.onAssembly(new SingleTakeUntilPublisher<T, U>(source, other));
    }

    public static <T, U> Maybe<T> takeUntil(Maybe<T> source, Publisher<U> other) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(other, "other is null");
        return RxJavaObservablePlugins.onAssembly(new MaybeTakeUntilPublisher<T, U>(source, other));
    }

    public static <T, U> Maybe<T> timeout(Maybe<T> source, Publisher<U> other) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(other, "other is null");
        return RxJavaObservablePlugins.onAssembly(new MaybeTimeoutPublisher<T, U>(source, other, null));
    }

    public static <T, U> Maybe<T> timeout(Maybe<T> source, Publisher<U> other, Maybe<T> fallback) {
        ObjectHelper.requireNonNull(source, "source is null");
        ObjectHelper.requireNonNull(other, "other is null");
        ObjectHelper.requireNonNull(fallback, "fallback is null");
        return RxJavaObservablePlugins.onAssembly(new MaybeTimeoutPublisher<T, U>(source, other, fallback));
    }
}
