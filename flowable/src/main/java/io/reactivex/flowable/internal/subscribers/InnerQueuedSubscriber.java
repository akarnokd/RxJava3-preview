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

import org.reactivestreams.Subscription;

import hu.akarnokd.reactivestreams.extensions.*;
import io.reactivex.flowable.internal.subscriptions.SubscriptionHelper;
import io.reactivex.flowable.internal.utils.QueueDrainHelper;

/**
 * Subscriber that can fuse with the upstream and calls a support interface
 * whenever an event is available.
 *
 * @param <T> the value type
 */
public final class InnerQueuedSubscriber<T>
extends AtomicReference<Subscription>
implements RelaxedSubscriber<T>, Subscription {


    private static final long serialVersionUID = 22876611072430776L;

    final InnerQueuedSubscriberSupport<T> parent;

    final int prefetch;

    final int limit;

    volatile FusedQueue<T> queue;

    volatile boolean done;

    long produced;

    int fusionMode;

    public InnerQueuedSubscriber(InnerQueuedSubscriberSupport<T> parent, int prefetch) {
        this.parent = parent;
        this.prefetch = prefetch;
        this.limit = prefetch - (prefetch >> 2);
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(this, s)) {
            if (s instanceof FusedQueueSubscription) {
                @SuppressWarnings("unchecked")
                FusedQueueSubscription<T> qs = (FusedQueueSubscription<T>) s;

                int m = qs.requestFusion(FusedQueueSubscription.ANY);
                if (m == FusedQueueSubscription.SYNC) {
                    fusionMode = m;
                    queue = qs;
                    done = true;
                    parent.innerComplete(this);
                    return;
                }
                if (m == FusedQueueSubscription.ASYNC) {
                    fusionMode = m;
                    queue = qs;
                    QueueDrainHelper.request(s, prefetch);
                    return;
                }
            }

            queue = QueueDrainHelper.createQueue(prefetch);

            QueueDrainHelper.request(s, prefetch);
        }
    }

    @Override
    public void onNext(T t) {
        if (fusionMode == FusedQueueSubscription.NONE) {
            parent.innerNext(this, t);
        } else {
            parent.drain();
        }
    }

    @Override
    public void onError(Throwable t) {
        parent.innerError(this, t);
    }

    @Override
    public void onComplete() {
        parent.innerComplete(this);
    }

    @Override
    public void request(long n) {
        if (fusionMode != FusedQueueSubscription.SYNC) {
            long p = produced + n;
            if (p >= limit) {
                produced = 0L;
                get().request(p);
            } else {
                produced = p;
            }
        }
    }

    public void requestOne() {
        if (fusionMode != FusedQueueSubscription.SYNC) {
            long p = produced + 1;
            if (p == limit) {
                produced = 0L;
                get().request(p);
            } else {
                produced = p;
            }
        }
    }

    @Override
    public void cancel() {
        SubscriptionHelper.cancel(this);
    }

    public boolean isDone() {
        return done;
    }

    public void setDone() {
        this.done = true;
    }

    public FusedQueue<T> queue() {
        return queue;
    }
}
