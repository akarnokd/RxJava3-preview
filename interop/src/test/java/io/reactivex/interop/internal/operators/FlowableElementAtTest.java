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

import static io.reactivex.interop.RxJava3Interop.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.reactivestreams.Subscriber;

import io.reactivex.common.RxJavaCommonPlugins;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.Function;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.internal.subscriptions.BooleanSubscription;
import io.reactivex.flowable.processors.PublishProcessor;
import io.reactivex.interop.TestHelper;
import io.reactivex.observable.*;

public class FlowableElementAtTest {

    @Test(expected = IndexOutOfBoundsException.class)
    public void testElementAtWithMinusIndex() {
        elementAt(Flowable.fromArray(1, 2), -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testElementAtOrDefaultWithMinusIndex() {
        elementAt(Flowable.fromArray(1, 2), -1, 0);
    }

    @Test
    public void testElementAt() {
        assertEquals(2, elementAt(Flowable.fromArray(1, 2), 1).blockingGet()
                .intValue());
    }

    @Test
    public void testElementAtWithIndexOutOfBounds() {
        assertNull(elementAt(Flowable.fromArray(1, 2), 2).blockingGet());
    }

    @Test
    public void testElementAtOrDefault() {
        assertEquals(2, elementAt(Flowable.fromArray(1, 2), 1, 0).blockingGet().intValue());
    }

    @Test
    public void testElementAtOrDefaultWithIndexOutOfBounds() {
        assertEquals(0, elementAt(Flowable.fromArray(1, 2), 2, 0).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrErrorNegativeIndex() {
        elementAtOrError(Flowable.empty(), -1);
    }

    @Test
    public void elementAtOrErrorNoElement() {
        elementAtOrError(Flowable.empty(), 0)
        .test()
        .assertNoValues()
        .assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorOneElement() {
        elementAtOrError(Flowable.just(1), 0)
        .test()
        .assertNoErrors()
        .assertValue(1);
    }

    @Test
    public void elementAtOrErrorMultipleElements() {
        elementAtOrError(Flowable.just(1, 2, 3), 1)
            .test()
            .assertNoErrors()
            .assertValue(2);
    }

    @Test
    public void elementAtOrErrorInvalidIndex() {
        elementAtOrError(Flowable.just(1, 2, 3), 3)
        .test()
        .assertNoValues()
        .assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorError() {
        elementAtOrError(Flowable.error(new RuntimeException("error")), 0)
        .test()
        .assertNoValues()
        .assertErrorMessage("error")
        .assertError(RuntimeException.class);
    }

    @Test
    public void elementAtIndex0OnEmptySource() {
        elementAt(Flowable.empty(), 0)
        .test()
        .assertResult();
    }

    @Test
    public void elementAtIndex0WithDefaultOnEmptySource() {
        elementAt(Flowable.empty(), 0, 5)
        .test()
        .assertResult(5);
    }

    @Test
    public void elementAtIndex1OnEmptySource() {
        elementAt(Flowable.empty(), 1)
        .test()
        .assertResult();
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySource() {
        elementAt(Flowable.empty(), 1, 10)
        .test()
        .assertResult(10);
    }

    @Test
    public void elementAtOrErrorIndex1OnEmptySource() {
        elementAtOrError(Flowable.empty(), 1)
        .test()
        .assertFailure(NoSuchElementException.class);
    }


    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Object>, Maybe<Object>>() {
            @Override
            public Maybe<Object> apply(Flowable<Object> o) throws Exception {
                return elementAt(o, 0);
            }
        });

        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<Object>>() {
            @Override
            public Single<Object> apply(Flowable<Object> o) throws Exception {
                return elementAt(o, 0, 1);
            }
        });
    }

    @Test
    public void error() {
        elementAt(Flowable.error(new TestException()), 1, 10)
        .test()
        .assertFailure(TestException.class);

        elementAt(Flowable.error(new TestException()), 1)
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {
            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return elementAt(f, 0);
            }
        }, false, null, 1);

        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {
            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return elementAt(f, 0, 1);
            }
        }, false, null, 1, 1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(elementAt(PublishProcessor.create(), 0));
        TestHelper.checkDisposed(elementAt(PublishProcessor.create(), 0, 1));
    }

    @Test
    public void badSource2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            elementAt(new Flowable<Integer>() {
                @Override
                protected void subscribeActual(Subscriber<? super Integer> observer) {
                    observer.onSubscribe(new BooleanSubscription());

                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }, 0, 1)
            .test()
            .assertResult(1);

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }
}
