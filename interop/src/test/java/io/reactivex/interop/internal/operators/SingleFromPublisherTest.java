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

import static io.reactivex.interop.RxJava3Interop.singleOrError;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.reactivestreams.Subscriber;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.internal.subscriptions.BooleanSubscription;
import io.reactivex.flowable.processors.PublishProcessor;
import io.reactivex.observable.TestHelper;
import io.reactivex.observable.observers.TestObserver;

public class SingleFromPublisherTest {

    @Test
    public void just() {
        singleOrError(Flowable.just(1))
        .test()
        .assertResult(1);
    }

    @Test
    public void range() {
        singleOrError(Flowable.range(1, 3))
        .test()
        .assertFailure(IllegalArgumentException.class);
    }

    @Test
    public void empty() {
        singleOrError(Flowable.empty())
        .test()
        .assertFailure(NoSuchElementException.class);
    }

    @Test
    public void error() {
        singleOrError(Flowable.error(new TestException()))
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestObserver<Integer> ts = singleOrError(pp).test();

        assertTrue(pp.hasSubscribers());

        pp.onNext(1);

        ts.cancel();

        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(singleOrError(Flowable.never()));
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();

        try {
            singleOrError(new Flowable<Integer>() {
                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    BooleanSubscription s2 = new BooleanSubscription();
                    s.onSubscribe(s2);
                    assertTrue(s2.isCancelled());

                    s.onNext(1);
                    s.onComplete();
                    s.onNext(2);
                    s.onError(new TestException());
                    s.onComplete();
                }
            })
            .test()
            .assertResult(1);

            TestCommonHelper.assertError(errors, 0, IllegalStateException.class, "Subscription already set!");
            TestCommonHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }
}
