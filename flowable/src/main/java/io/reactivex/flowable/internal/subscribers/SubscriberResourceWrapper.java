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

package io.reactivex.flowable.internal.subscribers;

import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.*;

import hu.akarnokd.reactivestreams.extensions.RelaxedSubscriber;
import io.reactivex.common.Disposable;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.flowable.internal.subscriptions.SubscriptionHelper;

public final class SubscriberResourceWrapper<T> extends AtomicReference<Disposable> implements RelaxedSubscriber<T>, Disposable, Subscription {

    private static final long serialVersionUID = -8612022020200669122L;

    final Subscriber<? super T> actual;

    final AtomicReference<Subscription> subscription = new AtomicReference<Subscription>();

    public SubscriberResourceWrapper(Subscriber<? super T> actual) {
        this.actual = actual;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(subscription, s)) {
            actual.onSubscribe(this);
        }
    }

    @Override
    public void onNext(T t) {
        actual.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        DisposableHelper.dispose(this);
        actual.onError(t);
    }

    @Override
    public void onComplete() {
        DisposableHelper.dispose(this);
        actual.onComplete();
    }

    @Override
    public void request(long n) {
        if (SubscriptionHelper.validate(n)) {
            subscription.get().request(n);
        }
    }

    @Override
    public void dispose() {
        SubscriptionHelper.cancel(subscription);

        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return subscription.get() == SubscriptionHelper.CANCELLED;
    }

    @Override
    public void cancel() {
        dispose();
    }

    public void setResource(Disposable resource) {
        DisposableHelper.set(this, resource);
    }
}
