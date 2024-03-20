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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.autofix.DTNodeMatcher;
import org.eclipse.emt4j.analysis.autofix.DTNode;
import org.eclipse.emt4j.analysis.autofix.MavenHelper;
import org.eclipse.emt4j.analysis.autofix.recipe.AbstractRecipeFixReporter.CountAsOneNoFileProblemRecipeFixReporter;
import org.eclipse.emt4j.common.util.FileUtil;
import org.openrewrite.Recipe;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyExclusionRecipeMaker {
    public List<DTNodeMatcher> exclusions = new ArrayList<>();

    public DependencyExclusionRecipeMaker() {
        // these jars no longer exists in jdk11
        exclusions.add(new JDKRemovedInternalJarMatcher());
        // conflict with javax.annotation:javax.annotation-api:1.3.2, will see this exception if not excluded:
        // java.lang.NoSuchMethodError: 'java.lang.String javax.annotation.Resource.lookup()'
        exclusions.add(new DTNodeMatcher.GATVSNodeMatcher("javax.annotation", "jsr250-api"));
    }

    public List<Recipe> getDependencyExclusionRecipes() {
        Map<String, Set<String>> exclusion2Dependency = new HashMap<>();
        MavenHelper.iterateDependencyTree((node, project) -> {
            if (shouldBeExcluded(node)) {
                String exclusionGroupID = node.getGroupId();
                String exclusionArtifactID = node.getArtifactId();
                String exclusionGA = exclusionGroupID + ":" + exclusionArtifactID;

                node = node.getParent();
                exclusion2Dependency.putIfAbsent(exclusionGA, new HashSet<>());
                while (node != null && !MavenHelper.isDependencyNodeFromSession(node)) {
                    exclusion2Dependency.get(exclusionGA)
                            .add(node.getGroupId() + ":" + node.getArtifactId());
                    node = node.getParent();
                }
            }
        });
        List<Recipe> result = exclusion2Dependency.entrySet().stream().flatMap((entry) -> {
            String[] exclusionGA = entry.getKey().split(":");
            RecipeFixReporter reporter = new CountAsOneNoFileProblemRecipeFixReporter(
                    "autofix.pom.excludeDependency", new String[]{entry.getKey()});
            return Stream.concat(Stream.of(new RemoveDependency(exclusionGA[0], exclusionGA[1], reporter)),
                    entry.getValue().stream().map(dependency -> {
                        String[] dependencyGA = dependency.split(":");
                        return new ExcludeDependency(dependencyGA[0], dependencyGA[1], exclusionGA[0], exclusionGA[1], reporter);
                    }));
        }).collect(Collectors.toList());
        return result;
    }

    private boolean shouldBeExcluded(DTNode node) {
        return exclusions.stream().anyMatch((exclusion) -> exclusion.matches(node));
    }

    private static class JDKRemovedInternalJarMatcher implements DTNodeMatcher {

        private static final String[] JDK_REMOVED_INTERNAL_JARS = new String[]{
                "jre/languages/nfi/truffle-nfi.jar",
                "jre/languages/nfi/truffle-nfi-libffi.jar",
                "jre/lib/security/policy/limited/US_export_policy.jar",
                "jre/lib/security/policy/limited/local_policy.jar",
                "jre/lib/security/policy/unlimited/US_export_policy.jar",
                "jre/lib/security/policy/unlimited/local_policy.jar",
                "jre/lib/graalvm/launcher-common.jar",
                "jre/lib/truffle/truffle-tck.jar",
                "jre/lib/truffle/truffle-api.jar",
                "jre/lib/truffle/truffle-dsl-processor.jar",
                "jre/lib/truffle/locator.jar",
                "jre/lib/jvmci-services.jar",
                "jre/lib/jvmci/graal-truffle-jfr-impl.jar",
                "jre/lib/jvmci/jvmci-api.jar",
                "jre/lib/jvmci/graal-management.jar",
                "jre/lib/jvmci/jvmci-hotspot.jar",
                "jre/lib/jvmci/graal.jar",
                "jre/lib/management-agent.jar",
                "jre/lib/jsse.jar",
                "jre/lib/graal/graal-processor.jar",
                "jre/lib/ext/cldrdata.jar",
                "jre/lib/ext/nashorn.jar",
                "jre/lib/ext/jaccess.jar",
                "jre/lib/ext/zipfs.jar",
                "jre/lib/ext/sunpkcs11.jar",
                "jre/lib/ext/sunjce_provider.jar",
                "jre/lib/ext/sunec.jar",
                "jre/lib/ext/localedata.jar",
                "jre/lib/ext/dnsns.jar",
                "jre/lib/ext/alikryo.jar",
                "jre/lib/resources.jar",
                "jre/lib/charsets.jar",
                "jre/lib/jce.jar",
                "jre/lib/boot/graal-sdk.jar",
                "jre/lib/rt.jar",
                "jre/lib/jfr.jar",
                "lib/sa-jdi.jar",
                "lib/tools.jar",
                "lib/jconsole.jar",
                "lib/dt.jar",
        };
        private final Set<Artifact> targets = new HashSet<>();

        public JDKRemovedInternalJarMatcher() {
            String javaHome = System.getenv("JAVA_HOME");
            if (StringUtils.isEmpty(javaHome)) {
                System.out.println("Exclusion jdk removed internal jar recipe is disabled because JAVA_HOME is not set.");
                return;
            }

            List<File> removedJars = Arrays.stream(JDK_REMOVED_INTERNAL_JARS)
                    .map(jar -> new File(javaHome + "/" + jar))
                    .collect(Collectors.toList());
            List<MavenProject> projects = MavenHelper.getProjects();
            for (MavenProject project : projects) {
                Set<Artifact> artifacts = project.getArtifacts();
                if (artifacts == null) {
                    continue;
                }
                for (Artifact artifact : artifacts) {
                    if (!Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
                        continue;
                    }
                    File file = artifact.getFile();
                    if (file == null) {
                        continue;
                    }
                    for (File removedJar : removedJars) {
                        if (FileUtil.isSameFile(removedJar, file)) {
                            targets.add(artifact);
                        }
                    }
                }
            }
        }

        @Override
        public boolean matches(DTNode artifact) {
            for (Artifact target : targets) {
                if (target.getGroupId().equals(artifact.getGroupId())
                        && target.getArtifactId().equals(artifact.getArtifactId())
                        && target.getVersion().equals(artifact.getVersion())
                        && target.getScope().equals(artifact.getScope())) {
                    return true;
                }
            }
            return false;
        }
    }
}
