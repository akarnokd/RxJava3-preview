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

import java.util.concurrent.atomic.*;

import io.reactivex.common.*;
import io.reactivex.common.internal.disposables.SequentialDisposable;
import io.reactivex.common.internal.utils.AtomicThrowable;
import io.reactivex.observable.*;
import io.reactivex.observable.internal.utils.NotificationLite;

/**
 * Concatenate values of each MaybeSource provided in an array and delays
 * any errors till the very end.
 *
 * @param <T> the value type
 */
public final class MaybeConcatArrayDelayError<T> extends Observable<T> {

    final MaybeSource<? extends T>[] sources;

    public MaybeConcatArrayDelayError(MaybeSource<? extends T>[] sources) {
        this.sources = sources;
    }

    @Override
    protected void subscribeActual(Observer<? super T> s) {
        ConcatMaybeObserver<T> parent = new ConcatMaybeObserver<T>(s, sources);
        s.onSubscribe(parent);
        parent.drain();
    }

    static final class ConcatMaybeObserver<T>
    extends AtomicInteger
    implements MaybeObserver<T>, Disposable {

        private static final long serialVersionUID = 3520831347801429610L;

        final Observer<? super T> actual;

        final AtomicReference<Object> current;

        final SequentialDisposable disposables;

        final MaybeSource<? extends T>[] sources;

        final AtomicThrowable errors;

        int index;

        ConcatMaybeObserver(Observer<? super T> actual, MaybeSource<? extends T>[] sources) {
            this.actual = actual;
            this.sources = sources;
            this.disposables = new SequentialDisposable();
            this.current = new AtomicReference<Object>(NotificationLite.COMPLETE); // as if a previous completed
            this.errors = new AtomicThrowable();
        }

        @Override
        public void dispose() {
            disposables.dispose();
        }
        
        @Override
        public boolean isDisposed() {
            return disposables.isDisposed();
        }

        @Override
        public void onSubscribe(Disposable d) {
            disposables.replace(d);
        }

        @Override
        public void onSuccess(T value) {
            current.lazySet(value);
            drain();
        }

        @Override
        public void onError(Throwable e) {
            current.lazySet(NotificationLite.COMPLETE);
            if (errors.addThrowable(e)) {
                drain();
            } else {
                RxJavaCommonPlugins.onError(e);
            }
        }

        @Override
        public void onComplete() {
            current.lazySet(NotificationLite.COMPLETE);
            drain();
        }

        @SuppressWarnings("unchecked")
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            AtomicReference<Object> c = current;
            Observer<? super T> a = actual;
            Disposable cancelled = disposables;

            for (;;) {
                if (cancelled.isDisposed()) {
                    c.lazySet(null);
                    return;
                }

                Object o = c.get();

                if (o != null) {
                    if (o != NotificationLite.COMPLETE) {
                        c.lazySet(null);

                        a.onNext((T)o);
                    } else {
                        c.lazySet(null);
                    }

                    if (!cancelled.isDisposed()) {
                        int i = index;
                        if (i == sources.length) {
                            Throwable ex = errors.get();
                            if (ex != null) {
                                a.onError(errors.terminate());
                            } else {
                                a.onComplete();
                            }
                            return;
                        }
                        index = i + 1;

                        sources[i].subscribe(this);
                    }
                }

                if (decrementAndGet() == 0) {
                    break;
                }
            }
        }
    }
}
