/********************************************************************************
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.eclipse.emt4j.analysis.autofix.AutofixConfig;
import org.eclipse.emt4j.analysis.autofix.BaseAutofixExecutor;
import org.eclipse.emt4j.analysis.autofix.DTNode;
import org.eclipse.emt4j.analysis.autofix.MavenHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Provides both check and autofix ability.
 */
public abstract class BaseCheckAndAutofixMojo extends BaseCheckMojo {

    /**
     * Dependency Graph Builder
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Component
    protected RuntimeInformation runtime;

    @Component
    protected SettingsDecrypter settingsDecrypter;

    @Parameter(property = "autofix", defaultValue = "false")
    protected boolean autofix;

    @Parameter(property = "skipDeps", defaultValue = "false")
    protected boolean skipDeps;

    @Parameter(property = "autofixFile", defaultValue = "emt4j-autofix.patch")
    protected String autofixFile;

    @Parameter(property = "autofixGeneratePatch", defaultValue = "true")
    protected boolean autofixGeneratePatch;

    @Parameter(property = "unfixedReportFileName", defaultValue = "emt4j-autofix-unfixed.html")
    protected String unfixedReportFile;

    @Parameter(property = "fixedReportFileName", defaultValue = "emt4j-autofix-fixed.html")
    protected String fixedReportFile;

    @Parameter(property = "check", defaultValue = "true")
    protected boolean check;

    private void prepareForAutofix() {
        AutofixConfig config = AutofixConfig.getInstance();
        MavenHelper.setSession(session);
        rebuildDependencyTreesFromFile();
        prepareAutofixConfig(config);
        BaseAutofixExecutor.setInstance(getAutofixExecutor());
    }

    protected void prepareAutofixConfig(AutofixConfig config) {
        config.setAutofix(autofix);
        config.setAutofixFile(autofixFile);
        config.setAutofixGeneratePatch(autofixGeneratePatch);
        config.setUnfixedReportFile(unfixedReportFile);
        config.setFixedReportFile(fixedReportFile);
        config.setFromVersion(fromVersion);
        config.setToVersion(toVersion);
    }

    @Override
    boolean preCheck() throws Exception {
        // Notice: the context classloader of current thread may change when preCheck() is called by different project.
        // So we MUST NOT save any data in class field before the last project arrives. Data should be saved in file
        // if necessary
        List<MavenProject> projects = session.getProjects();
        initFiles();
        if (project.equals(projects.get(0))) {
            prepare();
        } else {
            load();
        }
        if (project.isExecutionRoot()) {
            writeRoot();
        }
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setProject(project);
        DependencyNode root = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifact -> true);
        addModule(root);
        boolean last = project.equals(projects.get(projects.size() - 1));
        if (last) {
            // Now it is safe to initialize other classes
            System.setProperty("emt4j.disableCheck", String.valueOf(!check));
            prepareForAutofix();
            if (!check && !autofix) {
                throw new RuntimeException("At least one of -Dcheck and -Dautofix must be true");
            }
        }
        return last;
    }

    @Override
    List<String> getCheckTargets() {
        List<String> targets = new ArrayList<>();
        targets.add(configFile.getAbsolutePath());
        List<MavenProject> projects = session.getProjects();
        for (MavenProject project : projects) {
            File file = new File(project.getBuild().getDirectory(), "emt4j.dat");
            if (file.exists() && !file.isDirectory()) {
                targets.add(file.getAbsolutePath());
            }
        }
        return targets;
    }

    private File configFile;

    private File modulesFile;

    private File dependenciesFile;

    private File dependencyTreeFile;

    private final List<String> modules = new ArrayList<>();

    private final List<String> dependencies = new ArrayList<>();

    private void initFiles() {
        configFile = new File(session.getExecutionRootDirectory(), ".emt4j");
        modulesFile = new File(configFile, "modules");
        dependenciesFile = new File(configFile, "dependencies");
        dependencyTreeFile = new File(configFile, "dependencyTree");
    }

    private void writeRoot() throws IOException {
        File root = new File(configFile, "root");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(root.toPath(), StandardOpenOption.CREATE_NEW), StandardCharsets.UTF_8))) {
            String coordinate = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
            String name = project.getName();
            if (name.endsWith("-parent") || name.endsWith(".parent")) {
                name = name.substring(0, name.length() - 7);
            }
            bw.write(name + "@" + coordinate);
            bw.newLine();
        }
    }

    private String readRoot() throws IOException {
        try (BufferedReader br = Files.newBufferedReader(new File(configFile, "root").toPath())) {
            return br.readLine();
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "resource"})
    private void prepare() throws IOException {
        if (configFile.exists()) {
            Files.walk(configFile.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        configFile.mkdir();
        modulesFile.createNewFile();
        dependenciesFile.createNewFile();
        dependencyTreeFile.createNewFile();
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

    private String artifactToString(Artifact artifact) {
        // gatvs
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType() + ":" + artifact.getBaseVersion() + ":" + artifact.getScope();
    }

    private DTNode dtNodeFromString(String artifact) {
        String[] parts = artifact.split(":");
        DTNode node = new DTNode();
        node.setGroupId(parts[0]);
        node.setArtifactId(parts[1]);
        node.setType(parts[2]);
        node.setVersion(parts[3]);
        node.setScope(parts[4]);
        return node;
    }

    // this key is for deduplication
    private String keyOf(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion();
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
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(modulesFile.toPath(), StandardOpenOption.APPEND), StandardCharsets.UTF_8))) {
                bw.write(key + "=" + outputs);
                bw.newLine();
            }
        }
        if (!skipDeps) {
            System.setProperty("emt4j.dependencyTree", dependencyTreeFile.getAbsolutePath());
            recordDependencyTree(root);
            recordDependencies(root);
        }
    }

    private void recordDependencyTree(DependencyNode node) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(dependencyTreeFile.toPath(), StandardOpenOption.APPEND), StandardCharsets.UTF_8))) {
            recordDependencyTree(bw, node, 0);
        }
    }

    private void recordDependencyTree(BufferedWriter bw, DependencyNode node, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            bw.write(" ");
        }

        bw.write(artifactToString(node.getArtifact()));
        bw.newLine();

        List<DependencyNode> children = node.getChildren();

        if (children == null) {
            return;
        }

        for (DependencyNode child : node.getChildren()) {
            recordDependencyTree(bw, child, indent + 1);
        }
    }

    private void recordDependencies(DependencyNode root) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(dependenciesFile.toPath(), StandardOpenOption.APPEND), StandardCharsets.UTF_8))) {
            recordDependencies(root, bw);
        }
    }

    private void recordDependencies(DependencyNode node, BufferedWriter bw) throws IOException {
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
            recordDependencies(child, bw);
        }
    }

    private void rebuildDependencyTreesFromFile() {
        DTNode lastNode = null;
        int lastIndent = 0;
        Pattern pattern = Pattern.compile("( *)(.*)");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dependencyTreeFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    throw new RuntimeException("Invalid line: " + line);
                }

                int indent = matcher.group(1).length();
                String content = matcher.group(2);
                DTNode node = dtNodeFromString(content);

                if (indent == 0) {
                    // meet new project
                    MavenProject rootProject = MavenHelper.findProject(p -> p.getArtifact().getGroupId().equals(node.getGroupId())
                            && p.getArtifact().getArtifactId().equals(node.getArtifactId()));
                    if (rootProject == null) {
                        throw new RuntimeException("should not reach here: parsing " + line);
                    }
                    MavenHelper.setProjectDependencyRoot(rootProject, node);
                } else {
                    int parentLevel = lastIndent - indent + 1;
                    for (int i = 0; i < parentLevel; i++) {
                        lastNode = lastNode.getParent();
                    }
                    lastNode.addChild(node);
                    node.setParent(lastNode);
                }
                lastNode = node;
                lastIndent = indent;
            }
        } catch (Exception e) {
            System.err.println("Failed to read dependency tree: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected BaseAutofixExecutor getAutofixExecutor() {
        throw new UnsupportedOperationException();
    }
}
