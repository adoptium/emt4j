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
package org.eclipse.emt4j.analysis.autofix;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.autofix.recipe.MavenProjectLocalData;
import org.eclipse.emt4j.analysis.autofix.recipe.PomResolution;
import org.openrewrite.SourceFile;
import org.openrewrite.xml.tree.Xml;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.eclipse.emt4j.analysis.common.Constant.POM_VERSION;

public class MavenHelper {
    private MavenHelper() {
    }

    private static MavenSession session;
    private static Map<MavenProject, MavenProjectLocalData> projectLocalData = new HashMap<>();

    private static Set<String> propertiesSharedByDifferentGA = new HashSet<>();

    public static void setProjectDependencyRoot(MavenProject project, DTNode root) {
        getProjectData(project).setDependencyRoot(root);
    }

    public static void iterateDependencyTree(BiConsumer<DTNode, MavenProject> consumer) {
        for (MavenProject project : getProjects()) {
            visitDependencyNode(getProjectData(project).getDependencyRoot(), project, consumer);
        }
    }


    public static String buildGatvFromDependencyNode(DTNode node) {
        return node.getGroupId() + ":" + node.getArtifactId() +
                ":" + node.getType() + ":" + node.getVersion();
    }

    public static String buildGavFromDependencyNode(DTNode node) {
        return node.getGroupId() + ":" + node.getArtifactId() +
                ":" + node.getVersion();
    }

    private static void visitDependencyNode(DTNode node, MavenProject project,
                                            BiConsumer<DTNode, MavenProject> consumer) {
        if (node == null) {
            return;
        }
        consumer.accept(node, project);
        if (node.getChildren() != null) {
            for (DTNode child : node.getChildren()) {
                visitDependencyNode(child, project, consumer);
            }
        }
    }

