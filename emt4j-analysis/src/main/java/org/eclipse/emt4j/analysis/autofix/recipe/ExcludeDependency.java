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
import org.openrewrite.*;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.Optional;

import static org.eclipse.emt4j.analysis.autofix.recipe.XmlTagHelper.matchTag;
import static org.eclipse.emt4j.analysis.common.Constant.*;

@EqualsAndHashCode(callSuper = true)
public class ExcludeDependency extends Recipe {

    private String dependencyGroupId;
    private String dependencyArtifactId;
    private String exclusionGroupId;
    private String exclusionArtifactId;

    private RecipeFixReporter reporter;

    public ExcludeDependency(String dependencyGroupId, String dependencyArtifactId, String exclusionGroupId, String exclusionArtifactId, RecipeFixReporter reporter) {
        this.dependencyGroupId = dependencyGroupId;
        this.dependencyArtifactId = dependencyArtifactId;
        this.exclusionGroupId = exclusionGroupId;
        this.exclusionArtifactId = exclusionArtifactId;
        this.reporter = reporter;
    }

    @Override
    public String getDisplayName() {
        return "Exclude Maven dependency";
    }

    @Override
    public String getDescription() {
        return "Exclude specified dependency from any dependency that transitively includes it.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ExcludeDependencyVisitor();
    }

    private class ExcludeDependencyVisitor extends MyMavenVisitor<ExecutionContext> {

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (!(isDependencyTag() && matchTag(tag, POM_GROUP_ID, dependencyGroupId)
                    && matchTag(tag, POM_ARTIFACT_ID, dependencyArtifactId))) {
                return super.visitTag(tag, ctx);
            }

            Optional<Xml.Tag> maybeExclusions = tag.getChild("exclusions");
            if (maybeExclusions.isPresent()) {
                Xml.Tag exclusions = maybeExclusions.get();

                List<Xml.Tag> individualExclusions = exclusions.getChildren("exclusion");
                if (individualExclusions.stream().noneMatch(exclusion ->
                        exclusionGroupId.equals(exclusion.getChildValue(POM_GROUP_ID).orElse(null)) &&
                                exclusionArtifactId.equals(exclusion.getChildValue(POM_ARTIFACT_ID).orElse(null)))) {
                    reporter.recordModification(null);
                    doAfterVisit(new AddToTagVisitor<>(exclusions, Xml.Tag.build("" +
                            "<exclusion>\n" +
                            "<groupId>" + exclusionGroupId + "</groupId>\n" +
                            "<artifactId>" + exclusionArtifactId + "</artifactId>\n" +
                            "</exclusion>")));
                }
            } else {
                reporter.recordModification(null);
                doAfterVisit(new AddToTagVisitor<>(tag, Xml.Tag.build("" +
                        "<exclusions>\n" +
                        "<exclusion>\n" +
                        "<groupId>" + exclusionGroupId + "</groupId>\n" +
                        "<artifactId>" + exclusionArtifactId + "</artifactId>\n" +
                        "</exclusion>\n" +
                        "</exclusions>")));
            }
            return super.visitTag(tag, ctx);
        }
    }

    @Override
    public String toString() {
        return dependencyGroupId + ':' + dependencyArtifactId + "->" + exclusionGroupId + ":" + exclusionArtifactId;
    }
}
