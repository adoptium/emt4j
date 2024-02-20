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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

public class PomUpdateRecipe extends Recipe {
    private PomUpdatePlan pomUpdatePlan;

    @Override
    public String getDisplayName() {
        return "Pom update Recipe.";
    }

    @Override
    public String getDescription() {
        return "Update pom.xml based on update plan.";
    }

    public PomUpdateRecipe(PomUpdatePlan pomUpdatePlan) {
        this.pomUpdatePlan = pomUpdatePlan;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new PomUpdateVisitor();
    }

    private class PomUpdateVisitor extends MyMavenVisitor<ExecutionContext> {

        @Override
        public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
            Xml xml= super.visitDocument(document, executionContext);
            MavenProject mavenProject = getMavenProject();
            for (XmlVisitor<ExecutionContext> visitor : pomUpdatePlan.getProjectUpdateVisitors(mavenProject)) {
                doAfterVisit(visitor);
            }
            return xml;
        }

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
            XmlVisitor<ExecutionContext> plan = pomUpdatePlan.getTagUpdatePlan().getOrDefault(tag, null);
            if (plan == null) {
                return super.visitTag(tag, executionContext);
            } else {
                return plan.visitTag(tag, executionContext);
            }
        }
    }
}
