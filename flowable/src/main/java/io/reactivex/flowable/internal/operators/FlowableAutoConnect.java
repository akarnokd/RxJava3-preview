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

import java.util.concurrent.atomic.AtomicInteger;

import org.reactivestreams.Subscriber;

import io.reactivex.common.Disposable;
import io.reactivex.common.functions.Consumer;
import io.reactivex.flowable.*;

/**
 * Wraps a ConnectableObservable and calls its connect() method once
 * the specified number of Subscribers have subscribed.
 *
 * @param <T> the value type of the chain
 */
public final class FlowableAutoConnect<T> extends Flowable<T> {
    final ConnectableFlowable<? extends T> source;
    final int numberOfSubscribers;
    final Consumer<? super Disposable> connection;
    final AtomicInteger clients;

    public FlowableAutoConnect(ConnectableFlowable<? extends T> source,
            int numberOfSubscribers,
            Consumer<? super Disposable> connection) {
        this.source = source;
        this.numberOfSubscribers = numberOfSubscribers;
        this.connection = connection;
        this.clients = new AtomicInteger();
    }

    @Override
    public void subscribeActual(Subscriber<? super T> child) {
        source.subscribe(child);
        if (clients.incrementAndGet() == numberOfSubscribers) {
            source.connect(connection);
        }
    }
}
