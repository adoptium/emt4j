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

import org.eclipse.emt4j.analysis.autofix.MavenHelper;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.tree.Xml;

public class PomScanRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Pom Scan Recipe.";
    }

    @Override
    public String getDescription() {
        return "Scan pom file and record its structure.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new PomScanVisitor();
    }

    private class PomScanVisitor extends MyMavenVisitor<ExecutionContext> {
        private PomResolution pomResolution;

        @Override
        public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
            pomResolution = MavenHelper.getPomResolution(getMavenProject(document));
            return super.visitDocument(document, executionContext);
        }

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (isPropertyTag()) {
                pomResolution.getProperties().add(tag);
            } else if (isManagedDependencyTag()) {
                pomResolution.getManagedDependencies().add(tag);
            } else if (isDependencyTag()) {
                pomResolution.getDependencies().add(tag);
            } else if (isPluginTag()) {
                pomResolution.getPlugins().add(tag);
            } else if (isDependenciesTag()) {
                pomResolution.setDependenciesTag(tag);
            }else if(isParentTag()){
                pomResolution.setParentTag(tag);
            }else if(isModuleSelfTag()){
                pomResolution.setModuleSelfTag(tag);
            }
            return super.visitTag(tag, ctx);
        }
    }
}
