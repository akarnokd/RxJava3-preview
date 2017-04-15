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

package io.reactivex.flowable.internal.operators;

import java.util.concurrent.Callable;

import org.reactivestreams.*;

import hu.akarnokd.reactivestreams.extensions.RelaxedSubscriber;
import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.functions.BiFunction;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.internal.subscriptions.*;

public final class FlowableReduceWith<T, R> extends AbstractFlowableWithUpstream<T, R> {

    final Callable<R> initialValueSupplier;
    
    final BiFunction<R, ? super T, R> reducer;
    
    public FlowableReduceWith(Flowable<T> source, Callable<R> initialValueSupplier, BiFunction<R, ? super T, R> reducer) {
        super(source);
        this.initialValueSupplier = initialValueSupplier;
        this.reducer = reducer;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        R initialValue;

        try {
            initialValue = ObjectHelper.requireNonNull(initialValueSupplier.call(), "The initialValueSupplier returned a null value");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, s);
            return;
        }

        source.subscribe(new ReduceWithSubscriber<T, R>(s, initialValue, reducer));
    }

    static final class ReduceWithSubscriber<T, R> extends DeferredScalarSubscription<R>
    implements RelaxedSubscriber<T> {

        private static final long serialVersionUID = 7129356975009970557L;

        final BiFunction<R, ? super T, R> reducer;

        Subscription upstream;

        boolean done;

        public ReduceWithSubscriber(Subscriber<? super R> actual, R value, BiFunction<R, ? super T, R> reducer) {
            super(actual);
            this.value = value;
            this.reducer = reducer;
        }

        @Override
        public void onNext(T t) {
            if (!done) {
                try {
                    value = ObjectHelper.requireNonNull(reducer.apply(value, t), "The reducer returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    onError(ex);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                return;
            }
            done = true;
            value = null;
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            R v = value;
            value = null;
            complete(v);
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(upstream, s)) {
                upstream = s;

                actual.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            upstream.cancel();
        }
    }
}
