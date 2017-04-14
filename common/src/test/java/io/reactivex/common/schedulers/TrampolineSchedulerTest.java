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

import java.util.*;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.Scheduler.Worker;
import io.reactivex.common.disposables.CompositeDisposable;

public class TrampolineSchedulerTest extends AbstractSchedulerTests {

    @Override
    protected Scheduler getScheduler() {
        return Schedulers.trampoline();
    }

    @Test
    public void testNestedTrampolineWithUnsubscribe() {
        final ArrayList<String> workDone = new ArrayList<String>();
        final CompositeDisposable workers = new CompositeDisposable();
        Worker worker = Schedulers.trampoline().createWorker();
        try {
            workers.add(worker);
            worker.schedule(new Runnable() {

                @Override
                public void run() {
                    workers.add(doWorkOnNewTrampoline("A", workDone));
                }

            });

            final Worker worker2 = Schedulers.trampoline().createWorker();
            workers.add(worker2);
            worker2.schedule(new Runnable() {

                @Override
                public void run() {
                    workers.add(doWorkOnNewTrampoline("B", workDone));
                    // we unsubscribe worker2 ... it should not affect work scheduled on a separate Trampline.Worker
                    worker2.dispose();
                }

            });

            assertEquals(6, workDone.size());
            assertEquals(Arrays.asList("A.1", "A.B.1", "A.B.2", "B.1", "B.B.1", "B.B.2"), workDone);
        } finally {
            workers.dispose();
        }
    }

    private static Worker doWorkOnNewTrampoline(final String key, final ArrayList<String> workDone) {
        Worker worker = Schedulers.trampoline().createWorker();
        worker.schedule(new Runnable() {

            @Override
            public void run() {
                String msg = key + ".1";
                workDone.add(msg);
                System.out.println(msg);
                Worker worker3 = Schedulers.trampoline().createWorker();
                worker3.schedule(createPrintAction(key + ".B.1", workDone));
                worker3.schedule(createPrintAction(key + ".B.2", workDone));
            }

        });
        return worker;
    }

    private static Runnable createPrintAction(final String message, final ArrayList<String> workDone) {
        return new Runnable() {

            @Override
            public void run() {
                System.out.println(message);
                workDone.add(message);
            }

        };
    }
}
