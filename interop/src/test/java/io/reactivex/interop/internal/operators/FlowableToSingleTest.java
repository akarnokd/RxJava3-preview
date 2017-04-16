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

import static io.reactivex.interop.RxJava3Interop.single;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;

import io.reactivex.common.functions.Action;
import io.reactivex.flowable.Flowable;
import io.reactivex.observable.Single;
import io.reactivex.observable.observers.TestObserver;

public class FlowableToSingleTest {

    @Test
    public void testJustSingleItemObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        Single<String> single = single(Flowable.just("Hello World!"), "");
        single.subscribe(subscriber);

        subscriber.assertResult("Hello World!");
    }

    @Test
    public void testErrorObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        IllegalArgumentException error = new IllegalArgumentException("Error");
        Single<String> single = single(Flowable.<String>error(error), "");
        single.subscribe(subscriber);

        subscriber.assertError(error);
    }

    @Test
    public void testJustTwoEmissionsObservableThrowsError() {
        TestObserver<String> subscriber = TestObserver.create();
        Single<String> single = single(Flowable.just("First", "Second"), "");
        single.subscribe(subscriber);

        subscriber.assertError(IllegalArgumentException.class);
    }

    @Test
    public void testEmptyObservable() {
        TestObserver<String> subscriber = TestObserver.create();
        Single<String> single = single(Flowable.<String>empty(), "");
        single.subscribe(subscriber);

        subscriber.assertResult("");
    }

    @Test
    public void testRepeatObservableThrowsError() {
        TestObserver<String> subscriber = TestObserver.create();
        Single<String> single = single(Flowable.just("First", "Second").repeat(), "");
        single.subscribe(subscriber);

        subscriber.assertError(IllegalArgumentException.class);
    }

    @Test
    public void testShouldUseUnsafeSubscribeInternallyNotSubscribe() {
        TestObserver<String> subscriber = TestObserver.create();
        final AtomicBoolean unsubscribed = new AtomicBoolean(false);
        Single<String> single = single(Flowable.just("Hello World!").doOnCancel(new Action() {

            @Override
            public void run() {
                unsubscribed.set(true);
            }}), "");
        single.subscribe(subscriber);
        subscriber.assertComplete();
        Assert.assertFalse(unsubscribed.get());
    }
}
