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

import io.reactivex.common.Disposable;
import io.reactivex.common.exceptions.CompositeException;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.observable.*;

/**
 * Delay the emission of the main signal until the other signals an item or completes.
 * 
 * @param <T> the main value type
 * @param <U> the other value type
 */
public final class MaybeDelayOtherObservable<T, U> extends AbstractMaybeWithUpstream<T, T> {

    final ObservableSource<U> other;

    public MaybeDelayOtherObservable(MaybeSource<T> source, ObservableSource<U> other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new DelayMaybeObserver<T, U>(observer, other));
    }

    static final class DelayMaybeObserver<T, U>
    implements MaybeObserver<T>, Disposable {
        final OtherObserver<T> other;

        final ObservableSource<U> otherSource;

        Disposable d;

        DelayMaybeObserver(MaybeObserver<? super T> actual, ObservableSource<U> otherSource) {
            this.other = new OtherObserver<T>(actual);
            this.otherSource = otherSource;
        }

        @Override
        public void dispose() {
            d.dispose();
            d = DisposableHelper.DISPOSED;
            DisposableHelper.dispose(other);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(other.get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.d, d)) {
                this.d = d;

                other.actual.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            d = DisposableHelper.DISPOSED;
            other.value = value;
            subscribeNext();
        }

        @Override
        public void onError(Throwable e) {
            d = DisposableHelper.DISPOSED;
            other.error = e;
            subscribeNext();
        }

        @Override
        public void onComplete() {
            d = DisposableHelper.DISPOSED;
            subscribeNext();
        }

        void subscribeNext() {
            otherSource.subscribe(other);
        }
    }

    static final class OtherObserver<T> extends
    AtomicReference<Disposable>
    implements Observer<Object> {

        private static final long serialVersionUID = -1215060610805418006L;

        final MaybeObserver<? super T> actual;

        T value;

        Throwable error;

        OtherObserver(MaybeObserver<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onSubscribe(Disposable s) {
            DisposableHelper.setOnce(this, s);
        }

        @Override
        public void onNext(Object t) {
            Disposable s = get();
            if (s != DisposableHelper.DISPOSED) {
                lazySet(DisposableHelper.DISPOSED);
                s.dispose();
                onComplete();
            }
        }

        @Override
        public void onError(Throwable t) {
            Throwable e = error;
            if (e == null) {
                actual.onError(t);
            } else {
                actual.onError(new CompositeException(e, t));
            }
        }

        @Override
        public void onComplete() {
            Throwable e = error;
            if (e != null) {
                actual.onError(e);
            } else {
                T v = value;
                if (v != null) {
                    actual.onSuccess(v);
                } else {
                    actual.onComplete();
                }
            }
        }
    }
}
