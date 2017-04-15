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



import java.util.concurrent.*;

import org.junit.Test;

import io.reactivex.common.Schedulers;
import io.reactivex.observable.*;

public class SingleFromTest {

    @Test
    public void fromFuture() throws Exception {
        Single.fromFuture(Observable.just(1).toFuture(), Schedulers.io())
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(1);
    }

    @Test
    public void fromFutureTimeout() throws Exception {
        Single.fromFuture(Observable.never().toFuture(), 1, TimeUnit.SECONDS, Schedulers.io())
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertFailure(TimeoutException.class);
    }
}
