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

package io.reactivex.observable.internal.operators;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import io.reactivex.common.TestCommonHelper;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.Function;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.PublishSubject;

public class MaybeDelayOtherTest {

    @Test
    public void justWithOnNext() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.just(1)
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onNext(1);

        assertFalse(pp.hasObservers());

        ts.assertResult(1);
    }

    @Test
    public void justWithOnComplete() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.just(1)
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onComplete();

        assertFalse(pp.hasObservers());

        ts.assertResult(1);
    }


    @Test
    public void justWithOnError() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.just(1)
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onError(new TestException("Other"));

        assertFalse(pp.hasObservers());

        ts.assertFailureAndMessage(TestException.class, "Other");
    }

    @Test
    public void emptyWithOnNext() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.<Integer>empty()
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onNext(1);

        assertFalse(pp.hasObservers());

        ts.assertResult();
    }


    @Test
    public void emptyWithOnComplete() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.<Integer>empty()
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onComplete();

        assertFalse(pp.hasObservers());

        ts.assertResult();
    }

    @Test
    public void emptyWithOnError() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.<Integer>empty()
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onError(new TestException("Other"));

        assertFalse(pp.hasObservers());

        ts.assertFailureAndMessage(TestException.class, "Other");
    }

    @Test
    public void errorWithOnNext() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.<Integer>error(new TestException("Main"))
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onNext(1);

        assertFalse(pp.hasObservers());

        ts.assertFailureAndMessage(TestException.class, "Main");
    }

    @Test
    public void errorWithOnComplete() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.<Integer>error(new TestException("Main"))
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onComplete();

        assertFalse(pp.hasObservers());

        ts.assertFailureAndMessage(TestException.class, "Main");
    }

    @Test
    public void errorWithOnError() {
        PublishSubject<Object> pp = PublishSubject.create();

        TestObserver<Integer> ts = Maybe.<Integer>error(new TestException("Main"))
        .delay(pp).test();

        ts.assertEmpty();

        assertTrue(pp.hasObservers());

        pp.onError(new TestException("Other"));

        assertFalse(pp.hasObservers());

        ts.assertFailure(CompositeException.class);

        List<Throwable> list = TestCommonHelper.compositeList(ts.errors().get(0));
        assertEquals(2, list.size());

        TestCommonHelper.assertError(list, 0, TestException.class, "Main");
        TestCommonHelper.assertError(list, 1, TestException.class, "Other");
    }

    @Test
    public void withCompletableDispose() {
        TestHelper.checkDisposed(Completable.complete().andThen(Maybe.just(1)));
    }

    @Test
    public void withCompletableDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToMaybe(new Function<Completable, MaybeSource<Integer>>() {
            @Override
            public MaybeSource<Integer> apply(Completable c) throws Exception {
                return c.andThen(Maybe.just(1));
            }
        });
    }

    @Test
    public void withOtherObservableDispose() {
        TestHelper.checkDisposed(Maybe.just(1).delay(Observable.just(1)));
    }
}
