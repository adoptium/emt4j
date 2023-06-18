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

import org.apache.commons.io.IOUtils;
import org.eclipse.emt4j.analysis.common.util.ZipUtil;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.Dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Analysis all classes in a given jar.
 */
class JarAnalyzer extends ClassAnalyzer {
    private static final String CLASS = ".class";
    private static final String JAR = ".jar";
    public static final String SEPARATOR = "!/";

    public static void analyze(Path jarFilePath, Consumer<Dependency> consumer) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath.toFile());
        Enumeration<JarEntry> entries = jarFile.entries();
        boolean fatJar = false;
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(CLASS)) {
                try (InputStream input = jarFile.getInputStream(jarEntry)) {
                    byte[] classFileContent = IOUtils.toByteArray(input);
                    processClass(classFileContent, new URL(jarFilePath.toUri().toURL() + SEPARATOR + jarEntry.getName()), jarFilePath.toFile().getAbsolutePath(), consumer, toClassName(jarEntry.getName()));
                } catch (Exception e) {
                    // we don't want an error interrupt the analysis process
                    e.printStackTrace();
                }
            } else if (jarEntry.getName().endsWith(JAR)) {
                fatJar = true;
            }
        }
        consumer.accept(new Dependency(null, new DependTarget.Location(jarFilePath.toUri().toURL()), null, jarFilePath.toFile().getAbsolutePath()));

        //if this jar is a fat jar.Unzip to temporary files,scan each jars recursively.
        if (fatJar) {
            File tmp = Files.createTempDirectory("emt4j-unzip").toFile();
            try {
                ZipUtil.unzipTo(jarFilePath, tmp.toPath());
                Path unzipPath = tmp.toPath();

                try (Stream<Path> pathStream = Files.walk(unzipPath)) {
                    List<Path> subJars = pathStream.filter((path -> path.getFileName().toString().endsWith(JAR))).collect(Collectors.toList());
                    for (Path subJar : subJars) {
                        analyze(jarFilePath, unzipPath, subJar, consumer);
                    }
                }
            } finally {
                deleteFiles(tmp);
            }
        }
    }

    private static void analyze(Path parentJar, Path unzipTempDir, Path subJar, Consumer<Dependency> consumer) throws IOException {
        Path relativePath = unzipTempDir.relativize(subJar);
        URL location = new URL(parentJar.toUri().toURL().toExternalForm() + SEPARATOR + relativePath);
        String targetFilePath = parentJar.toFile().getAbsolutePath() + SEPARATOR + relativePath;

        try (JarFile jarFile = new JarFile(subJar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(CLASS)) {
                    try (InputStream input = jarFile.getInputStream(jarEntry)) {
                        byte[] classFileContent = IOUtils.toByteArray(input);
                        processClass(classFileContent, new URL(location + SEPARATOR + jarEntry.getName()), targetFilePath, consumer, toClassName(jarEntry.getName()));
                    } catch (Exception e) {
                        // we don't want an error interrupt the analysis process
                        System.err.println("Failed to analyze " + jarEntry.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        consumer.accept(new Dependency(null, new DependTarget.Location(location), null, targetFilePath));
    }

    private static void deleteFiles(File f) {
        if (f.exists()) {
            if (f.isFile()) {
                try {
                    Files.delete(f.toPath());
                } catch (IOException e) {
                    throw new Error("Clean up temporary file : " + f.getName() + " failed!", e);
                }
            } else if (f.isDirectory()) {
                try {
                    Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    throw new Error("Clean up temporary directory: " + f.getName() + " failed!", e);
                }
            }
        }
    }
}
