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

package io.reactivex.common.internal.schedulers;

import java.util.concurrent.*;

import io.reactivex.common.*;
import io.reactivex.common.annotations.*;
import io.reactivex.common.internal.disposables.DisposableContainer;

/**
 * Base class that manages a single-threaded ScheduledExecutorService as a
 * worker but doesn't perform task-tracking operations.
 *
 */
public class NewThreadWorker extends Scheduler.Worker implements Disposable {
    private final ScheduledExecutorService executor;

    volatile boolean disposed;

    public NewThreadWorker(ThreadFactory threadFactory) {
        executor = SchedulerPoolFactory.create(threadFactory);
    }

    @NonNull
    @Override
    public Disposable schedule(@NonNull final Runnable run) {
        return schedule(run, 0, null);
    }

    @NonNull
    @Override
    public Disposable schedule(@NonNull final Runnable action, long delayTime, @NonNull TimeUnit unit) {
        if (disposed) {
            return Scheduler.REJECTED;
        }
        return scheduleActual(action, delayTime, unit, null);
    }

    /**
     * Schedules the given runnable on the underlying executor directly and
     * returns its future wrapped into a Disposable.
     * @param run the Runnable to execute in a delayed fashion
     * @param delayTime the delay amount
     * @param unit the delay time unit
     * @return the ScheduledRunnable instance
     */
    public Disposable scheduleDirect(final Runnable run, long delayTime, TimeUnit unit) {
        ScheduledDirectTask task = new ScheduledDirectTask(RxJavaCommonPlugins.onSchedule(run));
        try {
            Future<?> f;
            if (delayTime <= 0L) {
                f = executor.submit(task);
            } else {
                f = executor.schedule(task, delayTime, unit);
            }
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaCommonPlugins.onError(ex);
            return Scheduler.REJECTED;
        }
    }

    /**
     * Schedules the given runnable periodically on the underlying executor directly
     * and returns its future wrapped into a Disposable.
     * @param run the Runnable to execute in a periodic fashion
     * @param initialDelay the initial delay amount
     * @param period the repeat period amount
     * @param unit the time unit for both the initialDelay and period
     * @return the ScheduledRunnable instance
     */
    public Disposable schedulePeriodicallyDirect(final Runnable run, long initialDelay, long period, TimeUnit unit) {
        ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(RxJavaCommonPlugins.onSchedule(run));
        try {
            Future<?> f = executor.scheduleAtFixedRate(task, initialDelay, period, unit);
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaCommonPlugins.onError(ex);
            return Scheduler.REJECTED;
        }
    }


    /**
     * Wraps the given runnable into a ScheduledRunnable and schedules it
     * on the underlying ScheduledExecutorService.
     * <p>If the schedule has been rejected, the ScheduledRunnable.wasScheduled will return
     * false.
     * @param run the runnable instance
     * @param delayTime the time to delay the execution
     * @param unit the time unit
     * @param parent the optional tracker parent to add the created ScheduledRunnable instance to before it gets scheduled
     * @return the ScheduledRunnable instance
     */
    @NonNull
    public ScheduledRunnable scheduleActual(final Runnable run, long delayTime, @NonNull TimeUnit unit, @Nullable DisposableContainer parent) {
        Runnable decoratedRun = RxJavaCommonPlugins.onSchedule(run);

        ScheduledRunnable sr = new ScheduledRunnable(decoratedRun, parent);

        if (parent != null) {
            if (!parent.add(sr)) {
                return sr;
            }
        }

        Future<?> f;
        try {
            if (delayTime <= 0) {
                f = executor.submit((Callable<Object>)sr);
            } else {
                f = executor.schedule((Callable<Object>)sr, delayTime, unit);
            }
            sr.setFuture(f);
        } catch (RejectedExecutionException ex) {
            if (parent != null) {
                parent.remove(sr);
            }
            RxJavaCommonPlugins.onError(ex);
        }

        return sr;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            executor.shutdownNow();
        }
    }

    /**
     * Shuts down the underlying executor in a non-interrupting fashion.
     */
    public void shutdown() {
        if (!disposed) {
            disposed = true;
            executor.shutdown();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
