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
import io.reactivex.common.disposables.CompositeDisposable;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.common.internal.utils.AtomicThrowable;
import io.reactivex.observable.*;

public final class CompletableMerge extends Completable {
    final ObservableSource<? extends CompletableSource> source;
    final int maxConcurrency;
    final boolean delayErrors;

    public CompletableMerge(ObservableSource<? extends CompletableSource> source, int maxConcurrency, boolean delayErrors) {
        this.source = source;
        this.maxConcurrency = maxConcurrency;
        this.delayErrors = delayErrors;
    }

    @Override
    public void subscribeActual(CompletableObserver s) {
        CompletableMergeObserver parent = new CompletableMergeObserver(s, maxConcurrency, delayErrors);
        source.subscribe(parent);
    }

    static final class CompletableMergeObserver
    extends AtomicInteger
    implements Observer<CompletableSource>, Disposable {

        private static final long serialVersionUID = -2108443387387077490L;

        final CompletableObserver actual;
        final int maxConcurrency;
        final boolean delayErrors;

        final AtomicThrowable error;

        final CompositeDisposable set;

        Disposable s;

        CompletableMergeObserver(CompletableObserver actual, int maxConcurrency, boolean delayErrors) {
            this.actual = actual;
            this.maxConcurrency = maxConcurrency;
            this.delayErrors = delayErrors;
            this.set = new CompositeDisposable();
            this.error = new AtomicThrowable();
            lazySet(1);
        }

        @Override
        public void dispose() {
            s.dispose();
            set.dispose();
        }

        @Override
        public boolean isDisposed() {
            return set.isDisposed();
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;
                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(CompletableSource t) {
            getAndIncrement();

            MergeInnerObserver inner = new MergeInnerObserver();
            set.add(inner);
            t.subscribe(inner);
        }

        @Override
        public void onError(Throwable t) {
            if (!delayErrors) {
                set.dispose();

                if (error.addThrowable(t)) {
                    if (getAndSet(0) > 0) {
                        actual.onError(error.terminate());
                    }
                } else {
                    RxJavaCommonPlugins.onError(t);
                }
            } else {
                if (error.addThrowable(t)) {
                    if (decrementAndGet() == 0) {
                        actual.onError(error.terminate());
                    }
                } else {
                    RxJavaCommonPlugins.onError(t);
                }
            }
        }

        @Override
        public void onComplete() {
            if (decrementAndGet() == 0) {
                Throwable ex = error.get();
                if (ex != null) {
                    actual.onError(error.terminate());
                } else {
                    actual.onComplete();
                }
            }
        }

        void innerError(MergeInnerObserver inner, Throwable t) {
            set.delete(inner);
            if (!delayErrors) {
                s.dispose();
                set.dispose();

                if (error.addThrowable(t)) {
                    if (getAndSet(0) > 0) {
                        actual.onError(error.terminate());
                    }
                } else {
                    RxJavaCommonPlugins.onError(t);
                }
            } else {
                if (error.addThrowable(t)) {
                    if (decrementAndGet() == 0) {
                        actual.onError(error.terminate());
                    } else {
                        if (maxConcurrency != Integer.MAX_VALUE) {
                            // FIXME proper maxConcurrency
                        }
                    }
                } else {
                    RxJavaCommonPlugins.onError(t);
                }
            }
        }

        void innerComplete(MergeInnerObserver inner) {
            set.delete(inner);
            if (decrementAndGet() == 0) {
                Throwable ex = error.get();
                if (ex != null) {
                    actual.onError(ex);
                } else {
                    actual.onComplete();
                }
            } else {
                if (maxConcurrency != Integer.MAX_VALUE) {
                    // FIXME proper maxConcurrency
                }
            }
        }

        final class MergeInnerObserver
        extends AtomicReference<Disposable>
        implements CompletableObserver, Disposable {
            private static final long serialVersionUID = 251330541679988317L;

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onError(Throwable e) {
                innerError(this, e);
            }

            @Override
            public void onComplete() {
                innerComplete(this);
            }

            @Override
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }

            @Override
            public void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
