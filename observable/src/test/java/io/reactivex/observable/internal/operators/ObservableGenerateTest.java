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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.*;
import io.reactivex.common.internal.functions.Functions;
import io.reactivex.observable.*;

public class ObservableGenerateTest {

    @Test
    public void statefulBiconsumer() {
        Observable.generate(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return 10;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {
            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(s);
            }
        }, new Consumer<Object>() {
            @Override
            public void accept(Object d) throws Exception {

            }
        })
        .take(5)
        .test()
        .assertResult(10, 10, 10, 10, 10);
    }

    @Test
    public void stateSupplierThrows() {
        Observable.generate(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        }, new BiConsumer<Object, Emitter<Object>>() {
            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(s);
            }
        }, Functions.emptyConsumer())
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void generatorThrows() {
        Observable.generate(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {
            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                throw new TestException();
            }
        }, Functions.emptyConsumer())
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void disposerThrows() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Observable.generate(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return 1;
                }
            }, new BiConsumer<Object, Emitter<Object>>() {
                @Override
                public void accept(Object s, Emitter<Object> e) throws Exception {
                    e.onComplete();
                }
            }, new Consumer<Object>() {
                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException();
                }
            })
            .test()
            .assertResult();

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.generate(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return 1;
                }
            }, new BiConsumer<Object, Emitter<Object>>() {
                @Override
                public void accept(Object s, Emitter<Object> e) throws Exception {
                    e.onComplete();
                }
            }, Functions.emptyConsumer()));
    }

    @Test
    public void nullError() {
        final int[] call = { 0 };
        Observable.generate(Functions.justCallable(1),
        new BiConsumer<Integer, Emitter<Object>>() {
            @Override
            public void accept(Integer s, Emitter<Object> e) throws Exception {
                try {
                    e.onError(null);
                } catch (NullPointerException ex) {
                    call[0]++;
                }
            }
        }, Functions.emptyConsumer())
        .test()
        .assertFailure(NullPointerException.class);

        assertEquals(0, call[0]);
    }

    @Test
    public void multipleOnNext() {
        Observable.generate(new Consumer<Emitter<Object>>() {
            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
            }
        })
        .test()
        .assertFailure(IllegalStateException.class, 1);
    }

    @Test
    public void multipleOnError() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            Observable.generate(new Consumer<Emitter<Object>>() {
                @Override
                public void accept(Emitter<Object> e) throws Exception {
                    e.onError(new TestException("First"));
                    e.onError(new TestException("Second"));
                }
            })
            .test()
            .assertFailure(TestException.class);

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void multipleOnComplete() {
        Observable.generate(new Consumer<Emitter<Object>>() {
            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onComplete();
                e.onComplete();
            }
        })
        .test()
        .assertResult();
    }
}
