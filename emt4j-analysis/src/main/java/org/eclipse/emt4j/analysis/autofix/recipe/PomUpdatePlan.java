/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis.autofix.recipe;

import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.autofix.*;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.*;
import java.util.stream.Collectors;

public class PomUpdatePlan {
    // record how tag will be updated
    private Map<Xml.Tag, XmlVisitor<ExecutionContext>> tagUpdatePlan = new HashMap<>();

    public PomUpdatePlan() {
    }

    public Set<XmlVisitor<ExecutionContext>> getProjectUpdateVisitors(MavenProject project) {
        return MavenHelper.getProjectData(project).getUpdateVisitors();
    }

    public Map<Xml.Tag, XmlVisitor<ExecutionContext>> getTagUpdatePlan() {
        return tagUpdatePlan;
    }

    public Collection<String> getNewlyAddedDependencies(MavenProject project) {
        return getProjectUpdateVisitors(project).stream()
                .filter(visitor -> visitor instanceof AddDependencyVisitor)
                .map(visitor -> ((AddDependencyVisitor) visitor).toGATV())
                .collect(Collectors.toList());
    }
}
