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

import org.eclipse.emt4j.analysis.analyzer.DependencyAnalyzer;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

public class SingleClassSource extends DependencySource {
    public SingleClassSource(File classFile) {
        super(classFile);
    }

    @Override
    public void parse(Consumer<Dependency> consumer, Progress sourceProgress) throws IOException {
        new DependencyAnalyzer(Collections.singletonList(getFile().toPath())).iterateDo(consumer, sourceProgress);
        consumer.accept(new Dependency(null, new DependTarget.Location(getFile().toURI().toURL()), null, getFile().getAbsolutePath()));
    }
}
