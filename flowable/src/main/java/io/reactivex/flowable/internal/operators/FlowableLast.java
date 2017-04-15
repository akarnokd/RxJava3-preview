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

import java.util.NoSuchElementException;

import org.reactivestreams.*;

import hu.akarnokd.reactivestreams.extensions.RelaxedSubscriber;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.internal.subscriptions.*;

public final class FlowableLast<T> extends AbstractFlowableWithUpstream<T, T> {

    final T defaultItem;

    final boolean errorOnEmpty;
    
    public FlowableLast(Flowable<T> source, T defaultItem, boolean errorOnEmpty) {
        super(source);
        this.defaultItem = defaultItem;
        this.errorOnEmpty = errorOnEmpty;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new LastSubscriber<T>(s, defaultItem, errorOnEmpty));
    }

    static final class LastSubscriber<T> extends DeferredScalarSubscription<T>
    implements RelaxedSubscriber<T> {

        private static final long serialVersionUID = 5455573518954847071L;

        final T defaultItem;

        final boolean errorOnEmpty;

        Subscription upstream;

        public LastSubscriber(Subscriber<? super T> actual, T defaultItem, boolean errorOnEmpty) {
            super(actual);
            this.defaultItem = defaultItem;
            this.errorOnEmpty = errorOnEmpty;
        }

        @Override
        public void onNext(T t) {
            this.value = t;
        }

        @Override
        public void onError(Throwable t) {
            value = null;
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            T v = value;
            value = null;
            if (v == null) {
                if (errorOnEmpty) {
                    actual.onError(new NoSuchElementException());
                } else {
                    actual.onComplete();
                }
            } else {
                complete(v);
            }
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
