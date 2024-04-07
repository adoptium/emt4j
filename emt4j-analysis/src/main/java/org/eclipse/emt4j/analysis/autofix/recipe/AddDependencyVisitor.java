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
import org.eclipse.emt4j.analysis.autofix.AutofixReport;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenTagInsertionComparator;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.tree.Xml;

import static java.util.Collections.emptyList;
import static org.eclipse.emt4j.analysis.common.Constant.*;

@EqualsAndHashCode
public class AddDependencyVisitor extends MyMavenVisitor<ExecutionContext> {
    private final String groupId;
    private final String artifactId;
    private final String version;

    @Nullable
    private final String scope;

    @Nullable
    private final String type;

    @Nullable
    private final String classifier;

    @Nullable
    private final Boolean optional;

    public AddDependencyVisitor(String groupId, String artifactId, String version, @Nullable String scope, @Nullable String type, @Nullable String classifier, @Nullable Boolean optional) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.type = type;
        this.classifier = classifier;
        this.optional = optional;
    }

    public String toGATV() {
        String type = this.type == null || this.type.equals(ANY) ? "jar" : this.type;
        return groupId + ":" + artifactId + ":" + type + ":" + version;
    }

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
        // Openrewrite will attempt to visit the same file more than one time using one visitor, so we
        // can not assume dependency has't been added and we must check again
        if (isDependencyTag() &&
                groupId.equals(tag.getChildValue(POM_GROUP_ID).orElse(null)) &&
                artifactId.equals(tag.getChildValue(POM_ARTIFACT_ID).orElse(null))) {
            getCursor().putMessageOnFirstEnclosing(Xml.Document.class, "alreadyHasDependency", true);
            return tag;
        }
        return (Xml.Tag) super.visitTag(tag, executionContext);
    }


    @Override
    public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
        Xml.Document maven = (Xml.Document) super.visitDocument(document, executionContext);

        if (getCursor().getMessage("alreadyHasDependency", false)) {
            return document;
        }

        Xml.Tag root = maven.getRoot();
        if (!root.getChild("dependencies").isPresent()) {
            doAfterVisit(new AddToTagVisitor<>(root, Xml.Tag.build("<dependencies/>"),
                    new MavenTagInsertionComparator(root.getContent() == null ? emptyList() : root.getContent())));
        }

        doAfterVisit(new InsertDependencyInOrder(scope));

        return maven;
    }

    @RequiredArgsConstructor
    private class InsertDependencyInOrder extends MyMavenVisitor<ExecutionContext> {

        @Nullable
        private final String scope;

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (isDependenciesTag()) {
                String versionToUse = version;
                AutofixReport.getInstance().reportChangeForGA(groupId, artifactId, null);

                Xml.Tag dependencyTag = Xml.Tag.build(
                        "\n<dependency>\n" +
                                "<groupId>" + groupId + "</groupId>\n" +
                                "<artifactId>" + artifactId + "</artifactId>\n" +
                                (versionToUse == null ? "" :
                                        "<version>" + versionToUse + "</version>\n") +
                                (classifier == null ? "" :
                                        "<classifier>" + classifier + "</classifier>\n") +
                                (scope == null || "compile" .equals(scope) ? "" :
                                        "<scope>" + scope + "</scope>\n") +
                                (type == null || "jar" .equals(type) ? "" :
                                        "<type>" + type + "</type>\n") +
                                (Boolean.TRUE.equals(optional) ? "<optional>true</optional>\n" : "") +
                                "</dependency>"
                );

                doAfterVisit(new AddToTagVisitor<>(tag, dependencyTag));

                return tag;
            }

            return super.visitTag(tag, ctx);
        }
    }
}
