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

import org.eclipse.emt4j.analysis.autofix.XmlTagMatcher;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openrewrite.Tree.randomId;

public class AddJDKMigrationCommentRecipe extends Recipe {

    private List<String> migrationGATVs;
    private PomUpdatePlan pomUpdatePlan;

    @Override
    public String getDisplayName() {
        return "Add \"JDK11 upgrade\" comments to dependencies we added";
    }

    @Override
    public String getDescription() {
        return getDisplayName();
    }

    public AddJDKMigrationCommentRecipe(List<String> migrationGATVs, PomUpdatePlan pomUpdatePlan) {
        this.migrationGATVs = migrationGATVs;
        this.pomUpdatePlan = pomUpdatePlan;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddJDKMigrationCommentVisitor();
    }

    private class AddJDKMigrationCommentVisitor extends MyMavenVisitor<ExecutionContext> {

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
            if (!isDependenciesTag()) {
                return super.visitTag(tag, executionContext);
            }
            List<? extends Content> children = tag.getContent();
            if (children == null) {
                return super.visitTag(tag, executionContext);
            }

            Collection<String> newlyAdded = pomUpdatePlan == null ? migrationGATVs :
                    pomUpdatePlan.getNewlyAddedDependencies(getMavenProject());
            List<String> newlyAddedForJDKMigration = migrationGATVs.stream()
                    .filter(newlyAdded::contains)
                    .collect(Collectors.toList());
            XmlTagMatcher tagMatcher = new XmlTagMatcher.GAGroupXmlTagMatcher(newlyAddedForJDKMigration);

            int first = -1, last = -1;
            for (int i = 0; i < children.size(); i++) {
                Content child = children.get(i);
                if (child instanceof Xml.Tag && tagMatcher.matches((Xml.Tag) child)) {
                    last = i;
                    if (first == -1) {
                        first = i;
                    }
                }
            }
            if (first == -1) {
                // no need to do add comment
                return super.visitTag(tag, executionContext);
            }

            List<Content> newChildren = new ArrayList<>(children);

            String commentText = "JDK11 upgrade end";
            if (last == children.size() - 1
                    || !(children.get(last + 1) instanceof Xml.Comment)
                    || !((Xml.Comment) children.get(last + 1)).getText().equals(commentText)) {
                Xml.Comment comment = new Xml.Comment(randomId(),
                        children.get(last).getPrefix(),
                        Markers.EMPTY,
                        commentText);
                newChildren.add(last + 1, comment);
            }

            commentText = "JDK11 upgrade start";
            if (first == 0
                    || !(children.get(first - 1) instanceof Xml.Comment)
                    || !((Xml.Comment) children.get(first - 1)).getText().equals(commentText)) {
                Xml.Comment comment = new Xml.Comment(randomId(),
                        children.get(first).getPrefix(),
                        Markers.EMPTY,
                        commentText);
                newChildren.add(first, comment);
            }

            if (children.size() != newChildren.size()) {
                tag = tag.withContent(newChildren);
            }

            return super.visitTag(tag, executionContext);
        }
    }
}
