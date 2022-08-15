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

import org.eclipse.emt4j.analysis.AnalysisMain;
import org.eclipse.emt4j.analysis.common.util.JdkUtil;
import org.eclipse.emt4j.common.JdkMigrationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implement a maven plugin that checks JDK compatible problems.
 * It scans java source files, then checks each file to whether contains incompatible problems.
 * If there is a problem, it will print a warning message.
 */
@Mojo(name = "check")
public class JdkIncompatibleCheckMojo extends AbstractMojo {

    private static final Set<MavenProject> proceeded = new HashSet<>();
    public static final int BATCH_SIZE = 10;

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

    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;

    @Parameter
    private String verbose;

    @Parameter
    private String outputFormat;

    @Parameter
    private String outputFile;

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
        this.outputFile = selectValue(this.outputFile, "outputFile");
        this.projectBuildDir = selectValue(this.projectBuildDir, "projectBuildDir");
        this.outputFormat = selectValue(this.outputFormat, "outputFormat");
    }

    private int selectValue(int value, String propertyKey) {
        String propertyValue = System.getProperty(propertyKey);
        return isEmpty(propertyValue) ? value : Integer.valueOf(propertyValue);
    }

    private String selectValue(String value, String propertyKey) {
        String propertyValue = System.getProperty(propertyKey);
        return isEmpty(propertyValue) ? value : propertyValue;
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
        if ("true".equals(verbose)) {
            args.add("-v");
        }
        String[] checkTargets = projectBuildDir.split(":");
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
