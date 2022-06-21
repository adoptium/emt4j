/********************************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.Dependency;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Analysis all classes in a given jar.
 */
class JarAnalyzer extends ClassAnalyzer {
    public static void analyze(Path jarFilePath, Consumer<Dependency> consumer) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath.toFile());
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                try (InputStream input = jarFile.getInputStream(jarEntry)) {
                    byte[] classFileContent = IOUtils.toByteArray(input);
                    processClass(classFileContent, jarFilePath, consumer, toClassName(jarEntry.getName()));
                } catch (Exception e) {
                    // we don't want an error interrupt the analysis process
                    e.printStackTrace();
                }
            }
        }
        consumer.accept(new Dependency(null, new DependTarget.Location(jarFilePath.toUri().toURL()), null));
    }
}
