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

import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.autofix.*;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.eclipse.emt4j.analysis.common.Constant.*;
import static org.eclipse.emt4j.analysis.common.Constant.ANY;

public class PomUpdatePlanGenerator {
    private PomUpdatePlan pomUpdatePlan = new PomUpdatePlan();
    // record dependencies that will be updated
    private Map<String, String> fromGatv2toGatv = new HashMap<>();

    // an index that currently which projects have such dependency
    private Map<String, Set<MavenProject>> gatv2projects = new HashMap<>();

    // record which projects will this dependency be added(not updated) to
    private Map<String, Set<MavenProject>> gatvToAdd2projectToAdd = new HashMap<>();
    private List<String> importGatvsToUpdate = new ArrayList<>();
    private List<String> parentGatvsToUpdate = new ArrayList<>();
    private List<String> moduleSelfGatvsToUpdate = new ArrayList<>();

    private boolean isForceUpdate = false;
    private boolean isAddDependencyVersion = true;

    public PomUpdatePlanGenerator() {
        resolveDependencyToProject();
    }

    public void setForceUpdate(boolean isForceUpdate) {
        this.isForceUpdate = isForceUpdate;
    }

    public void setAddDependencyVersion(boolean isAddDependencyVersion) {
        this.isAddDependencyVersion = isAddDependencyVersion;
    }

    // add dependency if not present, update dependency if present
    public void addDependency(String gatvToAdd) {
        String[] split = gatvToAdd.split(":");
        AutofixReport.getInstance().addReporterForGA(split[0], split[1],
                new AbstractRecipeFixReporter.CountAsOneNoFileProblemRecipeFixReporter("autofix.pom.addOrUpdateDependency", new String[]{split[0] + ":" + split[1], split[3]}));

        Set<MavenProject> projectsToAddDependency = new HashSet<>(MavenHelper.getProjects());
        String gaToAdd = split[0] + ":" + split[1] + ":";
        Version version = new Version(split[3]);
        for (Map.Entry<String, Set<MavenProject>> entry : gatv2projects.entrySet()) {
            if (entry.getKey().startsWith(gaToAdd)) {
                if (new Version(entry.getKey().split(":")[3]).shouldUpdateTo(version)) {
                    fromGatv2toGatv.put(entry.getKey(), gatvToAdd);
                }
                projectsToAddDependency.removeAll(entry.getValue());
            }
        }
        if (!projectsToAddDependency.isEmpty()) {
            gatvToAdd2projectToAdd.put(gatvToAdd, projectsToAddDependency);
        }
    }

    // update from fromGATV to toGATV, caller must guarantee the fromGATV exists and report the change
    public void updateDependency(String fromGATV, String toGATV) {
        fromGatv2toGatv.put(fromGATV, toGATV);
    }

    // update dependency based on the rule
    public void updateDependency(DependencyUpdateRule rule, boolean needReporting) {
        for (Map.Entry<String, Set<MavenProject>> entry : gatv2projects.entrySet()) {
            String fromGATV = entry.getKey();
            if (!rule.matches(fromGATV)) {
                continue;
            }
            String toGATV = rule.getToGATV();
            updateDependency(fromGATV, toGATV);
            if (needReporting) {
                new AbstractRecipeFixReporter.AlwaysOneProblemRecipeFixReporter("autofix.pom.updateDependency"
                        , new String[]{fromGATV, toGATV});
            }
        }
    }

    public void updateImportDependency(String gatv) {
        importGatvsToUpdate.add(gatv);
    }

    public void updateParentDependency(String gatv) {
        parentGatvsToUpdate.add(gatv);
    }

    public void updateModuleSelfDependency(String gatv) {
        moduleSelfGatvsToUpdate.add(gatv);
    }

    public PomUpdatePlan generatePlan() {
        generateUpdateDependencyPlan();
        generateAddDependencyPlan();
        return pomUpdatePlan;
    }

    public Map<String, String> getFromGatv2toGatv() {
        return fromGatv2toGatv;
    }

    private void generateUpdateDependencyPlan() {
        // update dependencies in dependency tree
        for (Map.Entry<String, String> entry : fromGatv2toGatv.entrySet()) {
            String from = entry.getKey();
            String[] fromGatv = from.split(":");
            String to = entry.getValue();
            String[] toGatv = to.split(":");
            Set<MavenProject> projects = gatv2projects.get(from);
            if (projects == null) {
                // should not reach here
                continue;
            }

            if (!moduleSelfGatvsToUpdate.contains(to) && !parentGatvsToUpdate.contains(to)) {
                planToAddDependencyOrUpdateVersion(projects, fromGatv, toGatv);
            }

            planToUpdateGA(projects, fromGatv, toGatv);
        }


        AtomicReference<RecipeFixReporter> reporter = new AtomicReference<>(null);
        // update import scope dependencies
        for (String dependency : importGatvsToUpdate) {
            MavenHelper.iterateDependencyTags((project, tag) -> {
                updatingVersion(dependency, project, tag, reporter);
            });
        }


        for (String dependency : parentGatvsToUpdate) {
            MavenHelper.iterateParentTags((project, tag) -> {
                updatingVersion(dependency, project, tag, reporter);
            });
        }


        for (String dependency : moduleSelfGatvsToUpdate) {
            MavenHelper.iterateModuleSelfTags((project, tag) -> {
                updatingVersion(dependency, project, tag, reporter);
            });
        }

    }

