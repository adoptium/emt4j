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

import org.eclipse.emt4j.common.staticanalysis.impl.CastArraysAsListToArrayAnalyzer;
import org.eclipse.emt4j.common.staticanalysis.impl.CastSystemClassLoaderToURLClassLoaderAnalyzer;
import org.eclipse.emt4j.common.staticanalysis.impl.PatternCompileAnalyzer;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

class InvokerImpl implements Invoker {

    private final File directory;

    private final Map<String, Analyzer> analyzerMap = new HashMap<>();

    InvokerImpl() throws IOException {

        directory = Files.createTempDirectory("files-for-static-analysis-" + Thread.currentThread().getId() + "-").toFile();

        System.out.println(directory.getAbsolutePath());

        directory.deleteOnExit();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                //noinspection resource
                Files.walk(directory.toPath())
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        add(new CastArraysAsListToArrayAnalyzer());
        add(new PatternCompileAnalyzer());
        add(new CastSystemClassLoaderToURLClassLoaderAnalyzer());
    }

    private void add(Analyzer analyzer) {
        analyzerMap.put(analyzer.rule(), analyzer);
    }

    private void reset() {
        G.v().reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_soot_classpath(directory.getAbsolutePath());
    }

    private Path writeTo(String className, byte[] bytecodes) {
        String[] split = className.split("\\.");
        // create package
        File current = directory;
        for (int i = 0; i < split.length - 1; i++) {
            current = new File(current, split[i]);
            //noinspection ResultOfMethodCallIgnored
            current.mkdir();
        }
        try {
            return Files.write(current.toPath().resolve(split[split.length - 1] + ".class"), bytecodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean invoke(String rule, String className, byte[] bytecodes) {
        if (className.endsWith("module-info") || className.endsWith("package-info")) {
            return false;
        }

        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - ".class".length());
        }

        reset();

        Analyzer analyzer = analyzerMap.get(rule);
        Path path = writeTo(className, bytecodes);
        try {
            SootClass clazz = Scene.v().loadClassAndSupport(className);
            clazz.setApplicationClass();
            Scene.v().loadNecessaryClasses();

            return analyzer.analyze(clazz);
        } finally {
            path.toFile().delete();
        }
    }

}
