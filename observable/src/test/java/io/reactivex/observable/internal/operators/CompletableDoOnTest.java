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
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.*;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;

public class CompletableDoOnTest {

    @Test
    public void successAcceptThrows() {
        Completable.complete().doOnEvent(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                throw new TestException();
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void errorAcceptThrows() {
        TestObserver<Void> to = Completable.error(new TestException("Outer")).doOnEvent(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        })
        .test()
        .assertFailure(CompositeException.class);

        List<Throwable> errors = TestCommonHelper.compositeList(to.errors().get(0));

        TestCommonHelper.assertError(errors, 0, TestException.class, "Outer");
        TestCommonHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnDisposeCalled() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();

        assertFalse(atomicBoolean.get());

        Completable.complete()
            .doOnDispose(new Action() {
                @Override
                public void run() throws Exception {
                    atomicBoolean.set(true);
                }
            })
            .test()
            .assertResult()
            .dispose();

        assertTrue(atomicBoolean.get());
    }

    @Test
    public void onSubscribeCrash() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            final Disposable bs = Disposables.empty();

            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    s.onSubscribe(bs);
                    s.onError(new TestException("Second"));
                    s.onComplete();
                }
            }
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable s) throws Exception {
                    throw new TestException("First");
                }
            })
            .test()
            .assertFailureAndMessage(TestException.class, "First");

            assertTrue(bs.isDisposed());

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }
}
