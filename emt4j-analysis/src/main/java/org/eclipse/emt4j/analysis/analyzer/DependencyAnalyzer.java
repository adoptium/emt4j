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


import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * For different file type,call corresponding Analyzer to do the incompatible analysis.
 */
public class DependencyAnalyzer {

    private final List<Path> files;

    public DependencyAnalyzer(List<Path> files) {
        this.files = files;
    }

    public void iterateDo(Consumer<Dependency> consumer, Progress sourceProgress) throws IOException {
        int i = 0;
        Progress progress = null;
        if (sourceProgress != null) {
            progress = new Progress(sourceProgress, 0, files.size(), "Analysis files");
        }
        for (Path file : files) {
            try {
                i++;
                switch (FileUtil.fileType(file.toString())) {
                    case Jar:
                        JarAnalyzer.analyze(file, consumer);
                        break;
                    case Class:
                        ClassAnalyzer.analyze(file, consumer);
                        break;
                }
                if (progress != null) {
                    progress.printProgress(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (progress != null) {
            progress.cleanProgress();
        }
    }
}
