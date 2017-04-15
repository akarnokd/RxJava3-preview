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
package io.reactivex.observable.observers;

import io.reactivex.common.*;
import io.reactivex.common.annotations.NonNull;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.observable.Observer;
import io.reactivex.observable.internal.disposables.EmptyDisposable;

/**
 * Wraps another Subscriber and ensures all onXXX methods conform the protocol
 * (except the requirement for serialized access).
 *
 * @param <T> the value type
 */
public final class SafeObserver<T> implements Observer<T>, Disposable {
    /** The actual Subscriber. */
    final Observer<? super T> actual;
    /** The subscription. */
    Disposable s;
    /** Indicates a terminal state. */
    boolean done;

    /**
     * Constructs a SafeObserver by wrapping the given actual Observer.
     * @param actual the actual Observer to wrap, not null (not validated)
     */
    public SafeObserver(@NonNull Observer<? super T> actual) {
        this.actual = actual;
    }

    @Override
    public void onSubscribe(@NonNull Disposable s) {
        if (DisposableHelper.validate(this.s, s)) {
            this.s = s;
            try {
                actual.onSubscribe(this);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                done = true;
                // can't call onError because the actual's state may be corrupt at this point
                try {
                    s.dispose();
                } catch (Throwable e1) {
                    Exceptions.throwIfFatal(e1);
                    RxJavaCommonPlugins.onError(new CompositeException(e, e1));
                    return;
                }
                RxJavaCommonPlugins.onError(e);
            }
        }
    }


    @Override
    public void dispose() {
        s.dispose();
    }

    @Override
    public boolean isDisposed() {
        return s.isDisposed();
    }

    @Override
    public void onNext(@NonNull T t) {
        if (done) {
            return;
        }
        if (s == null) {
            onNextNoSubscription();
            return;
        }

        if (t == null) {
            Throwable ex = new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
            try {
                s.dispose();
            } catch (Throwable e1) {
                Exceptions.throwIfFatal(e1);
                onError(new CompositeException(ex, e1));
                return;
            }
            onError(ex);
            return;
        }

        try {
            actual.onNext(t);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            try {
                s.dispose();
            } catch (Throwable e1) {
                Exceptions.throwIfFatal(e1);
                onError(new CompositeException(e, e1));
                return;
            }
            onError(e);
        }
    }

    void onNextNoSubscription() {
        done = true;

        Throwable ex = new NullPointerException("Subscription not set!");

        try {
            actual.onSubscribe(EmptyDisposable.INSTANCE);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because the actual's state may be corrupt at this point
            RxJavaCommonPlugins.onError(new CompositeException(ex, e));
            return;
        }
        try {
            actual.onError(ex);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // if onError failed, all that's left is to report the error to plugins
            RxJavaCommonPlugins.onError(new CompositeException(ex, e));
        }
    }

    @Override
    public void onError(@NonNull Throwable t) {
        if (done) {
            RxJavaCommonPlugins.onError(t);
            return;
        }
        done = true;

        if (s == null) {
            Throwable npe = new NullPointerException("Subscription not set!");

            try {
                actual.onSubscribe(EmptyDisposable.INSTANCE);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                // can't call onError because the actual's state may be corrupt at this point
                RxJavaCommonPlugins.onError(new CompositeException(t, npe, e));
                return;
            }
            try {
                actual.onError(new CompositeException(t, npe));
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                // if onError failed, all that's left is to report the error to plugins
                RxJavaCommonPlugins.onError(new CompositeException(t, npe, e));
            }
            return;
        }

        if (t == null) {
            t = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        }

        try {
            actual.onError(t);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);

            RxJavaCommonPlugins.onError(new CompositeException(t, ex));
        }
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }

        done = true;

        if (s == null) {
            onCompleteNoSubscription();
            return;
        }

        try {
            actual.onComplete();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            RxJavaCommonPlugins.onError(e);
        }
    }

    void onCompleteNoSubscription() {

        Throwable ex = new NullPointerException("Subscription not set!");

        try {
            actual.onSubscribe(EmptyDisposable.INSTANCE);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because the actual's state may be corrupt at this point
            RxJavaCommonPlugins.onError(new CompositeException(ex, e));
            return;
        }
        try {
            actual.onError(ex);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // if onError failed, all that's left is to report the error to plugins
            RxJavaCommonPlugins.onError(new CompositeException(ex, e));
        }
    }

}
