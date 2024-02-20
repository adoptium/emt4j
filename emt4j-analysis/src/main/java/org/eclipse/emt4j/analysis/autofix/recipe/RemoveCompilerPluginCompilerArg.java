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
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;

import static org.eclipse.emt4j.analysis.autofix.recipe.XmlTagHelper.matchTag;
import static org.eclipse.emt4j.analysis.common.Constant.POM_ARTIFACT_ID;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveCompilerPluginCompilerArg extends Recipe implements ReportingRecipe {
    @Override
    public String getDisplayName() {
        return "Remove maven-compiler-plugin compiler argument";
    }

    @Override
    public String getDescription() {
        return "Remove maven-compiler-plugin compiler argument";
    }

    String argumentName;

    public RemoveCompilerPluginCompilerArg(String argumentName) {
        this.argumentName = argumentName;
    }

    @Override
    public RecipeFixReporter getReporter() {
        return new AbstractRecipeFixReporter.CountAsOneNoFileProblemRecipeFixReporter("autofix.pom.removeCompilerPluginCompileArg", new String[]{argumentName});
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MyMavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (!isPluginTag() || !matchTag(tag, POM_ARTIFACT_ID, "maven-compiler-plugin")) {
                    return super.visitTag(tag, ctx);
                }

                List<Xml.Tag> tagsToRemove = XmlTagHelper.findDescendants(tag,"/configuration/compilerArguments/" + argumentName);
                for (Xml.Tag tagToMove : tagsToRemove) {
                    doAfterVisit(new RemoveContentVisitor<>(tagToMove, true));
                }
                return super.visitTag(tag, ctx);
            }
        };
    }
}
