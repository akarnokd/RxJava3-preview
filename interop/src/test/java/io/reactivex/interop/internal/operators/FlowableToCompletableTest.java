/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.interop.internal.operators;

import static io.reactivex.interop.RxJava3Interop.*;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.reactivex.common.functions.Action;
import io.reactivex.flowable.Flowable;
import io.reactivex.observable.Completable;
import io.reactivex.observable.observers.TestObserver;

public class FlowableToCompletableTest {

    @Test
    public void testJustSingleItemObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        Completable cmp = ignoreElements(Flowable.just("Hello World!"));
        cmp.subscribe(subscriber);

        subscriber.assertNoValues();
        subscriber.assertComplete();
        subscriber.assertNoErrors();
    }

    @Test
    public void testErrorObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        IllegalArgumentException error = new IllegalArgumentException("Error");
        Completable cmp = ignoreElements(Flowable.<String>error(error));
        cmp.subscribe(subscriber);

        subscriber.assertError(error);
        subscriber.assertNoValues();
    }

    @Test
    public void testJustTwoEmissionsObservableThrowsError() {
        TestObserver<String> subscriber = TestObserver.create();
        Completable cmp = ignoreElements(Flowable.just("First", "Second"));
        cmp.subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertNoValues();
    }

    @Test
    public void testEmptyObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        Completable cmp = ignoreElements(Flowable.<String>empty());
        cmp.subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertComplete();
    }

    @Test
    public void testNeverObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        Completable cmp = ignoreElements(Flowable.<String>never());
        cmp.subscribe(subscriber);

        subscriber.assertNotTerminated();
        subscriber.assertNoValues();
    }

    @Test
    public void testShouldUseUnsafeSubscribeInternallyNotSubscribe() {
        TestObserver<String> subscriber = TestObserver.create();
        final AtomicBoolean unsubscribed = new AtomicBoolean(false);
        Completable cmp = ignoreElements(Flowable.just("Hello World!").doOnCancel(new Action() {

            @Override
            public void run() {
                unsubscribed.set(true);
            }}));

        cmp.subscribe(subscriber);

        subscriber.assertComplete();

        assertFalse(unsubscribed.get());
    }
}
