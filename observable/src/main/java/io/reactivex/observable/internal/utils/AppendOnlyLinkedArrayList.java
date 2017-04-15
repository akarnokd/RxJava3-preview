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

import io.reactivex.common.internal.utils.AbstractAppendOnlyLinkedArrayList;
import io.reactivex.observable.Observer;

/**
 * A linked-array-list implementation that only supports appending and consumption.
 *
 * @param <T> the value type
 */
public class AppendOnlyLinkedArrayList<T> extends AbstractAppendOnlyLinkedArrayList<T> {

    /**
     * Constructs an empty list with a per-link capacity.
     * @param capacity the capacity of each link
     */
    public AppendOnlyLinkedArrayList(int capacity) {
        super(capacity);
    }

    /**
     * Interprets the contents as NotificationLite objects and calls
     * the appropriate Observer method.
     * 
     * @param <U> the target type
     * @param observer the observer to emit the events to
     * @return true if a terminal event has been reached
     */
    public <U> boolean accept(Observer<? super U> observer) {
        Object[] a = head;
        final int c = capacity;
        while (a != null) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null) {
                    break;
                }

                if (NotificationLite.acceptFull(o, observer)) {
                    return true;
                }
            }
            a = (Object[])a[c];
        }
        return false;
    }
}
