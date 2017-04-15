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

package io.reactivex.common.internal.utils;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.functions.BiPredicate;
import io.reactivex.common.internal.utils.AbstractAppendOnlyLinkedArrayList.NonThrowingPredicate;

public class MiscUtilTest {
    @Test
    public void pow2UtilityClass() {
        TestCommonHelper.checkUtilityClass(Pow2.class);
    }

    @Test
    public void isPowerOf2() {
        for (int i = 1; i > 0; i *= 2) {
            assertTrue(Pow2.isPowerOfTwo(i));
        }

        assertFalse(Pow2.isPowerOfTwo(3));
        assertFalse(Pow2.isPowerOfTwo(5));
        assertFalse(Pow2.isPowerOfTwo(6));
        assertFalse(Pow2.isPowerOfTwo(7));
    }

    @Test
    public void hashMapSupplier() {
        TestCommonHelper.checkEnum(HashMapSupplier.class);
    }

    @Test
    public void arrayListSupplier() {
        TestCommonHelper.checkEnum(ArrayListSupplier.class);
    }

    @Test
    public void errorModeEnum() {
        TestCommonHelper.checkEnum(ErrorMode.class);
    }

    @Test
    public void linkedArrayList() {
        LinkedArrayList list = new LinkedArrayList(2);
        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals("[1, 2, 3]", list.toString());
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhile() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(2) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(2, new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer t1, Integer t2) throws Exception {
                out.add(t2);
                return t1.equals(t2);
            }
        });

        assertEquals(Arrays.asList(1, 2), out);
    }


    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhilePreGrow() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(12) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(new NonThrowingPredicate<Integer>() {
            @Override
            public boolean test(Integer t2) {
                out.add(t2);
                return t2 == 2;
            }
        });

        assertEquals(Arrays.asList(1, 2), out);
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhileExact() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(3) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(new NonThrowingPredicate<Integer>() {
            @Override
            public boolean test(Integer t2) {
                out.add(t2);
                return t2 == 2;
            }
        });

        assertEquals(Arrays.asList(1, 2), out);
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhileAll() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(2) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(new NonThrowingPredicate<Integer>() {
            @Override
            public boolean test(Integer t2) {
                out.add(t2);
                return t2 == 3;
            }
        });

        assertEquals(Arrays.asList(1, 2, 3), out);
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhileBigger() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(4) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(new NonThrowingPredicate<Integer>() {
            @Override
            public boolean test(Integer t2) {
                out.add(t2);
                return false;
            }
        });

        assertEquals(Arrays.asList(1, 2, 3), out);
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhileBiPreGrow() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(12) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(2, new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer t1, Integer t2) throws Exception {
                out.add(t2);
                return t1.equals(t2);
            }
        });

        assertEquals(Arrays.asList(1, 2), out);
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhileBiExact() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(3) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(2, new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer t1, Integer t2) throws Exception {
                out.add(t2);
                return t1.equals(t2);
            }
        });

        assertEquals(Arrays.asList(1, 2), out);
    }

    @Test
    public void AbstractAppendOnlyLinkedArrayListForEachWhileBiAll() throws Exception {
        AbstractAppendOnlyLinkedArrayList<Integer> list = new AbstractAppendOnlyLinkedArrayList<Integer>(2) { };

        list.add(1);
        list.add(2);
        list.add(3);

        final List<Integer> out = new ArrayList<Integer>();

        list.forEachWhile(3, new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer t1, Integer t2) throws Exception {
                out.add(t2);
                return false;
            }
        });

        assertEquals(Arrays.asList(1, 2, 3), out);
    }
}
