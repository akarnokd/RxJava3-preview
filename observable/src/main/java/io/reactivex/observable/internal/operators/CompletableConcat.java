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
import io.reactivex.common.exceptions.*;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.observable.*;
import io.reactivex.observable.extensions.*;
import io.reactivex.observable.internal.queues.*;

public final class CompletableConcat extends Completable {
    final ObservableSource<? extends CompletableSource> sources;
    final int prefetch;

    public CompletableConcat(ObservableSource<? extends CompletableSource> sources, int prefetch) {
        this.sources = sources;
        this.prefetch = prefetch;
    }

    @Override
    public void subscribeActual(CompletableObserver s) {
        sources.subscribe(new CompletableConcatSubscriber(s, prefetch));
    }

    static final class CompletableConcatSubscriber
    extends AtomicInteger
    implements Observer<CompletableSource>, Disposable {
        private static final long serialVersionUID = 9032184911934499404L;

        final CompletableObserver actual;

        final int prefetch;

        final ConcatInnerObserver inner;

        final AtomicBoolean once;

        int sourceFused;

        int consumed;

        SimpleQueue<CompletableSource> queue;

        Disposable s;

        volatile boolean done;

        volatile boolean active;

        CompletableConcatSubscriber(CompletableObserver actual, int prefetch) {
            this.actual = actual;
            this.prefetch = prefetch;
            this.inner = new ConcatInnerObserver(this);
            this.once = new AtomicBoolean();
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;

                if (s instanceof QueueDisposable) {
                    @SuppressWarnings("unchecked")
                    QueueDisposable<CompletableSource> qs = (QueueDisposable<CompletableSource>) s;

                    int m = qs.requestFusion(QueueDisposable.ANY);

                    if (m == QueueDisposable.SYNC) {
                        sourceFused = m;
                        queue = qs;
                        done = true;
                        actual.onSubscribe(this);
                        drain();
                        return;
                    }
                    if (m == QueueDisposable.ASYNC) {
                        sourceFused = m;
                        queue = qs;
                        actual.onSubscribe(this);
                        return;
                    }
                }

                if (prefetch == Integer.MAX_VALUE) {
                    queue = new SpscLinkedArrayQueue<CompletableSource>(Observable.bufferSize());
                } else {
                    queue = new SpscArrayQueue<CompletableSource>(prefetch);
                }

                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(CompletableSource t) {
            if (sourceFused == QueueDisposable.NONE) {
                if (!queue.offer(t)) {
                    onError(new MissingBackpressureException());
                    return;
                }
            }
            drain();
        }

        @Override
        public void onError(Throwable t) {
            if (once.compareAndSet(false, true)) {
                DisposableHelper.dispose(inner);
                actual.onError(t);
            } else {
                RxJavaCommonPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @Override
        public void dispose() {
            s.dispose();
            DisposableHelper.dispose(inner);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(inner.get());
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            for (;;) {
                if (isDisposed()) {
                    return;
                }

                if (!active) {

                    boolean d = done;

                    CompletableSource cs;

                    try {
                        cs = queue.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        innerError(ex);
                        return;
                    }

                    boolean empty = cs == null;

                    if (d && empty) {
                        if (once.compareAndSet(false, true)) {
                            actual.onComplete();
                        }
                        return;
                    }

                    if (!empty) {
                        active = true;
                        cs.subscribe(inner);
                    }
                }

                if (decrementAndGet() == 0) {
                    break;
                }
            }
        }

        void innerError(Throwable e) {
            if (once.compareAndSet(false, true)) {
                s.dispose();
                actual.onError(e);
            } else {
                RxJavaCommonPlugins.onError(e);
            }
        }

        void innerComplete() {
            active = false;
            drain();
        }

        static final class ConcatInnerObserver extends AtomicReference<Disposable> implements CompletableObserver {
            private static final long serialVersionUID = -5454794857847146511L;

            final CompletableConcatSubscriber parent;

            ConcatInnerObserver(CompletableConcatSubscriber parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override
            public void onError(Throwable e) {
                parent.innerError(e);
            }

            @Override
            public void onComplete() {
                parent.innerComplete();
            }
        }
    }
}
