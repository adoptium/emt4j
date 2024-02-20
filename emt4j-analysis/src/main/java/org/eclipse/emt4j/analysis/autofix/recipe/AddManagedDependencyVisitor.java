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
/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.emt4j.analysis.autofix.recipe;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenTagInsertionComparator;
import org.openrewrite.maven.internal.InsertDependencyComparator;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.emptyList;
import static org.eclipse.emt4j.analysis.autofix.recipe.XmlTagHelper.matchTag;
import static org.eclipse.emt4j.analysis.common.Constant.POM_ARTIFACT_ID;
import static org.eclipse.emt4j.analysis.common.Constant.POM_GROUP_ID;

@RequiredArgsConstructor
@EqualsAndHashCode
public class AddManagedDependencyVisitor extends MyMavenVisitor<ExecutionContext> {

    private final String groupId;
    private final String artifactId;
    private final String version;

    @Nullable
    private final String scope;

    @Nullable
    private final String type;

    @Nullable
    private final String classifier;

    @Override
    public Xml visitDocument(Xml.Document document, ExecutionContext ctx) {
        Xml.Document doc = (Xml.Document)super.visitDocument(document, ctx);

        if (documentHasManagedDependency(doc, ctx)) {
            return document;
        }

        Xml.Tag root = document.getRoot();
        List<? extends Content> rootContent = root.getContent() != null ? root.getContent() : emptyList();

        Xml.Tag dependencyManagementTag = root.getChild("dependencyManagement").orElse(null);
        if (dependencyManagementTag == null) {
            doc = (Xml.Document) new AddToTagVisitor<>(root, Xml.Tag.build("<dependencyManagement>\n<dependencies/>\n</dependencyManagement>"),
                    new MavenTagInsertionComparator(rootContent)).visitNonNull(doc, ctx);
        } else if (!dependencyManagementTag.getChild("dependencies").isPresent()) {
            doc = (Xml.Document) new AddToTagVisitor<>(dependencyManagementTag, Xml.Tag.build("\n<dependencies/>\n"),
                    new MavenTagInsertionComparator(rootContent)).visitNonNull(doc, ctx);
        }

        doc = (Xml.Document) new InsertDependencyInOrder(groupId, artifactId, version,
                type, scope, classifier).visitNonNull(doc, ctx);
        return doc;
    }

    private boolean documentHasManagedDependency(Xml.Document doc, ExecutionContext ctx) {
        AtomicBoolean managedDepExists = new AtomicBoolean(false);
        new MyMavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                tag = (Xml.Tag)super.visitTag(tag, executionContext);
                if (isManagedDependencyTag() && matchTag(tag, POM_GROUP_ID, groupId)
                        && matchTag(tag, POM_ARTIFACT_ID, artifactId)) {
                    managedDepExists.set(true);
                }
                return tag;
            }
        }.visitNonNull(doc, ctx);
        return managedDepExists.get();
    }

    @RequiredArgsConstructor
    private static class InsertDependencyInOrder extends MyMavenVisitor<ExecutionContext> {
        private final String groupId;
        private final String artifactId;
        private final String version;
        @Nullable
        private final String type;
        @Nullable
        private final String scope;
        @Nullable
        private final String classifier;

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (isManagedDependenciesTag()) {
                Xml.Tag dependencyTag = Xml.Tag.build(
                        "\n<dependency>\n" +
                                "<groupId>" + groupId + "</groupId>\n" +
                                "<artifactId>" + artifactId + "</artifactId>\n" +
                                "<version>" + version + "</version>\n" +
                                (classifier == null ? "" :
                                        "<classifier>" + classifier + "</classifier>\n") +
                                (type == null || type.equals("jar") ? "" :
                                        "<type>" + type + "</type>\n") +
                                (scope == null || "compile".equals(scope) ? "" :
                                        "<scope>" + scope + "</scope>\n") +
                                "</dependency>"
                );
                doAfterVisit(new AddToTagVisitor<>(tag, dependencyTag,
                        new InsertDependencyComparator(tag.getContent() == null ? emptyList() : tag.getContent(), dependencyTag)));
            }
            return (Xml.Tag)super.visitTag(tag, ctx);
        }
    }
}
