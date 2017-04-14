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

package io.reactivex.common.schedulers;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.reactivex.common.Scheduler.Worker;

/**
 * Base tests for schedulers that involve threads (concurrency).
 *
 * These can only run on Schedulers that launch threads since they expect async/concurrent behavior.
 *
 * The Current/Immediate schedulers will not work with these tests.
 */
public abstract class AbstractSchedulerConcurrencyTests extends AbstractSchedulerTests {

    @Test
    public void testUnsubscribeRecursiveScheduleFromOutside() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        final AtomicInteger counter = new AtomicInteger();
        final Worker inner = getScheduler().createWorker();
        try {
            inner.schedule(new Runnable() {

                @Override
                public void run() {
                    inner.schedule(new Runnable() {

                        int i;

                        @Override
                        public void run() {
                            System.out.println("Run: " + i++);
                            if (i == 10) {
                                latch.countDown();
                                try {
                                    // wait for unsubscribe to finish so we are not racing it
                                    unsubscribeLatch.await();
                                } catch (InterruptedException e) {
                                    // we expect the countDown if unsubscribe is not working
                                    // or to be interrupted if unsubscribe is successful since
                                    // the unsubscribe will interrupt it as it is calling Future.cancel(true)
                                    // so we will ignore the stacktrace
                                }
                            }

                            counter.incrementAndGet();
                            inner.schedule(this);
                        }
                    });
                }

            });

            latch.await();
            inner.dispose();
            unsubscribeLatch.countDown();
            Thread.sleep(200); // let time pass to see if the scheduler is still doing work
            assertEquals(10, counter.get());
        } finally {
            inner.dispose();
        }
    }

    @Test
    public void testUnsubscribeRecursiveScheduleFromInside() throws InterruptedException {
        final CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        final AtomicInteger counter = new AtomicInteger();
        final Worker inner = getScheduler().createWorker();
        try {
            inner.schedule(new Runnable() {

                @Override
                public void run() {
                    inner.schedule(new Runnable() {

                        int i;

                        @Override
                        public void run() {
                            System.out.println("Run: " + i++);
                            if (i == 10) {
                                inner.dispose();
                            }

                            counter.incrementAndGet();
                            inner.schedule(this);
                        }
                    });
                }

            });

            unsubscribeLatch.countDown();
            Thread.sleep(200); // let time pass to see if the scheduler is still doing work
            assertEquals(10, counter.get());
        } finally {
            inner.dispose();
        }
    }

    @Test
    public void testUnsubscribeRecursiveScheduleWithDelay() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        final AtomicInteger counter = new AtomicInteger();
        final Worker inner = getScheduler().createWorker();

        try {
            inner.schedule(new Runnable() {

                @Override
                public void run() {
                    inner.schedule(new Runnable() {

                        long i = 1L;

                        @Override
                        public void run() {
                            if (i++ == 10) {
                                latch.countDown();
                                try {
                                    // wait for unsubscribe to finish so we are not racing it
                                    unsubscribeLatch.await();
                                } catch (InterruptedException e) {
                                    // we expect the countDown if unsubscribe is not working
                                    // or to be interrupted if unsubscribe is successful since
                                    // the unsubscribe will interrupt it as it is calling Future.cancel(true)
                                    // so we will ignore the stacktrace
                                }
                            }

                            counter.incrementAndGet();
                            inner.schedule(this, 10, TimeUnit.MILLISECONDS);
                        }
                    }, 10, TimeUnit.MILLISECONDS);
                }
            });

            latch.await();
            inner.dispose();
            unsubscribeLatch.countDown();
            Thread.sleep(200); // let time pass to see if the scheduler is still doing work
            assertEquals(10, counter.get());
        } finally {
            inner.dispose();
        }
    }

    @Test
    public void recursionFromOuterActionAndUnsubscribeInside() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Worker inner = getScheduler().createWorker();
        try {
            inner.schedule(new Runnable() {

                int i;

                @Override
                public void run() {
                    i++;
                    if (i % 100000 == 0) {
                        System.out.println(i + "  Total Memory: " + Runtime.getRuntime().totalMemory() + "  Free: " + Runtime.getRuntime().freeMemory());
                    }
                    if (i < 1000000L) {
                        inner.schedule(this);
                    } else {
                        latch.countDown();
                    }
                }
            });

            latch.await();
        } finally {
            inner.dispose();
        }
    }

    @Test
    public void testRecursion() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Worker inner = getScheduler().createWorker();
        try {
            inner.schedule(new Runnable() {

                private long i;

                @Override
                public void run() {
                    i++;
                    if (i % 100000 == 0) {
                        System.out.println(i + "  Total Memory: " + Runtime.getRuntime().totalMemory() + "  Free: " + Runtime.getRuntime().freeMemory());
                    }
                    if (i < 1000000L) {
                        inner.schedule(this);
                    } else {
                        latch.countDown();
                    }
                }
            });

            latch.await();
        } finally {
            inner.dispose();
        }
    }
}
