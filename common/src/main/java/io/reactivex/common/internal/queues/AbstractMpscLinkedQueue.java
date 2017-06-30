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

/*
 * The code was inspired by the similarly named JCTools class:
 * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic
 */

package io.reactivex.common.internal.queues;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.common.annotations.Nullable;

/**
 * A multi-producer single consumer unbounded queue.
 * @param <T> the contained value type
 */
public abstract class AbstractMpscLinkedQueue<T> {
    private final AtomicReference<LinkedQueueNode<T>> producerNode;
    private final AtomicReference<LinkedQueueNode<T>> consumerNode;

    public AbstractMpscLinkedQueue() {
        producerNode = new AtomicReference<LinkedQueueNode<T>>();
        consumerNode = new AtomicReference<LinkedQueueNode<T>>();
        LinkedQueueNode<T> node = new LinkedQueueNode<T>();
        spConsumerNode(node);
        xchgProducerNode(node);// this ensures correct construction: StoreLoad
    }

    public final boolean offer(final T e) {
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        final LinkedQueueNode<T> nextNode = new LinkedQueueNode<T>(e);
        final LinkedQueueNode<T> prevProducerNode = xchgProducerNode(nextNode);
        // Should a producer thread get interrupted here the chain WILL be broken until that thread is resumed
        // and completes the store in prev.next.
        prevProducerNode.soNext(nextNode); // StoreStore
        return true;
    }

    @Nullable
    public final T poll() {
        LinkedQueueNode<T> currConsumerNode = lpConsumerNode(); // don't load twice, it's alright
        LinkedQueueNode<T> nextNode = currConsumerNode.lvNext();
        if (nextNode != null) {
            // we have to null out the value because we are going to hang on to the node
            final T nextValue = nextNode.getAndNullValue();
            spConsumerNode(nextNode);
            return nextValue;
        }
        else if (currConsumerNode != lvProducerNode()) {
            // spin, we are no longer wait free
            while ((nextNode = currConsumerNode.lvNext()) == null) { } // NOPMD
            // got the next node...

            // we have to null out the value because we are going to hang on to the node
            final T nextValue = nextNode.getAndNullValue();
            spConsumerNode(nextNode);
            return nextValue;
        }
        return null;
    }

    public final boolean offer(T v1, T v2) {
        offer(v1);
        offer(v2);
        return true;
    }

    public final void clear() {
        while (poll() != null && !isEmpty()) { } // NOPMD
    }
    final LinkedQueueNode<T> lvProducerNode() {
        return producerNode.get();
    }
    final LinkedQueueNode<T> xchgProducerNode(LinkedQueueNode<T> node) {
        return producerNode.getAndSet(node);
    }
    final LinkedQueueNode<T> lvConsumerNode() {
        return consumerNode.get();
    }

    final LinkedQueueNode<T> lpConsumerNode() {
        return consumerNode.get();
    }
    final void spConsumerNode(LinkedQueueNode<T> node) {
        consumerNode.lazySet(node);
    }

    public final boolean isEmpty() {
        return lvConsumerNode() == lvProducerNode();
    }

    static final class LinkedQueueNode<E> extends AtomicReference<LinkedQueueNode<E>> {

        private static final long serialVersionUID = 2404266111789071508L;

        private E value;

        LinkedQueueNode() {
        }

        LinkedQueueNode(E val) {
            spValue(val);
        }
        /**
         * Gets the current value and nulls out the reference to it from this node.
         *
         * @return value
         */
        public E getAndNullValue() {
            E temp = lpValue();
            spValue(null);
            return temp;
        }

        public E lpValue() {
            return value;
        }

        public void spValue(E newValue) {
            value = newValue;
        }

        public void soNext(LinkedQueueNode<E> n) {
            lazySet(n);
        }

        public LinkedQueueNode<E> lvNext() {
            return get();
        }
    }
}
