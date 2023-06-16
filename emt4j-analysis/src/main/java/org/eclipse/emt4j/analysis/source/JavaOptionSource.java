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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * read a file that contain vm option.
 */
public class JavaOptionSource extends DependencySource {

    public JavaOptionSource(File optionFile) {
        super(optionFile);
    }

    @Override
    public void parse(Consumer<Dependency> consumer, Progress sourceProgress) {
        try {
            List<String> lines = FileUtils.readLines(getFile(), "UTF-8");
            lines.stream().filter((l) -> StringUtils.isNotEmpty(l))
                    .map((l) -> new Dependency(null, new DependTarget.VMOption(l), null,null))
                    .forEach((d) -> consumer.accept(d));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
