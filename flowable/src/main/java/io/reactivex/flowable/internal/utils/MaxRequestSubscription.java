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

package io.reactivex.flowable.internal.utils;

import org.reactivestreams.Subscription;

import io.reactivex.common.functions.Consumer;

public final class MaxRequestSubscription implements Consumer<Subscription> {
    public static final Consumer<Subscription> REQUEST_MAX = new MaxRequestSubscription();
    @Override
    public void accept(Subscription t) throws Exception {
        t.request(Long.MAX_VALUE);
    }
}
