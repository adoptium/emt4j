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
package org.eclipse.emt4j.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A tool that scans class files by the "exclude" and "include" rules.
 */
class ClassFileScanner {

    static List<Path> scan(String classDir, List<String> excludes, List<String> includes) throws IOException {
        List<Path> candidateFiles = new ArrayList<>();
        List<FileSetPattern> excludeFileSetPattern = toFileSetPattern(excludes);
        List<FileSetPattern> includeFileSetPattern = toFileSetPattern(includes);

        Path directory = new File(classDir).toPath();
        Files.walkFileTree(directory, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (accept(directory, file, excludeFileSetPattern, includeFileSetPattern)) {
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

    private static boolean accept(Path directory, Path file, List<FileSetPattern> excludeFileSetPattern, List<FileSetPattern> includeFileSetPattern) {
        String fileStr = file.toString().toLowerCase();
        if (!fileStr.endsWith(".class")) {
            return false;
        }
        String relative = directory.relativize(file).toString();
        if (excludeFileSetPattern != null) {
            for (FileSetPattern fsp : excludeFileSetPattern) {
                if (fsp.matches(relative)) {
                    return false;
                }
            }
        }

        //if no include set pattern ,it same as accept all
        if (includeFileSetPattern != null && !includeFileSetPattern.isEmpty()) {
            for (FileSetPattern fsp : includeFileSetPattern) {
                if (fsp.matches(relative)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private static List<FileSetPattern> toFileSetPattern(List<String> fileset) {
        if (null == fileset || fileset.isEmpty()) {
            return Collections.emptyList();
        }
        return fileset.stream().map((f) -> new FileSetPattern(f)).collect(Collectors.toList());
    }
}
