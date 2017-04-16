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

import static io.reactivex.interop.RxJava3Interop.ignoreElements;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.Test;

import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.*;
import io.reactivex.flowable.Flowable;
import io.reactivex.interop.TestHelper;
import io.reactivex.observable.observers.*;

public class FlowableIgnoreElementsTest {

    @Test
    public void testWithEmpty() {
        assertNull(ignoreElements(Flowable.empty()).blockingGet());
    }

    @Test
    public void testWithNonEmpty() {
        assertNull(ignoreElements(Flowable.just(1, 2, 3)).blockingGet());
    }

    @Test
    public void testUpstreamIsProcessedButIgnored() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        Object count = ignoreElements(Flowable.range(1, num)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer t) {
                        upstreamCount.incrementAndGet();
                    }
                })
                )
                .blockingGet();
        assertEquals(num, upstreamCount.get());
        assertNull(count);
    }

    @Test
    public void testCompletedOk() {
        TestObserver<Object> ts = new TestObserver<Object>();
        ignoreElements(Flowable.range(1, 10)).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertTerminated();
        // FIXME no longer testable
//        ts.assertUnsubscribed();
    }

    @Test
    public void testErrorReceived() {
        TestObserver<Object> ts = new TestObserver<Object>();
        TestException ex = new TestException("boo");
        ignoreElements(Flowable.error(ex)).subscribe(ts);
        ts.assertNoValues();
        ts.assertTerminated();
        // FIXME no longer testable
//        ts.assertUnsubscribed();
        ts.assertError(TestException.class);
        ts.assertErrorMessage("boo");
    }

    @Test
    public void testUnsubscribesFromUpstream() {
        final AtomicBoolean unsub = new AtomicBoolean();
        ignoreElements(Flowable.range(1, 10).concatWith(Flowable.<Integer>never())
        .doOnCancel(new Action() {
            @Override
            public void run() {
                unsub.set(true);
            }})
            )
            .subscribe().dispose();

        assertTrue(unsub.get());
    }

    @Test(timeout = 10000)
    public void testDoesNotHangAndProcessesAllUsingBackpressure() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final AtomicInteger count = new AtomicInteger(0);
        int num = 10;
        ignoreElements(Flowable.range(1, num)
        //
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer t) {
                        upstreamCount.incrementAndGet();
                    }
                })
                //
                )
                //
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count.get());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(ignoreElements(Flowable.just(1)));
    }
}
