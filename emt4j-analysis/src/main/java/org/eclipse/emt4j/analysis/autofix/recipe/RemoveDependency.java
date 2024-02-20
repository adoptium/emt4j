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
 * Copyright 2020 the original author or authors.
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
import lombok.Value;
import org.eclipse.emt4j.analysis.autofix.XmlTagMatcher;
import org.openrewrite.*;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveDependency extends Recipe {

    private XmlTagMatcher matcher;
    RecipeFixReporter reporter;

    public RemoveDependency(String groupId, String artifactId, RecipeFixReporter reporter) {
        matcher = new XmlTagMatcher.GAXmlTagMatcher(groupId, artifactId);
        this.reporter = reporter;
    }

    @Override
    public String getDisplayName() {
        return "Remove Maven dependency";
    }

    @Override
    public String getDescription() {
        return "Removes a single dependency from the <dependencies> section of the pom.xml.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveDependencyVisitor();
    }

    private class RemoveDependencyVisitor extends MyMavenVisitor<ExecutionContext> {
        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (isDependencyTag() && matcher.matches(tag)) {
                reporter.recordModification(null);
                doAfterVisit(new RemoveContentVisitor<>(tag, true));
            }

            return super.visitTag(tag, ctx);
        }
    }
}
