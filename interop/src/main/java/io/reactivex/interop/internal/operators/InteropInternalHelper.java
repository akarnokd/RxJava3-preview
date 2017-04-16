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

package io.reactivex.interop.internal.operators;

import org.reactivestreams.Publisher;

import io.reactivex.common.functions.Function;
import io.reactivex.observable.SingleSource;

public final class InteropInternalHelper {

    private InteropInternalHelper() {
        throw new IllegalStateException("No instances!");
    }

    @SuppressWarnings("rawtypes")
    enum ToFlowable implements Function<SingleSource, Publisher> {
        INSTANCE;
        @SuppressWarnings("unchecked")
        @Override
        public Publisher apply(SingleSource v) {
            return new SingleToFlowable(v);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Function<SingleSource<? extends T>, Publisher<? extends T>> toFlowable() {
        return (Function)ToFlowable.INSTANCE;
    }

}
