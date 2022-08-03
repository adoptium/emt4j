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
package org.eclipse.emt4j.analysis.source;

import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.analysis.analyzer.DependencyAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Regard all classes and jars in directory as source to check.
 */
public class DirectorySource extends DependencySource {
    public DirectorySource(File directory) {
        super(directory);
    }

    @Override
    public void parse(Consumer<Dependency> consumer, Progress sourceProgress) throws IOException {
        List<Path> files = walk();
        for (Path f : files) {
            consumer.accept(new Dependency(null, new DependTarget.Location(f.toFile().toURI().toURL()), null,f.toFile().getAbsolutePath()));
        }
        new DependencyAnalyzer(files).iterateDo(consumer, sourceProgress);
    }

    @Override
    public String desc() {
        return getFile().getName();
    }

    private List<Path> walk() throws IOException {
        List<Path> candidateFiles = new ArrayList<>();
        Files.walkFileTree(getFile().toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileStr = file.toString().toLowerCase();
                if (fileStr.endsWith(".jar") || fileStr.endsWith(".class")) {
                    candidateFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return candidateFiles;
    }
}
