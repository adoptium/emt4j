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
public class UpdatePluginVersion extends Recipe implements ReportingRecipe {
    @Override
    public String getDisplayName() {
        return "Update Plugin";
    }

    @Override
    public String getDescription() {
        return "Update Plugin";
    }

    String groupId;

    String artifactId;

    String version;


    public UpdatePluginVersion(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public RecipeFixReporter getReporter() {
        return new AbstractRecipeFixReporter.CountAsOneNoFileProblemRecipeFixReporter("autofix.pom.updatePlugin", new String[]{artifactId, version});
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MyMavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {

                if (isPluginTag() && matchTag(tag, POM_GROUP_ID, groupId)
                        && matchTag(tag, POM_ARTIFACT_ID, artifactId)) {
                    updatePossiblyExistVersionTag(tag, version);
                }
                return super.visitTag(tag, ctx);
            }
        };
    }
}
