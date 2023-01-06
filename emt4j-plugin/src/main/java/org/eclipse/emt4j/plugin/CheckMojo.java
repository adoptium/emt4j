/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A mojo for finding the incompatible issues existing in the project.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.TEST)
public class CheckMojo extends BaseCheckMojo {

    /**
     * Dependency Graph Builder
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    boolean preCheck() throws Exception {
        List<MavenProject> projects = session.getProjects();
        initFiles();
        if (project.equals(projects.get(0))) {
            prepare();
        } else {
            load();
        }
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setProject(project);
        DependencyNode root = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifact -> true);
        addModule(root);
        return project.equals(projects.get(projects.size() - 1));
    }

    @Override
    List<String> getCheckTargets() {
        List<String> targets = new ArrayList<>();
        targets.add(configFile.getAbsolutePath());
        List<MavenProject> projects = session.getProjects();
        for (MavenProject project : projects) {
            File file = new File(project.getBuild().getDirectory(), "emt4j.dat");
            if (file.exists() && !file.isDirectory()) {
                System.out.println(file.getAbsolutePath());
                targets.add(file.getAbsolutePath());
            }
        }
        return targets;
    }

    private File configFile;

    private File modulesFile;

    private File dependenciesFile;

    private final List<String> modules = new ArrayList<>();

    private final List<String> dependencies = new ArrayList<>();

    private void initFiles() {
        configFile = new File(session.getExecutionRootDirectory(), ".emt4j");
        modulesFile = new File(configFile, "modules");
        dependenciesFile = new File(configFile, "dependencies");
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "resource"})
    private void prepare() throws IOException {
        if (configFile.exists()) {
            Files.walk(configFile.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        configFile.mkdir();
        modulesFile.createNewFile();
        dependenciesFile.createNewFile();
    }

    private void load() throws IOException {
        modules.clear();
        dependencies.clear();
        BufferedReader br = Files.newBufferedReader(modulesFile.toPath());
        String str;
        while ((str = br.readLine()) != null) {
            String[] pair = str.split("=");
            modules.add(pair[0]);
        }
        br.close();
        br = Files.newBufferedReader(dependenciesFile.toPath());
        while ((str = br.readLine()) != null) {
            String[] pair = str.split("=");
            dependencies.add(pair[0]);
        }
        br.close();
    }

    private String keyOf(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }

    private void addModule(DependencyNode root) throws IOException {
        Artifact artifact = root.getArtifact();
        String key = keyOf(artifact);
        if (modules.contains(key)) {
            return;
        }
        StringBuilder outputs = new StringBuilder();
        String outputDirectory = project.getBuild().getOutputDirectory();
        if (new File(outputDirectory).exists()) {
            outputs.append(outputDirectory);
        }
        String testOutputDirectory = project.getBuild().getTestOutputDirectory();
        if (new File(testOutputDirectory).exists()) {
            if (outputs.length() > 0) {
                outputs.append(File.pathSeparatorChar);
            }
            outputs.append(testOutputDirectory);
        }
        if (outputs.length() > 0) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(modulesFile.toPath(), StandardOpenOption.APPEND)))) {
                bw.write(key + "=" + outputs);
                bw.newLine();
            }
        }
        addDependencies(root);
    }

    private void addDependencies(DependencyNode root) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(dependenciesFile.toPath(), StandardOpenOption.APPEND)))) {
            addDependencies(root, bw);
        }
    }

    private void addDependencies(DependencyNode node, BufferedWriter bw) throws IOException {
        List<DependencyNode> children = node.getChildren();
        if (children == null) {
            return;
        }
        for (DependencyNode child : children) {
            Artifact artifact = child.getArtifact();
            String key = keyOf(artifact);
            if (dependencies.contains(key) || modules.contains(key)) {
                continue;
            }
            ArtifactRepository localRepository = session.getLocalRepository();
            bw.write(key + "=" + new File(localRepository.getBasedir(), localRepository.pathOf(artifact)).getAbsolutePath());
            bw.newLine();
            addDependencies(child, bw);
        }
    }
}
