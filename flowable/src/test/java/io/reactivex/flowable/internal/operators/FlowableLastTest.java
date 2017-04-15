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

package io.reactivex.flowable.internal.operators;

import java.util.NoSuchElementException;

import org.junit.Test;

import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.Function;
import io.reactivex.flowable.*;

public class FlowableLastTest {

    @Test
    public void lastOrErrorNoElement() {
        Flowable.empty()
            .lastOrError()
            .test()
            .assertNoValues()
            .assertError(NoSuchElementException.class);
    }

    @Test
    public void lastOrErrorOneElement() {
        Flowable.just(1)
            .lastOrError()
            .test()
            .assertNoErrors()
            .assertValue(1);
    }

    @Test
    public void lastOrErrorMultipleElements() {
        Flowable.just(1, 2, 3)
            .lastOrError()
            .test()
            .assertNoErrors()
            .assertValue(3);
    }

    @Test
    public void lastOrErrorError() {
        Flowable.error(new RuntimeException("error"))
            .lastOrError()
            .test()
            .assertNoValues()
            .assertErrorMessage("error")
            .assertError(RuntimeException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().lastElement());
        TestHelper.checkDisposed(Flowable.never().lastElement());

        TestHelper.checkDisposed(Flowable.just(1).lastOrError());
        TestHelper.checkDisposed(Flowable.just(1).lastOrError());

        TestHelper.checkDisposed(Flowable.just(1).last(2));
        TestHelper.checkDisposed(Flowable.just(1).last(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {
            @Override
            public Flowable<Object> apply(Flowable<Object> o) throws Exception {
                return o.lastElement();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {
            @Override
            public Flowable<Object> apply(Flowable<Object> o) throws Exception {
                return o.last(2);
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException())
        .lastElement()
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void errorLastOrErrorFlowable() {
        Flowable.error(new TestException())
        .lastOrError()
        
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void emptyLastOrErrorFlowable() {
        Flowable.empty()
        .lastOrError()
        
        .test()
        .assertFailure(NoSuchElementException.class);
    }
}
