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
import org.eclipse.emt4j.analysis.autofix.XmlTagMatcher;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

import static org.eclipse.emt4j.analysis.common.Constant.POM_ARTIFACT_ID;


@EqualsAndHashCode(callSuper = true)
public class OrderPluginRecipe extends Recipe implements ReportingRecipe {
    private XmlTagMatcher beforeTag;
    private XmlTagMatcher afterTag;
    private RecipeFixReporter reporter;

    @Override
    public String getDisplayName() {
        return "Order plugin";
    }

    @Override
    public String getDescription() {
        return "Guarantee one plugin is placed before another plugin";
    }

    public OrderPluginRecipe(String beforeArtifactID, String afterArtifactID) {
        this.beforeTag = (tag) -> MyMavenVisitor.isPluginTag(tag) && XmlTagHelper.matchTag(tag, POM_ARTIFACT_ID, beforeArtifactID);
        this.afterTag = (tag) -> MyMavenVisitor.isPluginTag(tag) && XmlTagHelper.matchTag(tag, POM_ARTIFACT_ID, afterArtifactID);
        reporter = new AbstractRecipeFixReporter.CountAsOneProblemRecipeFixReporter("autofix.pom.orderPlugin", new String[]{beforeArtifactID, afterArtifactID});
    }

    @Override
    public RecipeFixReporter getReporter() {
        return reporter;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new OrderTagVisitor(MyMavenVisitor::isPluginsTag, beforeTag, afterTag);
    }
}
