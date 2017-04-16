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

import static io.reactivex.interop.RxJava3Interop.toFlowable;

import org.junit.Test;

import io.reactivex.common.exceptions.MissingBackpressureException;
import io.reactivex.flowable.BackpressureStrategy;
import io.reactivex.flowable.subscribers.TestSubscriber;
import io.reactivex.observable.Observable;

public class ObservableToXTest {

    @Test
    public void toFlowableBuffer() {
        toFlowable(Observable.range(1, 5)
        , BackpressureStrategy.BUFFER)
        .test(2L)
        .assertValues(1, 2)
        .assertNoErrors()
        .assertNotComplete();
    }

    @Test
    public void toFlowableDrop() {
        toFlowable(Observable.range(1, 5)
        , BackpressureStrategy.DROP)
        .test(1)
        .assertResult(1);
    }

    @Test
    public void toFlowableLatest() {
        TestSubscriber<Integer> ts = toFlowable(Observable.range(1, 5)
        , BackpressureStrategy.LATEST)
        .test(0);

        ts.request(1);
        ts
        .assertResult(5);
    }

    @Test
    public void toFlowableError1() {
        toFlowable(Observable.range(1, 5)
        , BackpressureStrategy.ERROR)
        .test(1)
        .assertFailure(MissingBackpressureException.class, 1);
    }

    @Test
    public void toFlowableError2() {
        toFlowable(Observable.range(1, 5)
        , BackpressureStrategy.ERROR)
        .test(5)
        .assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void toFlowableMissing() {
        TestSubscriber<Integer> ts = toFlowable(Observable.range(1, 5)
                , BackpressureStrategy.MISSING)
                .test(0);

        ts.request(2);
        ts
        .assertResult(1, 2, 3, 4, 5);
    }
}
