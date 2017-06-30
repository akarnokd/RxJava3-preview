package io.reactivex.common.internal.schedulers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.common.*;
import io.reactivex.common.internal.functions.Functions;

/**
 * Wrapper for a regular task that gets immediately rescheduled when the task completed.
 */
final class InstantPeriodicTask implements Callable<Void>, Disposable {

    final Runnable task;

    final AtomicReference<Future<?>> rest;

    final AtomicReference<Future<?>> first;

    final ExecutorService executor;

    Thread runner;

    static final FutureTask<Void> CANCELLED = new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null);

    InstantPeriodicTask(Runnable task, ExecutorService executor) {
        super();
        this.task = task;
        this.first = new AtomicReference<Future<?>>();
        this.rest = new AtomicReference<Future<?>>();
        this.executor = executor;
    }

    @Override
    public Void call() throws Exception {
        try {
            runner = Thread.currentThread();
            try {
                task.run();
                setRest(executor.submit(this));
            } catch (Throwable ex) {
                RxJavaCommonPlugins.onError(ex);
            }
        } finally {
            runner = null;
        }
        return null;
    }

    @Override
    public void dispose() {
        Future<?> current = first.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
        current = rest.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
    }

    @Override
    public boolean isDisposed() {
        return first.get() == CANCELLED;
    }

    void setFirst(Future<?> f) {
        for (;;) {
            Future<?> current = first.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
            }
            if (first.compareAndSet(current, f)) {
                return;
            }
        }
    }

    void setRest(Future<?> f) {
        for (;;) {
            Future<?> current = rest.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
            }
            if (rest.compareAndSet(current, f)) {
                return;
            }
        }
    }
}