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
import io.reactivex.common.RxJavaCommonPlugins;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.internal.subscriptions.*;

public final class FlowableSingle<T> extends AbstractFlowableWithUpstream<T, T> {

    final T defaultValue;

    final boolean errorOnEmpty;

    public FlowableSingle(Flowable<T> source, T defaultValue, boolean errorOnEmpty) {
        super(source);
        this.defaultValue = defaultValue;
        this.errorOnEmpty = errorOnEmpty;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        source.subscribe(new SingleElementSubscriber<T>(s, defaultValue, errorOnEmpty));
    }

    static final class SingleElementSubscriber<T> extends DeferredScalarSubscription<T>
    implements RelaxedSubscriber<T> {

        private static final long serialVersionUID = -5526049321428043809L;

        final T defaultValue;

        final boolean errorOnEmpty;

        Subscription s;

        boolean done;

        SingleElementSubscriber(Subscriber<? super T> actual, T defaultValue, boolean errorOnEmpty) {
            super(actual);
            this.defaultValue = defaultValue;
            this.errorOnEmpty = errorOnEmpty;
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
            if (value != null) {
                done = true;
                s.cancel();
                actual.onError(new IllegalArgumentException("Sequence contains more than one element!"));
                return;
            }
            value = t;
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
            value = null;
            if (v == null) {
                v = defaultValue;
            }
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
        public void cancel() {
            super.cancel();
            s.cancel();
        }
    }
}
