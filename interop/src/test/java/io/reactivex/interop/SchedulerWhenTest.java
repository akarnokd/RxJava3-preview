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

package io.reactivex.interop;

import static io.reactivex.flowable.Flowable.just;
import static io.reactivex.flowable.Flowable.merge;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.functions.Function;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.subscribers.TestSubscriber;
import io.reactivex.interop.internal.operators.SchedulerWhen;
import io.reactivex.observable.Completable;

public class SchedulerWhenTest {
    @Test
    public void testAsyncMaxConcurrent() {
        TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = maxConcurrentScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        asyncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);

        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(0);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    @Test
    public void testAsyncDelaySubscription() {
        final TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = throttleScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        asyncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);

        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(0);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(1);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(1);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    @Test
    public void testSyncMaxConcurrent() {
        TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = maxConcurrentScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        syncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);
        tSched.advanceTimeBy(0, SECONDS);

        // since all the work is synchronous nothing is blocked and its all done
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    @Test
    public void testSyncDelaySubscription() {
        final TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = throttleScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        syncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);

        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(1);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    private Flowable<Long> asyncWork(final Scheduler sched) {
        return Flowable.range(1, 5).flatMap(new Function<Integer, Flowable<Long>>() {
            @Override
            public Flowable<Long> apply(Integer t) {
                return Flowable.timer(1, SECONDS, sched);
            }
        });
    }

    private Flowable<Long> syncWork(final Scheduler sched) {
        return Flowable.range(1, 5).flatMap(new Function<Integer, Flowable<Long>>() {
            @Override
            public Flowable<Long> apply(Integer t) {
                return Flowable.defer(new Callable<Flowable<Long>>() {
                    @Override
                    public Flowable<Long> call() {
                        return Flowable.just(0l);
                    }
                }).subscribeOn(sched);
            }
        });
    }

    private SchedulerWhen maxConcurrentScheduler(TestScheduler tSched) {
        SchedulerWhen sched = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> workerActions) {
                Flowable<Completable> workers = workerActions.map(new Function<Flowable<Completable>, Completable>() {
                    @Override
                    public Completable apply(Flowable<Completable> actions) {
                        return RxJava3Interop.concatCompletable(actions);
                    }
                });
                return RxJava3Interop.mergeCompletable(workers, 2);
            }
        }, tSched);
        return sched;
    }

    private SchedulerWhen throttleScheduler(final TestScheduler tSched) {
        SchedulerWhen sched = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> workerActions) {
                Flowable<Completable> workers = workerActions.map(new Function<Flowable<Completable>, Completable>() {
                    @Override
                    public Completable apply(Flowable<Completable> actions) {
                        return RxJava3Interop.concatCompletable(actions);
                    }
                });
                return RxJava3Interop.concatCompletable(workers.map(new Function<Completable, Completable>() {
                    @Override
                    public Completable apply(Completable worker) {
                        return worker.delay(1, SECONDS, tSched);
                    }
                }));
            }
        }, tSched);
        return sched;
    }

    @Test(timeout = 1000)
    public void testRaceConditions() {
        Scheduler comp = Schedulers.computation();
        Scheduler limited = RxJava3Interop.when(comp, new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> t) {
                return RxJava3Interop.mergeCompletable(Flowable.merge(t, 10));
            }
        });

        merge(just(just(1).subscribeOn(limited).observeOn(comp)).repeat(1000)).blockingSubscribe();
    }
}
