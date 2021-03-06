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

package io.reactivex.flowable.internal.operators;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.common.Schedulers;
import io.reactivex.flowable.Flowable;

public class FlowableIntervalTest {

    @Test(timeout = 2000)
    public void cancel() {
        Flowable.interval(1, TimeUnit.MILLISECONDS, Schedulers.trampoline())
        .take(10)
        .test()
        .assertResult(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
    }
}