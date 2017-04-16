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

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.common.*;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.observable.*;

/**
 * Delay the subscription to the main Maybe until the other signals an item or completes.
 * 
 * @param <T> the main value type
 * @param <U> the other value type
 */
public final class MaybeDelaySubscriptionOtherObservable<T, U> extends AbstractMaybeWithUpstream<T, T> {

    final ObservableSource<U> other;

    public MaybeDelaySubscriptionOtherObservable(MaybeSource<T> source, ObservableSource<U> other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        other.subscribe(new OtherObserver<T>(observer, source));
    }

    static final class OtherObserver<T> implements Observer<Object>, Disposable {
        final DelayMaybeObserver<T> main;

        MaybeSource<T> source;

        Disposable s;

        OtherObserver(MaybeObserver<? super T> actual, MaybeSource<T> source) {
            this.main = new DelayMaybeObserver<T>(actual);
            this.source = source;
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;

                main.actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(Object t) {
            if (s != DisposableHelper.DISPOSED) {
                s.dispose();
                s = DisposableHelper.DISPOSED;

                subscribeNext();
            }
        }

        @Override
        public void onError(Throwable t) {
            if (s != DisposableHelper.DISPOSED) {
                s = DisposableHelper.DISPOSED;

                main.actual.onError(t);
            } else {
                RxJavaCommonPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (s != DisposableHelper.DISPOSED) {
                s = DisposableHelper.DISPOSED;

                subscribeNext();
            }
        }

        void subscribeNext() {
            MaybeSource<T> src = source;
            source = null;

            src.subscribe(main);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(main.get());
        }

        @Override
        public void dispose() {
            s.dispose();
            s = DisposableHelper.DISPOSED;
            DisposableHelper.dispose(main);
        }
    }

    static final class DelayMaybeObserver<T> extends AtomicReference<Disposable>
    implements MaybeObserver<T> {

        private static final long serialVersionUID = 706635022205076709L;

        final MaybeObserver<? super T> actual;

        DelayMaybeObserver(MaybeObserver<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onSuccess(T value) {
            actual.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
        }

        @Override
        public void onComplete() {
            actual.onComplete();
        }
    }
}
