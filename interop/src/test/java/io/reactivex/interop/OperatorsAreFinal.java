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

import java.io.File;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class OperatorsAreFinal {

    File directoryOf(String baseClassName, String project) throws Exception {
        File f = MaybeNo2Dot0Since.findSource(baseClassName, project);
        if (f == null) {
            return null;
        }

        String parent = f.getParentFile().getAbsolutePath().replace('\\', '/');
        if (!parent.endsWith("/")) {
            parent += "/";
        }

        parent += "internal/operators/";
        return new File(parent);
    }

    void check(String baseClassName, String project) throws Exception {
        File f = directoryOf(baseClassName, project);
        if (f == null) {
            return;
        }

        StringBuilder e = new StringBuilder();

        File[] files = f.listFiles();
        if (files != null) {
            for (File g : files) {
                if (g.getName().startsWith(baseClassName) && g.getName().endsWith(".java")) {
                    String className = "io.reactivex." + project + ".internal.operators." + g.getName().replace(".java", "");

                    Class<?> clazz = Class.forName(className);

                    if ((clazz.getModifiers() & Modifier.FINAL) == 0 && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
                        e.append("java.lang.RuntimeException: ").append(className).append(" is not final\r\n");
                        e.append(" at ").append(className).append(" (").append(g.getName()).append(":14)\r\n\r\n");
                    }
                }
            }
        }

        if (e.length() != 0) {
            System.out.println(e);

            throw new AssertionError(e.toString());
        }
    }

    @Test
    public void flowable() throws Exception {
        check("Flowable", "flowable");
    }

    @Test
    public void observable() throws Exception {
        check("Observable", "observable");
    }

    @Test
    public void single() throws Exception {
        check("Single", "observable");
    }

    @Test
    public void completable() throws Exception {
        check("Completable", "observable");
    }

    @Test
    public void maybe() throws Exception {
        check("Maybe", "observable");
    }

}
