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

import org.junit.*;
import static io.reactivex.interop.RxJava3Interop.*;

import io.reactivex.common.functions.Function;
import io.reactivex.flowable.Flowable;
import io.reactivex.interop.TestHelper;
import io.reactivex.observable.SingleSource;

public class FlowableCountTest {
    @Test
    public void simple() {
        Assert.assertEquals(0, count(Flowable.empty()).blockingGet().intValue());

        Assert.assertEquals(1, count(Flowable.just(1)).blockingGet().intValue());

        Assert.assertEquals(10, count(Flowable.range(1, 10)).blockingGet().intValue());

    }


    @Test
    public void dispose() {
        TestHelper.checkDisposed(count(Flowable.just(1)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, SingleSource<Long>>() {
            @Override
            public SingleSource<Long> apply(Flowable<Object> o) throws Exception {
                return count(o);
            }
        });
    }

}
