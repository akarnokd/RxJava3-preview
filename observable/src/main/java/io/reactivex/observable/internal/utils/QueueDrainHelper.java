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
package io.reactivex.observable.internal.utils;

import io.reactivex.common.Disposable;
import io.reactivex.observable.Observer;
import io.reactivex.observable.extensions.*;
import io.reactivex.observable.internal.queues.*;

/**
 * Utility class to help with the queue-drain serialization idiom.
 */
public final class QueueDrainHelper {
    /** Utility class. */
    private QueueDrainHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Creates a queue: spsc-array if capacityHint is positive and
     * spsc-linked-array if capacityHint is negative; in both cases, the
     * capacity is the absolute value of prefetch.
     * @param <T> the value type of the queue
     * @param capacityHint the capacity hint, negative value will create an array-based SPSC queue
     * @return the queue instance
     */
    public static <T> SimplePlainQueue<T> createQueue(int capacityHint) {
        if (capacityHint < 0) {
            return new SpscLinkedArrayQueue<T>(-capacityHint);
        }
        return new SpscArrayQueue<T>(capacityHint);
    }
    
    public static <T, U> void drainLoop(SimplePlainQueue<T> q, Observer<? super U> a, boolean delayError, Disposable dispose, ObservableQueueDrain<T, U> qd) {

        int missed = 1;

        for (;;) {
            if (checkTerminated(qd.done(), q.isEmpty(), a, delayError, q, dispose, qd)) {
                return;
            }

            for (;;) {
                boolean d = qd.done();
                T v = q.poll();
                boolean empty = v == null;

                if (checkTerminated(d, empty, a, delayError, q, dispose, qd)) {
                    return;
                }

                if (empty) {
                    break;
                }

                qd.accept(a, v);
            }

            missed = qd.leave(-missed);
            if (missed == 0) {
                break;
            }
        }
    }

    public static <T, U> boolean checkTerminated(boolean d, boolean empty,
            Observer<?> s, boolean delayError, SimpleQueue<?> q, Disposable disposable, ObservableQueueDrain<T, U> qd) {
        if (qd.cancelled()) {
            q.clear();
            disposable.dispose();
            return true;
        }

        if (d) {
            if (delayError) {
                if (empty) {
                    disposable.dispose();
                    Throwable err = qd.error();
                    if (err != null) {
                        s.onError(err);
                    } else {
                        s.onComplete();
                    }
                    return true;
                }
            } else {
                Throwable err = qd.error();
                if (err != null) {
                    q.clear();
                    disposable.dispose();
                    s.onError(err);
                    return true;
                } else
                if (empty) {
                    disposable.dispose();
                    s.onComplete();
                    return true;
                }
            }
        }

        return false;
    }

}
