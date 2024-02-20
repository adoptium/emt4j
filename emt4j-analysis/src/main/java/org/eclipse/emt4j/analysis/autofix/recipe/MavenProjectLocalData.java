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

import lombok.Data;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.autofix.DTNode;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.HashSet;
import java.util.Set;

// This class saves data that is related to a maven project from session, to reduce code like
// Map<MavenProject, SomeKindOfData>
@Data
public class MavenProjectLocalData {
    private MavenProject project;

    // absolute path of pom.xml
    private String pom;
    private PomResolution resolution = new PomResolution();
    private int javaSourceCount = 0;
    private DTNode dependencyRoot;
    private Set<XmlVisitor<ExecutionContext>> updateVisitors = new HashSet<>();

    public MavenProjectLocalData(MavenProject project) {
        this.project = project;
        this.pom = project.getBasedir().getAbsolutePath() + "/pom.xml";
    }

}
