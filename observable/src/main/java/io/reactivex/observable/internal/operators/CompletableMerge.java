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
import io.reactivex.observable.internal.queues.SpscLinkedArrayQueue;

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

        final CompletableBuffer buffer;

        Disposable s;

        CompletableMergeObserver(CompletableObserver actual, int maxConcurrency, boolean delayErrors) {
            this.actual = actual;
            this.maxConcurrency = maxConcurrency;
            this.delayErrors = delayErrors;
            this.set = new CompositeDisposable();
            this.error = new AtomicThrowable();
            this.buffer = maxConcurrency != Integer.MAX_VALUE ? new CompletableBuffer(maxConcurrency) : null;
            lazySet(1);
        }

        @Override
        public void dispose() {
            s.dispose();
            set.dispose();
            CompletableBuffer b = buffer;
            if (b != null) {
                b.cancel();
            }
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
            if (maxConcurrency == Integer.MAX_VALUE) {
                onNextActual(t);
            } else {
                buffer.offer(t);
            }
        }

        void onNextActual(CompletableSource t) {
            MergeInnerObserver inner = new MergeInnerObserver();
            set.add(inner);
            t.subscribe(inner);
        }

        @Override
        public void onError(Throwable t) {
            if (!delayErrors) {
                set.dispose();
                CompletableBuffer b = buffer;
                if (b != null) {
                    b.cancel();
                }

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
                CompletableBuffer b = buffer;
                if (b != null) {
                    b.cancel();
                }

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
                            buffer.request();
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
                    buffer.request();
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

        final class CompletableBuffer extends AtomicInteger {

            private static final long serialVersionUID = -6105068104477470875L;

            final AtomicLong requested = new AtomicLong();

            volatile boolean cancelled;

            final SpscLinkedArrayQueue<CompletableSource> queue =
                    new SpscLinkedArrayQueue<CompletableSource>(Observable.bufferSize());

            CompletableBuffer(int initialRequest) {
                requested.lazySet(initialRequest);
            }

            void offer(CompletableSource t) {
                queue.offer(t);
                drain();
            }

            void request() {
                requested.getAndIncrement();
                drain();
            }

            void drain() {
                if (getAndIncrement() == 0) {
                    int missed = 1;
                    for (;;) {
                        long e = 0L;
                        long r = requested.get();

                        while (e != r) {
                            if (cancelled) {
                                queue.clear();
                                return;
                            }

                            CompletableSource t = queue.poll();
                            if (t != null) {
                                onNextActual(t);
                                e++;
                            } else {
                                break;
                            }
                        }

                        if (e == r) {
                            if (cancelled) {
                                queue.clear();
                                return;
                            }
                        }

                        if (e != 0) {
                            requested.addAndGet(-e);
                        }

                        missed = addAndGet(-missed);
                        if (missed == 0) {
                            break;
                        }
                    }
                }
            }

            void cancel() {
                cancelled = true;
                if (getAndIncrement() == 0) {
                    queue.clear();
                }
            }
        }
    }
}
