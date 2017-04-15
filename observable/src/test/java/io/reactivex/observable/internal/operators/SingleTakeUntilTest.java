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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CancellationException;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.PublishSubject;

public class SingleTakeUntilTest {

    @Test
    public void mainSuccessPublisher() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp)
        .test();

        source.onNext(1);
        source.onComplete();

        ts.assertResult(1);
    }

    @Test
    public void mainSuccessSingle() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.single(-99))
        .test();

        source.onNext(1);
        source.onComplete();

        ts.assertResult(1);
    }


    @Test
    public void mainSuccessCompletable() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.ignoreElements())
        .test();

        source.onNext(1);
        source.onComplete();

        ts.assertResult(1);
    }

    @Test
    public void mainErrorPublisher() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp)
        .test();

        source.onError(new TestException());

        ts.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorSingle() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.single(-99))
        .test();

        source.onError(new TestException());

        ts.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorCompletable() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.ignoreElements())
        .test();

        source.onError(new TestException());

        ts.assertFailure(TestException.class);
    }

    @Test
    public void otherOnNextPublisher() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp)
        .test();

        pp.onNext(1);

        ts.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnNextSingle() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.single(-99))
        .test();

        pp.onNext(1);
        pp.onComplete();

        ts.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnNextCompletable() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.ignoreElements())
        .test();

        pp.onNext(1);
        pp.onComplete();

        ts.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnCompletePublisher() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp)
        .test();

        pp.onComplete();

        ts.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnCompleteCompletable() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.ignoreElements())
        .test();

        pp.onComplete();

        ts.assertFailure(CancellationException.class);
    }

    @Test
    public void otherErrorPublisher() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp)
        .test();

        pp.onError(new TestException());

        ts.assertFailure(TestException.class);
    }

    @Test
    public void otherErrorSingle() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.single(-99))
        .test();

        pp.onError(new TestException());

        ts.assertFailure(TestException.class);
    }

    @Test
    public void otherErrorCompletable() {
        PublishSubject<Integer> pp = PublishSubject.create();
        PublishSubject<Integer> source = PublishSubject.create();

        TestObserver<Integer> ts = source.single(-99).takeUntil(pp.ignoreElements())
        .test();

        pp.onError(new TestException());

        ts.assertFailure(TestException.class);
    }

    @Test
    public void withPublisherDispose() {
        TestHelper.checkDisposed(Single.never().takeUntil(Observable.never()));
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < 500; i++) {
            List<Throwable> errors = TestCommonHelper.trackPluginErrors();

            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();

                TestObserver<Integer> to = ps1.singleOrError().takeUntil(ps2).test();

                final TestException ex = new TestException();

                Runnable r1 = new Runnable() {
                    @Override
                    public void run() {
                        ps1.onError(ex);
                    }
                };

                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        ps2.onError(ex);
                    }
                };

                TestCommonHelper.race(r1, r2, Schedulers.single());

                to.assertFailure(TestException.class);

                if (!errors.isEmpty()) {
                    TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaCommonPlugins.reset();
            }
        }
    }

    @Test
    public void otherSignalsAndCompletes() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Single.just(1).takeUntil(Observable.just(1).take(1))
            .test()
            .assertFailure(CancellationException.class);

            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }
}
