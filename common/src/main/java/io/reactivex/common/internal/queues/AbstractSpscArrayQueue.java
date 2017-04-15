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

import java.util.concurrent.atomic.*;

import io.reactivex.common.annotations.Nullable;
import io.reactivex.common.internal.utils.Pow2;

/**
 * A Single-Producer-Single-Consumer queue backed by a pre-allocated buffer.
 * <p>
 * This implementation is a mashup of the <a href="http://sourceforge.net/projects/mc-fastflow/">Fast Flow</a>
 * algorithm with an optimization of the offer method taken from the <a
 * href="http://staff.ustc.edu.cn/~bhua/publications/IJPP_draft.pdf">BQueue</a> algorithm (a variation on Fast
 * Flow), and adjusted to comply with Queue.offer semantics with regards to capacity.<br>
 * For convenience the relevant papers are available in the resources folder:<br>
 * <i>2010 - Pisa - SPSC Queues on Shared Cache Multi-Core Systems.pdf<br>
 * 2012 - Junchang- BQueue- Efficient and Practical Queuing.pdf <br>
 * </i> This implementation is wait free.
 *
 * @param <E> the element type of the queue
 */
public abstract class AbstractSpscArrayQueue<E> extends AtomicReferenceArray<E> {
    private static final long serialVersionUID = -1296597691183856449L;
    private static final Integer MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.spsc.max.lookahead.step", 4096);
    final int mask;
    final AtomicLong producerIndex;
    long producerLookAhead;
    final AtomicLong consumerIndex;
    final int lookAheadStep;

    public AbstractSpscArrayQueue(int capacity) {
        super(Pow2.roundToPowerOfTwo(capacity));
        this.mask = length() - 1;
        this.producerIndex = new AtomicLong();
        this.consumerIndex = new AtomicLong();
        lookAheadStep = Math.min(capacity / 4, MAX_LOOK_AHEAD_STEP);
    }

    public final boolean offer(E e) {
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        // local load of field to avoid repeated loads after volatile reads
        final int mask = this.mask;
        final long index = producerIndex.get();
        final int offset = calcElementOffset(index, mask);
        if (index >= producerLookAhead) {
            int step = lookAheadStep;
            if (null == lvElement(calcElementOffset(index + step, mask))) { // LoadLoad
                producerLookAhead = index + step;
            } else if (null != lvElement(offset)) {
                return false;
            }
        }
        soElement(offset, e); // StoreStore
        soProducerIndex(index + 1); // ordered store -> atomic and ordered for size()
        return true;
    }

    public final boolean offer(E v1, E v2) {
        // FIXME
        return offer(v1) && offer(v2);
    }

    @Nullable
    public final E poll() {
        final long index = consumerIndex.get();
        final int offset = calcElementOffset(index);
        // local load of field to avoid repeated loads after volatile reads
        final E e = lvElement(offset);// LoadLoad
        if (null == e) {
            return null;
        }
        soConsumerIndex(index + 1); // ordered store -> atomic and ordered for size()
        soElement(offset, null);// StoreStore
        return e;
    }

    public final boolean isEmpty() {
        return producerIndex.get() == consumerIndex.get();
    }

    final void soProducerIndex(long newIndex) {
        producerIndex.lazySet(newIndex);
    }

    final void soConsumerIndex(long newIndex) {
        consumerIndex.lazySet(newIndex);
    }

    public final void clear() {
        // we have to test isEmpty because of the weaker poll() guarantee
        while (poll() != null || !isEmpty()) { } // NOPMD
    }

    final int calcElementOffset(long index, int mask) {
        return (int)index & mask;
    }

    final int calcElementOffset(long index) {
        return (int)index & mask;
    }

    final void soElement(int offset, E value) {
        lazySet(offset, value);
    }

    final E lvElement(int offset) {
        return get(offset);
    }
}