    private void updatingVersion(String dependency, MavenProject project, Xml.Tag tag, AtomicReference<RecipeFixReporter> reporter) {
        String[] toGatv = dependency.split(":");
        if (null == tag || !tagMatchGat(tag, toGatv)) {
            return;
        }

        boolean versionUpdated = updateTag(tag.getChild(POM_VERSION), toGatv[3], project);
        if (versionUpdated) {
            if (reporter.get() == null) {
                reporter.set(new AbstractRecipeFixReporter.AlwaysOneProblemRecipeFixReporter("autofix.pom.updateDependency"
                        , new String[]{toGatv[0], toGatv[1]}));
            }
            reporter.get().recordModification(null);
        }
    }

    private void generateAddDependencyPlan() {
        for (Map.Entry<String, Set<MavenProject>> entry : gatvToAdd2projectToAdd.entrySet()) {
            String[] gatv = entry.getKey().split(":");
            for (MavenProject project : entry.getValue()) {
                project = getProjectRoot(project);
                if (!noNeedToAddDependency(project)) {
                    addDependency(project, gatv);
                }
            }
        }
    }

    private void planToUpdateGA(Set<MavenProject> projects, String[] fromGatv, String[] toGatv) {
        if (fromGatv[0].equals(toGatv[0]) && fromGatv[1].equals(toGatv[1])) {
            return;
        }
        XmlTagMatcher.GAXmlTagMatcher fromMatcher = new XmlTagMatcher.GAXmlTagMatcher(fromGatv[0], fromGatv[1]);
        XmlTagMatcher.GAXmlTagMatcher toMatcher = new XmlTagMatcher.GAXmlTagMatcher(toGatv[0], toGatv[1]);
        Set<MavenProject> projectSet = new HashSet<>();
        for (MavenProject project : projects) {
            while (MavenHelper.isProjectFromSession(project) && !projectSet.contains(project)) {
                MavenProject thisProject = project;
                PomResolution res = MavenHelper.getPomResolution(project);
                boolean alreadyHasDependency = res.getDependencies().stream().anyMatch(toMatcher::matches);
                res.getDependencies().stream()
                        .filter(fromMatcher::matches)
                        .forEach(tag -> {
                            if (alreadyHasDependency) {
                                removeTag(tag, thisProject);
                            } else {
                                updateDependencyGATag(tag, toGatv, thisProject);
                            }
                        });
                boolean alreadyHasDependency2 = res.getManagedDependencies().stream().anyMatch(toMatcher::matches);
                res.getManagedDependencies().stream()
                        .filter(fromMatcher::matches)
                        .forEach(tag -> {
                            if (alreadyHasDependency2) {
                                removeTag(tag, thisProject);
                            } else {
                                updateDependencyGATag(tag, toGatv, thisProject);
                            }
                        });
                projectSet.add(project);
                project = project.getParent();
            }
        }
    }

    private void removeTag(Xml.Tag tag, MavenProject project) {
        RemoveContentVisitor<ExecutionContext> visitor = new RemoveContentVisitor<>(tag, false);
        pomUpdatePlan.getProjectUpdateVisitors(project).add(visitor);
    }

    private void planToAddDependencyOrUpdateVersion(Set<MavenProject> projects, String[] fromGatv, String[] toGatv) {
        for (MavenProject project : projects) {
            // this project has dependency version?
            Xml.Tag dependency = MavenHelper.getPomResolution(project).getDependencies().stream()
                    .filter((tag) -> tagMatchGat(tag, fromGatv)).findAny().orElse(null);
            if (dependency != null && dependency.getChild(POM_VERSION).isPresent()) {
                updateDependencyVersionTag(dependency, toGatv, project);
                continue;
            }
            // dependency management version?
            AtomicReference<MavenProject> projectWithThisTag = new AtomicReference<>(null);
            Xml.Tag dependencyTag = MavenHelper.findTagRecursively(project, (p) -> {
                Xml.Tag d = MavenHelper.getPomResolution(p).getManagedDependencies().stream()
                        .filter((tag) -> tagMatchGat(tag, fromGatv)).findAny().orElse(null);
                if (d != null && d.getChild(POM_VERSION).isPresent()) {
                    projectWithThisTag.set(p);
                    return d;
                }
                return null;
            });
            if (dependencyTag != null) {
                updateDependencyVersionTag(dependencyTag, toGatv, project);
                continue;
            }
            // parent has dependency?
            projectWithThisTag.set(null);
            dependencyTag = MavenHelper.findTagRecursively(project.getParent(), (p) -> {
                Xml.Tag d = MavenHelper.getPomResolution(p).getDependencies().stream()
                        .filter((tag) -> tagMatchGat(tag, fromGatv)).findAny().orElse(null);
                if (d != null && d.getChild(POM_VERSION).isPresent()) {
                    projectWithThisTag.set(p);
                    return d;
                }
                return null;
            });
            if (dependencyTag != null) {
                updateDependencyVersionTag(dependencyTag, toGatv, projectWithThisTag.get());
                continue;
            }
            // add to root
            MavenProject root = getProjectRoot(project);
            addManagedDependency(root, toGatv);
        }
    }

