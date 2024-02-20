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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;

import static org.openrewrite.internal.StringUtils.matchesGlob;
import static org.openrewrite.xml.FilterTagChildrenVisitor.filterTagChildren;
import static org.eclipse.emt4j.analysis.common.Constant.*;

@EqualsAndHashCode(callSuper = true)
public class RemovePluginDependency extends Recipe {

    private RecipeFixReporter reporter;
    @Option(displayName = "Plugin Group",
            description = "GroupId of the plugin from which the dependency will be removed. Supports glob." +
                    "A GroupId is the first part of a dependency coordinate 'org.openrewrite.maven:rewrite-maven-plugin:VERSION'.",
            example = "org.openrewrite.maven")
    String pluginGroupId;

    @Option(displayName = "Plugin Artifact",
            description = "ArtifactId of the plugin from which the dependency will be removed. Supports glob." +
                    "The second part of a dependency coordinate 'org.openrewrite.maven:rewrite-maven-plugin:VERSION'.",
            example = "rewrite-maven-plugin")
    String pluginArtifactId;

    @Option(displayName = "Group",
            description = "The first part of a plugin dependency coordinate. Supports glob.",
            example = "com.google.guava")
    String groupId;

    @Option(displayName = "Artifact",
            description = "The second part of a plugin dependency coordinate. Supports glob.",
            example = "guava")
    String artifactId;

    @Override
    public String getDisplayName() {
        return "Remove Maven plugin dependency";
    }

    @Override
    public String getDescription() {
        return "Removes a dependency from the <dependencies> section of a plugin in the pom.xml.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveDependencyVisitor();
    }

    public RemovePluginDependency(String pluginGroupId, String pluginArtifactId, String groupId, String artifactId) {
        this.pluginGroupId = pluginGroupId;
        this.pluginArtifactId = pluginArtifactId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        reporter = new AbstractRecipeFixReporter.CountAsOneNoFileProblemRecipeFixReporter("autofix.java.removePluginOption", new String[]{pluginGroupId + ":" + pluginArtifactId, groupId + ":" + artifactId});
    }

    private class RemoveDependencyVisitor extends MyMavenVisitor<ExecutionContext> {
        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag plugins = (Xml.Tag) super.visitTag(tag, ctx);
            if (!isPluginsTag(tag)) {
                return plugins;
            }
            Optional<Xml.Tag> maybePlugin = plugins.getChildren().stream()
                    .filter(plugin ->
                            POM_PLUGIN.equals(plugin.getName()) &&
                                    childValueMatches(plugin, POM_GROUP_ID, pluginGroupId) &&
                                    childValueMatches(plugin, POM_ARTIFACT_ID, pluginArtifactId)
                    )
                    .findAny();
            if (!maybePlugin.isPresent()) {
                return plugins;
            }
            Xml.Tag plugin = maybePlugin.get();
            Optional<Xml.Tag> maybeDependencies = plugin.getChild("dependencies");
            if (!maybeDependencies.isPresent()) {
                return plugins;
            }
            Xml.Tag dependencies = maybeDependencies.get();
            int dependencyCount = dependencies.getChildren().size();
            plugins = filterTagChildren(plugins, dependencies, dependencyTag ->
                    !(childValueMatches(dependencyTag, POM_GROUP_ID, groupId)
                            && childValueMatches(dependencyTag, POM_ARTIFACT_ID, artifactId))
            );
            plugins = filterTagChildren(plugins, plugin, pluginChildTag ->
                    !(pluginChildTag.getName().equals("dependencies") && pluginChildTag.getChildren().isEmpty()));

            if (plugins.getChildren().size() != dependencyCount) {
                reporter.recordModification(null);
            }

            return plugins;
        }

        private boolean childValueMatches(Xml.Tag tag, String childValueName, String globPattern) {
            return tag.getChildValue(childValueName).map(it -> matchesGlob(it, globPattern)).orElse(false);
        }
    }
}
