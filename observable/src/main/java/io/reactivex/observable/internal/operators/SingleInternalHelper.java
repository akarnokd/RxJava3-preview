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

import java.util.*;
import java.util.concurrent.Callable;

import io.reactivex.common.functions.Function;
import io.reactivex.observable.Observable;
import io.reactivex.observable.SingleSource;

/**
 * Helper utility class to support Single with inner classes.
 */
public final class SingleInternalHelper {

    /** Utility class. */
    private SingleInternalHelper() {
        throw new IllegalStateException("No instances!");
    }

    enum NoSuchElementCallable implements Callable<NoSuchElementException> {
        INSTANCE;

        @Override
        public NoSuchElementException call() throws Exception {
            return new NoSuchElementException();
        }
    }

    public static <T> Callable<NoSuchElementException> emptyThrower() {
        return NoSuchElementCallable.INSTANCE;
    }

    @SuppressWarnings("rawtypes")
    enum ToObservable implements Function<SingleSource, Observable> {
        INSTANCE;
        @SuppressWarnings("unchecked")
        @Override
        public Observable apply(SingleSource v) {
            return new SingleToObservable(v);
        }
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Function<SingleSource<? extends T>, Observable<? extends T>> toObservable() {
        return (Function)ToObservable.INSTANCE;
    }
    
    static final class ToObservableIterator<T> implements Iterator<Observable<T>> {
        private final Iterator<? extends SingleSource<? extends T>> sit;

        ToObservableIterator(Iterator<? extends SingleSource<? extends T>> sit) {
            this.sit = sit;
        }

        @Override
        public boolean hasNext() {
            return sit.hasNext();
        }

        @Override
        public Observable<T> next() {
            return new SingleToObservable<T>(sit.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static final class ToObservableIterable<T> implements Iterable<Observable<T>> {

        private final Iterable<? extends SingleSource<? extends T>> sources;

        ToObservableIterable(Iterable<? extends SingleSource<? extends T>> sources) {
            this.sources = sources;
        }

        @Override
        public Iterator<Observable<T>> iterator() {
            return new ToObservableIterator<T>(sources.iterator());
        }
    }

    public static <T> Iterable<? extends Observable<T>> iterableToObservable(final Iterable<? extends SingleSource<? extends T>> sources) {
        return new ToObservableIterable<T>(sources);
    }

}