    public static boolean isDependencyNodeFromSession(DTNode node) {
        for (MavenProject project : getProjects()) {
            if (node.getGroupId().equals(project.getGroupId())
                    && node.getArtifactId().equals(project.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    public static MavenSession getSession() {
        return session;
    }

    public static void setSession(MavenSession session) {
        MavenHelper.session = session;
        for (MavenProject project : getProjects()) {
            saveProjectLocalData(new MavenProjectLocalData(project));
        }
    }

    public static List<MavenProject> getProjects() {
        return session.getProjects();
    }

    public static boolean hasChildProject(MavenProject project) {
        for (MavenProject child : MavenHelper.getProjects()) {
            if (project.equals(child.getParent())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isProjectFromSession(MavenProject project) {
        return project != null && projectLocalData.containsKey(project);
    }

    // return the project that this file belongs to, return null if file is not from any project
    public static MavenProject getProjectFromProjectFile(SourceFile file) {
        MavenProject root = null;
        for (MavenProject project : MavenHelper.getProjects()) {
            if (project.isExecutionRoot()) {
                root = project;
            } else if (file.getSourcePath().toFile().getAbsolutePath().startsWith(project.getBasedir().getAbsolutePath())) {
                return project;
            }
        }
        if (root != null && file.getSourcePath().toFile().getAbsolutePath().startsWith(root.getBasedir().getAbsolutePath())) {
            return root;
        }
        return null;
    }

    // return project if this is the pom.xml of a project, otherwise null
    public static MavenProject getProjectFromPom(SourceFile file) {
        // Notice that project.getFile() may return path of .flattened-pom.xml if the project is using
        // flatten-maven-plugin. Here we just want to find the pom.xml.
        String sourcePath = file.getSourcePath().toFile().getAbsolutePath();
        for (MavenProjectLocalData data : projectLocalData.values()) {
            if (sourcePath.equals(data.getPom())) {
                return data.getProject();
            }
        }
        return null;
    }

    public static void saveProjectLocalData(MavenProjectLocalData data) {
        projectLocalData.put(data.getProject(), data);
    }

    public static MavenProjectLocalData getProjectData(MavenProject project) {
        if (project == null) {
            return null;
        }
        return projectLocalData.get(project);
    }

    public static MavenProjectLocalData getDataFromProjectFile(SourceFile file) {
        MavenProject project = getProjectFromProjectFile(file);
        return getProjectData(project);
    }

    public static Xml.Tag findTagRecursively(MavenProject project, Function<MavenProject, Xml.Tag> function) {
        while (MavenHelper.isProjectFromSession(project)) {
            Xml.Tag result = function.apply(project);
            if (result != null) {
                return result;
            }
            project = project.getParent();
        }
        return null;
    }

    public static PomResolution getPomResolution(MavenProject project) {
        return getProjectData(project).getResolution();
    }

    /**
     * Finds a tag that might be updated.
     * If the tag is a version tag and uses a property (like ${version}), checks if the property is shared across different GAs.
     * If the property is shared across different GAs, returns the original tag; otherwise, recursively finds the tag that defines the property value.
     */
    public static Xml.Tag findTagThatMaybeUpdated(MavenProject project, Xml.Tag tag, boolean mustInSameProject) {
        if (tag == null) {
            return null;
        }
        boolean isVersionTag = Objects.equals(tag.getName(), POM_VERSION);
        String value = tag.getValue().orElse("");
        boolean usingProperty = value.startsWith("${") && value.endsWith("}");
        if (isVersionTag && usingProperty) {
            String propertyName = value.substring(2, value.length() - 1);
            // the version is shared by different ga, we must just update version itself
            if (MavenHelper.isPropertySharedByDifferentGA(propertyName)) {
                return tag;
            }
        }
        return findTagRecursivelyThatDefinesValue(project, tag, mustInSameProject);
    }

    // if this tag is using a property like ${version}, return the tag that defines the value of this tag
    // (but still return tag itself if such property definition can not be found)
    // return tag itself if not using property
    public static Xml.Tag findTagRecursivelyThatDefinesValue(MavenProject project, Xml.Tag tag, boolean mustInSameProject) {
        if (tag == null) {
            return null;
        }
        String value = tag.getValue().orElse("");
        boolean usingProperty = value.startsWith("${") && value.endsWith("}");
        if (!usingProperty) {
            return tag;
        }
        String propertyName = value.substring(2, value.length() - 1);
        Xml.Tag result = MavenHelper.findTagRecursively(project, p -> {
            if (mustInSameProject && project != p) {
                return null;
            }
            Optional<Xml.Tag> property = MavenHelper.getPomResolution(p).getProperties().stream()
                    .filter(find -> find.getName().equals(propertyName)).findAny();
            // this tag may be using another property, find it recursively
            if (property.isPresent()) {
                return findTagRecursivelyThatDefinesValue(p, property.get(), mustInSameProject);
            } else {
                return null;
            }
        });
        return result == null ? tag : result;
    }

    public static void iterateDependencyTags(BiConsumer<MavenProject, Xml.Tag> consumer) {
        for (MavenProject project : getProjects()) {
            PomResolution resolution = getPomResolution(project);
            for (Xml.Tag dependency : resolution.getDependencies()) {
                consumer.accept(project, dependency);
            }
            for (Xml.Tag dependency : resolution.getManagedDependencies()) {
                consumer.accept(project, dependency);
            }
        }
    }


    public static void iterateParentTags(BiConsumer<MavenProject, Xml.Tag> consumer) {
        for (MavenProject project : getProjects()) {
            PomResolution resolution = getPomResolution(project);
            consumer.accept(project, resolution.getParentTag());
        }
    }

    public static void iterateModuleSelfTags(BiConsumer<MavenProject, Xml.Tag> consumer) {
        for (MavenProject project : getProjects()) {
            PomResolution resolution = getPomResolution(project);
            consumer.accept(project, resolution.getModuleSelfTag());
        }
    }

    public static MavenProject findProject(Predicate<MavenProject> predicate) {
        return session.getProjects().stream().filter(predicate).findFirst().orElse(null);
    }

    public static void setPropertiesSharedByDifferentGA(Set<String> properties) {
        propertiesSharedByDifferentGA = properties;
    }

    public static boolean isPropertySharedByDifferentGA(String propertyName) {
        return propertiesSharedByDifferentGA.contains(propertyName);
    }
}