    private void addDependency(MavenProject project, String[] gatv) {
        MyMavenVisitor<ExecutionContext> visitor = new AddDependencyVisitor(gatv[0], gatv[1], isAddDependencyVersion ? gatv[3] : null, null,
                ANY.equals(gatv[2]) ? null : gatv[2], null, false);
        pomUpdatePlan.getProjectUpdateVisitors(project).add(visitor);
    }

    private void addManagedDependency(MavenProject project, String[] gatv) {
        MyMavenVisitor<ExecutionContext> visitor = new AddManagedDependencyVisitor(gatv[0], gatv[1], gatv[3], null,
                ANY.equals(gatv[2]) ? null : gatv[2], null);
        pomUpdatePlan.getProjectUpdateVisitors(project).add(visitor);
    }

    private void updateDependencyVersionTag(Xml.Tag dependency, String[] toGatv, MavenProject project) {
        Optional<Xml.Tag> g = dependency.getChild(POM_GROUP_ID);
        Optional<Xml.Tag> a = dependency.getChild(POM_ARTIFACT_ID);
        // we don't update g and a here
        boolean versionUpdated = updateTag(dependency.getChild(POM_VERSION), toGatv[3], project);
        if (g.isPresent() && a.isPresent() && versionUpdated) {
            AutofixReport.getInstance().reportChangeForGA(
                    g.get().getValue().orElse(null), a.get().getValue().orElse(null), null);
        }
    }

    private void updateDependencyGATag(Xml.Tag dependency, String[] toGatv, MavenProject project) {
        Optional<Xml.Tag> g = dependency.getChild(POM_GROUP_ID);
        Optional<Xml.Tag> a = dependency.getChild(POM_ARTIFACT_ID);
        updateTag(g, toGatv[0], project);
        updateTag(a, toGatv[1], project);
    }

    private MavenProject getProjectRoot(MavenProject project) {
        while (MavenHelper.isProjectFromSession(project.getParent())) {
            project = project.getParent();
        }
        return project;
    }

    // return if any change is made
    private boolean updateTag(Optional<Xml.Tag> tagOptional, String to, MavenProject project) {
        if (!tagOptional.isPresent()) {
            return false;
        }
        Xml.Tag tag = tagOptional.get();
        boolean isVersionTag = POM_VERSION.equals(tag.getName());
        Xml.Tag tagToUpdate = MavenHelper.findTagRecursivelyThatDefinesValue(project, tag, false);
        return saveTagUpdatePlan(tagToUpdate, to, isVersionTag);
    }

    // return if any change is made
    private boolean saveTagUpdatePlan(Xml.Tag tag, String to, boolean isVersionUpdate) {
        String from = tag.getValue().orElse("");
        if (pomUpdatePlan.getTagUpdatePlan().containsKey(tag) || to.equals(from)) {
            return false;
        }
        if (isVersionUpdate && !isForceUpdate && !new Version(from).shouldUpdateTo(new Version(to))) {
            return false;
        }

        pomUpdatePlan.getTagUpdatePlan().put(tag, new ChangeTagValueVisitor<>(tag, to));
        return true;
    }

    private boolean tagMatchGat(Xml.Tag tag, String[] gatv) {
        Optional<String> g = tag.getChildValue(POM_GROUP_ID);
        Optional<String> a = tag.getChildValue(POM_ARTIFACT_ID);
        Optional<String> t = tag.getChildValue(POM_TYPE);
        if (!g.isPresent() || !a.isPresent()) {
            return false;
        }
        return gatv[0].equals(g.get()) && gatv[1].equals(a.get())
                && (!t.isPresent() || ANY.equals(gatv[2]) || gatv[2].equals(t.get()));
    }

    private boolean noNeedToAddDependency(MavenProject project) {
        return MavenHelper.getProjectData(project).getJavaSourceCount() == 0 && !MavenHelper.hasChildProject(project);
    }

    private void resolveDependencyToProject() {
        MavenHelper.iterateDependencyTree((node, project) -> {
            String gatv = MavenHelper.buildGatvFromDependencyNode(node);
            Set<MavenProject> projects = gatv2projects.computeIfAbsent(gatv, (k) -> new HashSet<>());
            projects.add(project);
        });
    }
}
