/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis.analyzer;

import org.apache.commons.io.IOUtils;
import org.eclipse.emt4j.common.ClassSymbol;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.classanalyze.ClassInspectorInstance;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Analysis class file.
 */
public class ClassAnalyzer {
    public static void analyze(Path classFilePath, Consumer<Dependency> consumer) throws IOException {
        try (InputStream inputStream = new FileInputStream(classFilePath.toFile())) {
            byte[] classFileContent = IOUtils.toByteArray(inputStream);
            processClass(classFileContent, classFilePath.toUri().toURL(), classFilePath.toFile().getAbsolutePath(), consumer, classFilePath.toFile().getName());
        }
    }

    protected static void processClass(byte[] classFileContent, URL location, String targetFilePath, Consumer<Dependency> consumer, String className) throws IOException {
        ClassSymbol symbol = ClassInspectorInstance.getInstance().getSymbolInClass(classFileContent);
        for (String type : symbol.getTypeSet()) {
            consumer.accept(new Dependency(location, new DependTarget.Class(type, DependType.CLASS), null, targetFilePath));
        }
        for (DependTarget.Method method : symbol.getCallMethodSet()) {
            Dependency dependency = new Dependency(location, method, null, targetFilePath);
            dependency.setLines(symbol.getCallMethodToLines().get(method));
            consumer.accept(dependency);
        }

        Dependency wholeClass = new Dependency(location,
                new DependTarget.Class(className, DependType.WHOLE_CLASS), null, targetFilePath);
        wholeClass.setClassSymbol(symbol);
        wholeClass.setCurrClassBytecode(classFileContent);
        consumer.accept(wholeClass);
    }

    protected static String toClassName(String jarEntryName) {
        return normalize(jarEntryName.substring(0, jarEntryName.length() - ".class".length()));
    }


    private static String normalize(String internalName) {
        return internalName.replace('/', '.').replace('$', '.');
    }
}
