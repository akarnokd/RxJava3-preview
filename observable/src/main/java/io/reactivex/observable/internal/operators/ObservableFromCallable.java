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

import java.util.concurrent.Callable;

import io.reactivex.common.RxJavaCommonPlugins;
import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.observable.*;
import io.reactivex.observable.internal.observers.DeferredScalarDisposable;

/**
 * Calls a Callable and emits its resulting single value or signals its exception.
 * @param <T> the value type
 */
public final class ObservableFromCallable<T> extends Observable<T> implements Callable<T> {
    final Callable<? extends T> callable;
    public ObservableFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }
    @Override
    public void subscribeActual(Observer<? super T> s) {
        DeferredScalarDisposable<T> d = new DeferredScalarDisposable<T>(s);
        s.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        T value;
        try {
            value = ObjectHelper.requireNonNull(callable.call(), "Callable returned null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                s.onError(e);
            } else {
                RxJavaCommonPlugins.onError(e);
            }
            return;
        }
        d.complete(value);
    }

    @Override
    public T call() throws Exception {
        return callable.call();
    }
}
