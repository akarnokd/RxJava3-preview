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

package io.reactivex.interop.internal.operators;

import org.reactivestreams.*;

import hu.akarnokd.reactivestreams.extensions.RelaxedSubscriber;
import io.reactivex.common.*;
import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.functions.BiFunction;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.flowable.*;
import io.reactivex.flowable.extensions.*;
import io.reactivex.flowable.internal.operators.FlowableReduce;
import io.reactivex.flowable.internal.subscriptions.SubscriptionHelper;
import io.reactivex.observable.*;

/**
 * Reduce a Flowable into a single value exposed as Single or signal NoSuchElementException.
 *
 * @param <T> the value type
 */
public final class FlowableReduceMaybe<T>
extends Maybe<T>
implements HasUpstreamPublisher<T>, FuseToFlowable<T> {

    final Flowable<T> source;

    final BiFunction<T, T, T> reducer;

    public FlowableReduceMaybe(Flowable<T> source, BiFunction<T, T, T> reducer) {
        this.source = source;
        this.reducer = reducer;
    }

    @Override
    public Publisher<T> source() {
        return source;
    }

    @Override
    public Flowable<T> fuseToFlowable() {
        return RxJavaFlowablePlugins.onAssembly(new FlowableReduce<T>(source, reducer));
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new ReduceSubscriber<T>(observer, reducer));
    }

    static final class ReduceSubscriber<T> implements RelaxedSubscriber<T>, Disposable {
        final MaybeObserver<? super T> actual;

        final BiFunction<T, T, T> reducer;

        T value;

        Subscription s;

        boolean done;

        ReduceSubscriber(MaybeObserver<? super T> actual, BiFunction<T, T, T> reducer) {
            this.actual = actual;
            this.reducer = reducer;
        }

        @Override
        public void dispose() {
            s.cancel();
            done = true;
        }

        @Override
        public boolean isDisposed() {
            return done;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.s, s)) {
                this.s = s;

                actual.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            T v = value;
            if (v == null) {
                value = t;
            } else {
                try {
                    value = ObjectHelper.requireNonNull(reducer.apply(v, t), "The reducer returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    s.cancel();
                    onError(ex);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaCommonPlugins.onError(t);
                return;
            }
            done = true;
            actual.onError(t);
         }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            T v = value;
            if (v != null) {
//                value = null;
                actual.onSuccess(v);
            } else {
                actual.onComplete();
            }
        }


    }
}
