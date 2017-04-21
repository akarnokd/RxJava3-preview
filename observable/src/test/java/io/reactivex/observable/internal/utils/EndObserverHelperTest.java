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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.ProtocolViolationException;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.*;

public class EndObserverHelperTest {

    List<Throwable> errors;

    @Before
    public void before() {
        errors = TestCommonHelper.trackPluginErrors();
    }

    @After
    public void after() {
        RxJavaCommonPlugins.reset();
    }

    @Test
    public void utilityClass() {
        TestCommonHelper.checkUtilityClass(EndObserverHelper.class);
    }

    @Test
    public void checkDoubleDefaultObserver() {
        Observer<Integer> consumer = new DefaultObserver<Integer>() {
            @Override
            public void onNext(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableObserver() {
        Observer<Integer> consumer = new DisposableObserver<Integer>() {
            @Override
            public void onNext(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceObserver() {
        Observer<Integer> consumer = new ResourceObserver<Integer>() {
            @Override
            public void onNext(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableSingleObserver() {
        SingleObserver<Integer> consumer = new DisposableSingleObserver<Integer>() {
            @Override
            public void onSuccess(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceSingleObserver() {
        SingleObserver<Integer> consumer = new ResourceSingleObserver<Integer>() {
            @Override
            public void onSuccess(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableMaybeObserver() {
        MaybeObserver<Integer> consumer = new DisposableMaybeObserver<Integer>() {
            @Override
            public void onSuccess(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceMaybeObserver() {
        MaybeObserver<Integer> consumer = new ResourceMaybeObserver<Integer>() {
            @Override
            public void onSuccess(Integer t) {
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableCompletableObserver() {
        CompletableObserver consumer = new DisposableCompletableObserver() {
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceCompletableObserver() {
        CompletableObserver consumer = new ResourceCompletableObserver() {
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onComplete() {
            }
        };

        Disposable sub1 = Disposables.empty();

        consumer.onSubscribe(sub1);

        assertFalse(sub1.isDisposed());

        Disposable sub2 = Disposables.empty();

        consumer.onSubscribe(sub2);

        assertFalse(sub1.isDisposed());

        assertTrue(sub2.isDisposed());

        TestCommonHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndObserverHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void validateDisposable() {
        Disposable d1 = Disposables.empty();

        assertFalse(EndObserverHelper.validate(DisposableHelper.DISPOSED, d1, getClass()));

        assertTrue(d1.isDisposed());

        assertTrue(errors.toString(), errors.isEmpty());
    }
}