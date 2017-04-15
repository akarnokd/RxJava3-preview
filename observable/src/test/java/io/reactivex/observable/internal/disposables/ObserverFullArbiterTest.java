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

package io.reactivex.observable.internal.disposables;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.observable.internal.utils.NotificationLite;
import io.reactivex.observable.observers.TestObserver;

public class ObserverFullArbiterTest {

    @Test
    public void setSubscriptionAfterCancel() {
        ObserverFullArbiter<Integer> fa = new ObserverFullArbiter<Integer>(new TestObserver<Integer>(), null, 128);

        fa.dispose();

        Disposable bs = Disposables.empty();

        assertFalse(fa.setDisposable(bs));

        assertFalse(fa.setDisposable(null));
    }

    @Test
    public void cancelAfterPoll() {
        ObserverFullArbiter<Integer> fa = new ObserverFullArbiter<Integer>(new TestObserver<Integer>(), null, 128);

        Disposable bs = Disposables.empty();

        fa.queue.offer(fa.s, NotificationLite.disposable(bs));

        assertFalse(fa.isDisposed());

        fa.dispose();

        assertTrue(fa.isDisposed());

        fa.drain();

        assertTrue(bs.isDisposed());
    }

    @Test
    public void errorAfterCancel() {
        ObserverFullArbiter<Integer> fa = new ObserverFullArbiter<Integer>(new TestObserver<Integer>(), null, 128);

        Disposable bs = Disposables.empty();

        fa.dispose();

        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            fa.onError(new TestException(), bs);

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void cancelAfterError() {
        ObserverFullArbiter<Integer> fa = new ObserverFullArbiter<Integer>(new TestObserver<Integer>(), null, 128);

        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            fa.queue.offer(fa.s, NotificationLite.error(new TestException()));

            fa.dispose();

            fa.drain();
            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void offerDifferentSubscription() {
        TestObserver<Integer> ts = new TestObserver<Integer>();

        ObserverFullArbiter<Integer> fa = new ObserverFullArbiter<Integer>(ts, null, 128);

        Disposable bs = Disposables.empty();

        fa.queue.offer(bs, NotificationLite.next(1));

        fa.drain();

        ts.assertNoValues();
    }

    @Test
    public void dontEnterDrain() {
        TestObserver<Integer> ts = new TestObserver<Integer>();

        ObserverFullArbiter<Integer> fa = new ObserverFullArbiter<Integer>(ts, null, 128);

        fa.queue.offer(fa.s, NotificationLite.next(1));

        fa.wip.getAndIncrement();

        fa.drain();

        ts.assertNoValues();
    }
}
