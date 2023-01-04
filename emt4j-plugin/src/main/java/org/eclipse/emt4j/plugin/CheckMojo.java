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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.eclipse.emt4j.analysis.AnalysisMain;
import org.eclipse.emt4j.analysis.common.util.ProcessUtil;
import org.eclipse.emt4j.analysis.common.util.ZipUtil;
import org.eclipse.emt4j.common.JdkMigrationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Implement a maven plugin that checks JDK compatible problems.
 * It scans java source files, then checks each file to whether contains incompatible problems.
 * If there is a problem, it will print a warning message.
 */
@SuppressWarnings("unused")
@Mojo(name = "check", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class CheckMojo extends BaseMojo {

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Parameter(property = "externalToolHome")
    private String externalToolHome;

    @Parameter(property = "targetJDKHome")
    private String targetJDKHome;

    /**
     * Specify a comma separated list of external tools in the form of maven coordinate. The specified external tools
     * shall be automatically downloaded with {@code mvn dependency:get -Dartifact=[tool coordinate]}, and copy to the
     * external tool working home specified by {@link CheckMojo#externalToolHome}.
     * The external tool can be either jar or zip. Assume the external tool home is {@code EXT_HOME}. Specified external
     * tools is {@code -DexternalTools=org1:artifact1:version,org2:artifact2:veresion:zip:classifier}. The {@code artifact1} is
     * jar file, and will be copied to {@code EXT_HOME/org1-artifact1-version}. The {@code artifact2} is a zip file, and
     * will be unzipped to {@code EXT_HOME/org2-artifact2-version-zip-classifier}.
     * <p>
     * In summary, there are two constrains for this options:
     * <ol>
     *     <li>Each item should follow maven-dependency-plugin's idiom: {@code groupId:artifactId:version[:type[:classifier]]}</li>
     *     <li>{@link CheckMojo#externalToolHome} must be set as well.</li>
     * </ol>
     */
    @Parameter
    private List<String> externalTools;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String externalToolsProperty = System.getProperty("externalTools");
        if (externalToolsProperty != null && !externalToolsProperty.isEmpty()) {
            this.externalTools = Arrays.asList(externalToolsProperty.split(","));
        }

        if (fromVersion >= toVersion) {
            throw new JdkMigrationException("fromVersion should less than toVersion");
        }

        List<MavenProject> projects = session.getProjects();
        boolean last = project.equals(projects.get(projects.size() - 1));
        try {
            initFiles();
            if (project.equals(projects.get(0))) {
                prepare();
            } else {
                load();
            }

            ProjectBuildingRequest buildingRequest =
                    new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
            buildingRequest.setProject(project);
            DependencyNode root = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifact -> true);
            addModule(root);

            if (!last) {
                return;
            }
            load();
        } catch (IOException e) {
            throw new MojoExecutionException("IOException", e);
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Failed to build dependency", e);
        }


        if (externalTools != null && !externalTools.isEmpty()) {
            Path localRepoPath = Paths.get(session.getRequest().getLocalRepositoryPath().toURI());
            if (externalToolHome == null || externalToolHome.length() == 0) {
                throw new MojoFailureException("There is no available external tool home set." +
                        " Please set with -DexternalToolHome= in your mvn command line or <externalToolHome> in pom.");
            }
            for (String externalTool : externalTools) {

                Path externalToolHomePath = Paths.get(externalToolHome);
                Path toolPath = externalToolHomePath.resolve(externalTool.replace(":", "-"));
                try {
                    if (Files.notExists(toolPath) || Files.list(toolPath).findAny().isPresent()) {
                        List<String> command = new ArrayList<>();

                        String mvnHome = System.getProperty("maven.home");
                        if (mvnHome == null) {
                            throw new MojoFailureException("System property maven.home is not set. This plugin should be called from mvn command line.");
                        }
                        Path mvnPath = Paths.get(mvnHome).resolve("bin").resolve("mvn");
                        if (Files.notExists(mvnPath)) {
                            throw new MojoFailureException("Can't find mvn executable file " + mvnPath);
                        }
                        command.add(mvnPath.toString());
                        command.add("dependency:get");
                        command.add("-Dartifact=" + externalTool);
                        command.add("-s");
                        command.add(session.getRequest().getUserSettingsFile().toString());
                        try {
                            int ret = ProcessUtil.noBlockingRun(command);
                            if (ret != 0) {
                                throw new MojoFailureException("Fail to download required external tool:" + externalTool);
                            }
                        } catch (IOException | InterruptedException e) {
                            throw new MojoFailureException("Fail to download required external tool:" + externalTool, e);
                        }
                        String[] tokens = externalTool.split(":");
                        String groupId = tokens[0];
                        String artifactId = tokens[1];
                        String version = tokens[2];
                        String type = "jar";
                        String classifier = null;
                        if (tokens.length >= 4) {
                            type = tokens[3];
                        }
                        if (tokens.length == 5) {
                            classifier = tokens[4];
                        }
                        Path artifactPath = localRepoPath.resolve(groupId.replace(".", File.separator)).resolve(artifactId).resolve(version)
                                .resolve(artifactId + "-" + version + "-" + (classifier == null ? "" : classifier) + "." + type);
                        if (Files.exists(artifactPath)) {
                            if (type.equals("jar")) {
                                Files.copy(artifactPath, toolPath.resolve(artifactPath.getFileName()));
                            } else if (type.equals("zip")) {
                                ZipUtil.unzipTo(artifactPath, toolPath);
                            }
                        } else {
                            throw new MojoExecutionException("The external tool " + artifactPath + " doesn't exist.");
                        }
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed when checking existed external tool directory.", e);
                }
            }
        }

        try {
            AnalysisMain.main(buildArgs(resolveOutputFile(), outputFormat));
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            getLog().error(e);
        }
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
            Files.walk(configFile.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
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
            if (outputs.length()> 0) {
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

    private File resolveOutputFile() throws MojoExecutionException {
        if (outputFile != null) {
            Path path = Paths.get(outputFile);
            if (path.isAbsolute()) {
                return new File(outputFile);
            }
        }
        File dir = new File(session.getExecutionRootDirectory());
        if (outputFile == null) {
            outputFile = "report." + (outputFormat == null ? "html" : outputFormat);
        }
        return new File(dir, outputFile);
    }

    private String[] buildArgs(File output, String format) {
        List<String> args = new ArrayList<>();
        param(args, "-f", String.valueOf(fromVersion));
        param(args, "-t", String.valueOf(toVersion));
        param(args, "-p", format);
        param(args, "-o", output.getAbsolutePath());
        param(args, "-e", externalToolHome);
        if (targetJDKHome != null) {
            param(args, "-j", targetJDKHome);
        }
        if (verbose) {
            args.add("-v");
        }
        if (priority != null) {
            param(args, "-priority", priority);
        }
        args.add(configFile.getAbsolutePath());
        return args.toArray(new String[0]);
    }

    private void param(List<String> args, String k, String v) {
        if (v != null && !"".equals(v)) {
            args.add(k);
            args.add(v);
        }
    }
}
