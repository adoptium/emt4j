/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.emt4j.common.staticanalysis;

import org.eclipse.emt4j.common.JdkMigrationException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

public class StaticAnalysisEntry {

    static class MyClassLoader extends URLClassLoader {
        public MyClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
            if ((name.startsWith(Invoker.class.getPackage().getName())
                 && !name.endsWith("Invoker"))
                || name.startsWith("soot.")) {
                Class<?> loaded = findLoadedClass(name);
                return loaded != null ? loaded : super.findClass(name);
            }
            return super.loadClass(name);
        }
    }

    private static final ThreadLocal<Invoker> INVOKER = ThreadLocal.withInitial(() -> {
        try {
            ClassLoader classLoader = StaticAnalysisEntry.class.getClassLoader();
            MyClassLoader myClassLoader;
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                myClassLoader = new MyClassLoader(urlClassLoader.getURLs(), urlClassLoader);
            } else {
                String[] cps = System.getProperty("java.class.path").split(File.pathSeparator);
                URL[] urls = new URL[cps.length];
                for (int i = 0; i < cps.length; i++) {
                    urls[i] = new File(cps[i]).toURI().toURL();
                }
                //noinspection resource
                myClassLoader = new MyClassLoader(urls, classLoader);
            }

            Class<?> clazz = myClassLoader.loadClass("org.eclipse.emt4j.common.staticanalysis.InvokerImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Invoker) constructor.newInstance();
        } catch (Throwable t) {
            throw new JdkMigrationException(t);
        }
    });

    public static boolean analyze(String rule, String className, byte[] bytecodes) {
        return INVOKER.get().invoke(rule, className, bytecodes);
    }
}
