/********************************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.AnalysisMain;
import org.eclipse.emt4j.analysis.common.util.ProcessUtil;
import org.eclipse.emt4j.analysis.common.util.ZipUtil;
import org.eclipse.emt4j.common.JdkMigrationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implement a maven plugin that checks JDK compatible problems.
 * It scans java source files, then checks each file to whether contains incompatible problems.
 * If there is a problem, it will print a warning message.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
@Execute(phase = LifecyclePhase.PROCESS_CLASSES)
public class JdkIncompatibleCheckMojo extends AbstractMojo {

    private static final Set<MavenProject> proceeded = new HashSet<>();
    public static final int BATCH_SIZE = 10;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;
    /**
     * Define files that need to exclude
     */
    @Parameter
    private List<String> excludes;

    /**
     * Define files that need to include
     */
    @Parameter
    private List<String> includes;

    /**
     * Define the from JDK version, now support: 8 and 11
     */
    @Parameter
    private int fromVersion;

    /**
     * Define the target JDK version, now support 11 and 17
     */
    @Parameter
    private int toVersion;

    @Parameter
    private String priority;

    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;

    @Parameter
    private String verbose;

    @Parameter
    private String outputFormat;

    @Parameter
    private String outputFile;

    @Parameter
    private String externalToolHome;

    /**
     * Specify a comma separated list of external tools in the form of maven coordinate. The specified external tools
     * shall be automatically downloaded with {@code mvn dependency:get -Dartifact=[tool coordinate]}, and copy to the
     * external tool working home specified by {@link JdkIncompatibleCheckMojo#externalToolHome}.
     * The external tool can be either jar or zip. Assume the external tool home is {@code EXT_HOME}. Specified external
     * tools is {@code -DexternalTools=org1:artifact1:version,org2:artifact2:veresion:zip:classifier}. The {@code artifact1} is
     * jar file, and will be copied to {@code EXT_HOME/org1-artifact1-version}. The {@code artifact2} is a zip file, and
     * will be unzipped to {@code EXT_HOME/org2-artifact2-version-zip-classifier}.
     * <p>
     * In summary, there are two constrains for this options:
     * <ol>
     *     <li>Each item should follow maven-dependency-plugin's idiom: {@code groupId:artifactId:version[:type[:classifier]]}</li>
     *     <li>{@link JdkIncompatibleCheckMojo#externalToolHome} must be set as well.</li>
     * </ol>
     */
    @Parameter
    private List<String> externalTools;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject project = (MavenProject) getPluginContext().get("project");
        selectParameters();
        boolean verbose = "true".equals(this.verbose);
        if (verbose) {
            getLog().info("Target directory: " + projectBuildDir);
        }
        if (proceeded.contains(project)) {
            return;
        }
        if (StringUtils.isEmpty(projectBuildDir)) {
            getLog().error("Project build directory is empty,so skip this project!");
            return;
        }
        if (fromVersion >= toVersion) {
            throw new JdkMigrationException("fromVersion should less than toVersion");
        }

        if (externalTools != null && !externalTools.isEmpty()) {
            Path localRepoPath = Paths.get(session.getRequest().getLocalRepositoryPath().toURI());
            if (externalToolHome == null && externalToolHome.length() == 0) {
                throw new MojoFailureException("There is no available external tool home set." +
                        " Please set with -DexternalToolHome= in your mvn command line or <externalToolHome> in pom.");
            }
            for (String externalTool : externalTools) {

                Path externalToolHomePath = Paths.get(externalToolHome);
                Path toolPath = externalToolHomePath.resolve(externalTool.replace(":", "-"));
                try {
                    if (Files.notExists(toolPath) || Files.list(toolPath).count() > 0) {
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
            if (isEmpty(outputFile)) {
                File tmpOutput = File.createTempFile("jdk-incompatible-plugin-check", ".txt");
                tmpOutput.deleteOnExit();
                AnalysisMain.main(buildArgs(tmpOutput, "txt"));
                List<String> lines = FileUtils.readLines(tmpOutput, "UTF-8");
                if (lines.size() > 0) {
                    getLog().warn(String.join("\n", lines));
                }
            } else {
                AnalysisMain.main(buildArgs(new File(outputFile), outputFormat));
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    private void selectParameters() {
        this.verbose = selectValue(this.verbose, "verbose");
        this.fromVersion = selectValue(this.fromVersion, "fromVersion");
        this.toVersion = selectValue(this.toVersion, "toVersion");
        this.priority = selectValue(this.priority, "priority");
        this.outputFile = selectValue(this.outputFile, "outputFile");
        this.projectBuildDir = selectValue(this.projectBuildDir, "projectBuildDir");
        this.outputFormat = selectValue(this.outputFormat, "outputFormat");
        this.externalToolHome = selectValue(this.externalToolHome, "externalToolHome");
        this.externalTools = selectValue(this.externalTools, "externalTools");
    }

    private int selectValue(int value, String propertyKey) {
        String propertyValue = System.getProperty(propertyKey);
        return isEmpty(propertyValue) ? value : Integer.valueOf(propertyValue);
    }

    private String selectValue(String value, String propertyKey) {
        String propertyValue = System.getProperty(propertyKey);
        return isEmpty(propertyValue) ? value : propertyValue;
    }

    private List<String> selectValue(List<String> value, String propertyKey) {
        String propertyValue = System.getProperty(propertyKey);
        if (isEmpty(propertyValue)) {
            return value;
        } else {
            String[] vals = propertyValue.split(",");
            List<String> ret = new ArrayList<>(vals.length);
            for (String val : vals) {
                ret.add(val);
            }
            return ret;
        }
    }

    private boolean isEmpty(String value) {
        return null == value || "".equals(value);
    }

    private String[] buildArgs(File output, String format) {
        List<String> args = new ArrayList<>();
        param(args, "-f", String.valueOf(fromVersion));
        param(args, "-t", String.valueOf(toVersion));
        param(args, "-p", format);
        param(args, "-o", output.getAbsolutePath());
        param(args, "-e", externalToolHome);
        if ("true".equals(verbose)) {
            args.add("-v");
        }
        if (priority != null) {
            param(args, "-priority", priority);
        }
        String[] checkTargets = projectBuildDir.split(File.pathSeparator);
        for (String checkTarget : checkTargets) {
            args.add(checkTarget);
        }
        return args.toArray(new String[args.size()]);
    }

    private void param(List<String> args, String k, String v) {
        if (v != null && !"".equals(v)) {
            args.add(k);
            args.add(v);
        }
    }
}
