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

package io.reactivex.flowable.internal.utils;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;
import org.reactivestreams.Subscriber;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.ProtocolViolationException;
import io.reactivex.flowable.internal.subscriptions.*;
import io.reactivex.flowable.subscribers.*;

public class EndSubscriberHelperTest {

    List<Throwable> errors;

    @Before
    public void before() {
        errors = TestCommonHelper.trackPluginErrors();
    }

    @After
    public void after() {
        RxJavaCommonPlugins.reset();
    }

    @Test
    public void utilityClass() {
        TestCommonHelper.checkUtilityClass(EndSubscriberHelper.class);
    }

    @Test
    public void checkDoubleDefaultSubscriber() {
        Subscriber<Integer> consumer = new DefaultSubscriber<Integer>() {
            @Override
            public void onNext(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        BooleanSubscription sub1 = new BooleanSubscription();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isCancelled());

        BooleanSubscription sub2 = new BooleanSubscription();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isCancelled());

        assertTrue(sub2.isCancelled());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndSubscriberHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    static final class EndDefaultSubscriber extends DefaultSubscriber<Integer> {
        @Override
        public void onNext(Integer t) {
        }
        @Override
        public void onError(Throwable t) {
        }
        @Override
        public void onComplete() {
        }
    }

    @Test
    public void checkDoubleDefaultSubscriberNonAnonymous() {
        Subscriber<Integer> consumer = new EndDefaultSubscriber();

        BooleanSubscription sub1 = new BooleanSubscription();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isCancelled());

        BooleanSubscription sub2 = new BooleanSubscription();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isCancelled());

        assertTrue(sub2.isCancelled());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);

        // with this consumer, the class name should be predictable
        assertEquals(EndSubscriberHelper.composeMessage("io.reactivex.flowable.internal.utils.EndSubscriberHelperTest$EndDefaultSubscriber"), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableSubscriber() {
        Subscriber<Integer> consumer = new DisposableSubscriber<Integer>() {
            @Override
            public void onNext(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        BooleanSubscription sub1 = new BooleanSubscription();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isCancelled());

        BooleanSubscription sub2 = new BooleanSubscription();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isCancelled());

        assertTrue(sub2.isCancelled());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndSubscriberHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceSubscriber() {
        Subscriber<Integer> consumer = new ResourceSubscriber<Integer>() {
            @Override
            public void onNext(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        BooleanSubscription sub1 = new BooleanSubscription();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isCancelled());

        BooleanSubscription sub2 = new BooleanSubscription();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isCancelled());

        assertTrue(sub2.isCancelled());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndSubscriberHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void validateSubscription() {
        BooleanSubscription d1 = new BooleanSubscription();

        assertFalse(EndSubscriberHelper.validate(SubscriptionHelper.CANCELLED, d1, getClass()));

        assertTrue(d1.isCancelled());

        assertTrue(errors.toString(), errors.isEmpty());
    }
}