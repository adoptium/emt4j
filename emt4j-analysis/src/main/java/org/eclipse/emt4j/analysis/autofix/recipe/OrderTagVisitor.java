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
import org.eclipse.emt4j.analysis.autofix.XmlTagMatcher;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

// As to the children of parent, tags that satisfies afterMatcher will be put after tags that satisfies afterMatcher.
// beforeMatcher and afterMatcher should not overlap.
@EqualsAndHashCode
public class OrderTagVisitor extends MyMavenVisitor<ExecutionContext> {
    private XmlTagMatcher parentMatcher;
    private XmlTagMatcher beforeMatcher;
    private XmlTagMatcher afterMatcher;

    public OrderTagVisitor(XmlTagMatcher parent, XmlTagMatcher before, XmlTagMatcher after) {
        this.parentMatcher = parent;
        this.beforeMatcher = before;
        this.afterMatcher = after;
    }


    @Override
    public Xml visitTag(Xml.Tag parent, ExecutionContext executionContext) {
        if (!parentMatcher.matches(parent) || parent.getContent() == null) {
            return super.visitTag(parent, executionContext);
        }
        // find tags that we concern
        List<? extends Content> children = parent.getContent();
        int lastBeforeIndex = -1;
        TreeSet<Integer> afterIndexes = new TreeSet<>();
        for (int i = 0; i < children.size(); i++) {
            Content child = children.get(i);
            if (child instanceof Xml.Tag) {
                if (beforeMatcher.matches((Xml.Tag) child)) {
                    lastBeforeIndex = i;
                }
                if (afterMatcher.matches((Xml.Tag) child)) {
                    afterIndexes.add(i);
                }
            }
        }

        boolean alreadyOrdered = lastBeforeIndex == -1 || afterIndexes.isEmpty() ||
                lastBeforeIndex < afterIndexes.first();
        if (alreadyOrdered) {
            return super.visitTag(parent, executionContext);
        }

        // build new children
        final int finalLastBeforeIndex = lastBeforeIndex;
        List<Content> newChildren = new ArrayList<>();
        for (int i = 0; i <= finalLastBeforeIndex; i++) {
            if (!afterIndexes.contains(i)) {
                newChildren.add(children.get(i));
            }
        }
        afterIndexes.stream().filter(i -> i < finalLastBeforeIndex).forEach(i -> newChildren.add(children.get(i)));
        for (int i = finalLastBeforeIndex + 1; i < children.size(); i++) {
            newChildren.add(children.get(i));
        }
        Xml.Tag newParent = parent.withContent(newChildren);
        return super.visitTag(newParent, executionContext);
    }
}
