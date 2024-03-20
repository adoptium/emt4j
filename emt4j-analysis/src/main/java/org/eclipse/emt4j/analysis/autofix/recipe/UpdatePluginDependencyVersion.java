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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.tree.Xml;

import static org.eclipse.emt4j.analysis.autofix.recipe.XmlTagHelper.matchTag;
import static org.eclipse.emt4j.analysis.common.Constant.*;

@Value
@EqualsAndHashCode(callSuper = true)
public class UpdatePluginDependencyVersion extends Recipe {
    private RecipeFixReporter fixReporter;

    @Override
    public String getDisplayName() {
        return "Update plugin dependency version.";
    }

    @Override
    public String getDescription() {
        return "Update plugin dependency version.";
    }

    private String pluginGroupId;

    private String pluginArtifactId;

    private String dependencyGroupId;

    private String dependencyArtifactID;

    private String dependencyVersion;

    public UpdatePluginDependencyVersion(String pluginGroupId, String pluginArtifactId, String dependencyGroupId, String dependencyArtifactID, String dependencyVersion) {
        this.pluginGroupId = pluginGroupId;
        this.pluginArtifactId = pluginArtifactId;
        this.dependencyGroupId = dependencyGroupId;
        this.dependencyArtifactID = dependencyArtifactID;
        this.dependencyVersion = dependencyVersion;
        fixReporter = new AbstractRecipeFixReporter.CountAsOneNoFileProblemRecipeFixReporter("autofix.pom.updatePluginDependency"
                , new String[]{pluginArtifactId, dependencyGroupId + ":" + dependencyArtifactID, dependencyVersion});
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MyMavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (isPluginTag() && matchTag(tag, POM_GROUP_ID, pluginGroupId)
                        && matchTag(tag, POM_ARTIFACT_ID, pluginArtifactId)) {
                    tag = (Xml.Tag)new MyMavenVisitor<ExecutionContext>() {
                        @Override
                        public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                            if (tag.getName().equals(POM_DEPENDENCY) && matchTag(tag, POM_GROUP_ID, dependencyGroupId)
                                && matchTag(tag, POM_ARTIFACT_ID, dependencyArtifactID)) {
                                boolean success = updatePossiblyExistVersionTag(tag, dependencyVersion);
                                if (success) {
                                    fixReporter.recordModification(null);
                                }
                            }
                            return super.visitTag(tag, executionContext);
                        }
                    }.visitTag(tag,ctx);
                }
                return super.visitTag(tag, ctx);
            }
        };
    }

}
