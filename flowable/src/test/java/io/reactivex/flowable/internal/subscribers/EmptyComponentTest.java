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

package io.reactivex.flowable.internal.subscribers;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.flowable.internal.subscriptions.BooleanSubscription;
import io.reactivex.flowable.internal.utils.EmptyComponent;

public class EmptyComponentTest {

    @Test
    public void normal() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();

        try {
            TestCommonHelper.checkEnum(EmptyComponent.class);

            EmptyComponent c = EmptyComponent.INSTANCE;

            assertTrue(c.isDisposed());

            c.request(10);

            c.request(-10);

            BooleanSubscription s = new BooleanSubscription();

            c.onSubscribe(s);

            assertTrue(s.isCancelled());

            c.onNext(null);

            c.onNext(1);

            c.onComplete();

            c.onError(new TestException());

            c.cancel();

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }
}
