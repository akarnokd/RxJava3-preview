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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.flowable.subscribers.TestSubscriber;
import io.reactivex.interop.RxJava3Interop;
import io.reactivex.observable.*;

public class MaybeConcatArrayTest {

    @SuppressWarnings("unchecked")
    @Test
    public void cancel() {
        RxJava3Interop.concatMaybeArray(Maybe.just(1), Maybe.just(2))
        .take(1)
        .test()
        .assertResult(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void cancelDelayError() {
        RxJava3Interop.concatMaybeArrayDelayError(Maybe.just(1), Maybe.just(2))
        .take(1)
        .test()
        .assertResult(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = RxJava3Interop.concatMaybeArray(Maybe.just(1), Maybe.just(2))
        .test(0L);

        ts.assertEmpty();

        ts.request(1);

        ts.assertValue(1);

        ts.request(2);

        ts.assertResult(1, 2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void backpressureDelayError() {
        TestSubscriber<Integer> ts = RxJava3Interop.concatMaybeArrayDelayError(Maybe.just(1), Maybe.just(2))
        .test(0L);

        ts.assertEmpty();

        ts.request(1);

        ts.assertValue(1);

        ts.request(2);

        ts.assertResult(1, 2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestCancelRace() {
        for (int i = 0; i < 500; i++) {
            final TestSubscriber<Integer> ts = RxJava3Interop.concatMaybeArray(Maybe.just(1), Maybe.just(2))
                    .test(0L);

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    ts.cancel();
                }
            };

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    ts.request(1);
                }
            };

            TestCommonHelper.race(r1, r2);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestCancelRaceDelayError() {
        for (int i = 0; i < 500; i++) {
            final TestSubscriber<Integer> ts = RxJava3Interop.concatMaybeArrayDelayError(Maybe.just(1), Maybe.just(2))
                    .test(0L);

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    ts.cancel();
                }
            };

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    ts.request(1);
                }
            };

            TestCommonHelper.race(r1, r2);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorAfterTermination() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final MaybeObserver<?>[] o = { null };
            RxJava3Interop.concatMaybeArrayDelayError(Maybe.just(1),
                    Maybe.error(new IOException()),
            new Maybe<Integer>() {
                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                    observer.onSubscribe(Disposables.empty());
                    observer.onSuccess(2);
                    o[0] = observer;
                }
            })
            .test()
            .assertFailure(IOException.class, 1, 2);

            o[0].onError(new TestException());


            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noSubsequentSubscription() {
        final int[] calls = { 0 };

        Maybe<Integer> source = Maybe.create(new MaybeOnSubscribe<Integer>() {
            @Override
            public void subscribe(MaybeEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });

        RxJava3Interop.concatMaybeArray(source, source).firstElement()
        .test()
        .assertResult(1);

        assertEquals(1, calls[0]);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noSubsequentSubscriptionDelayError() {
        final int[] calls = { 0 };

        Maybe<Integer> source = Maybe.create(new MaybeOnSubscribe<Integer>() {
            @Override
            public void subscribe(MaybeEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });

        RxJava3Interop.concatMaybeArrayDelayError(source, source).firstElement()
        .test()
        .assertResult(1);

        assertEquals(1, calls[0]);
    }
}
