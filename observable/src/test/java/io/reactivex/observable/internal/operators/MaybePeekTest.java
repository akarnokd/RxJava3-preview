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

import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.Functions;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.PublishSubject;

public class MaybePeekTest {

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.create().singleElement().doOnSuccess(Functions.emptyConsumer()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {
            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.doOnSuccess(Functions.emptyConsumer());
            }
        });
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();

        final Throwable[] err = { null };

        try {
            TestObserver<Integer> to = new Maybe<Integer>() {
                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                    observer.onSubscribe(Disposables.empty());
                    observer.onError(new TestException("First"));
                    observer.onError(new TestException("Second"));
                }
            }
            .doOnError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable e) throws Exception {
                    err[0] = e;
                }
            })
            .test();

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class, "Second");

            assertTrue("" + err, err[0] instanceof TestException);
            assertEquals("First", err[0].getMessage());

            to.assertFailureAndMessage(TestException.class, "First");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void doubleComplete() {
        final int[] compl = { 0 };

        TestObserver<Integer> to = new Maybe<Integer>() {
            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposables.empty());
                observer.onComplete();
                observer.onComplete();
            }
        }
        .doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                compl[0]++;
            }
        })
        .test();

        assertEquals(1, compl[0]);

        to.assertResult();
    }

    @Test
    public void doOnErrorThrows() {
        TestObserver<Object> to = Maybe.error(new TestException("Main"))
        .doOnError(new Consumer<Object>() {
            @Override
            public void accept(Object t) throws Exception {
                throw new TestException("Inner");
            }
        })
        .test();

        to.assertFailure(CompositeException.class);

        List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

        TestCommonHelper.assertError(errors, 0, TestException.class, "Main");
        TestCommonHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void afterTerminateThrows() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();

        try {

            Maybe.just(1)
            .doAfterTerminate(new Action() {
                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            })
            .test()
            .assertResult(1);

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }
}
