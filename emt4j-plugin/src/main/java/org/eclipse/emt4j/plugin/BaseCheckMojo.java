/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.emt4j.analysis.common.util.ProcessUtil;
import org.eclipse.emt4j.analysis.common.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class BaseCheckMojo extends BaseMojo {

    @Parameter(property = "externalToolHome")
    protected String externalToolHome;

    @Parameter(property = "targetJDKHome")
    protected String targetJDKHome;

    /**
     * Specify a comma separated list of external tools in the form of maven coordinate. The specified external tools
     * shall be automatically downloaded with {@code mvn dependency:get -Dartifact=[tool coordinate]}, and copy to the
     * external tool working home specified by {@link BaseCheckMojo#externalToolHome}.
     * The external tool can be either jar or zip. Assume the external tool home is {@code EXT_HOME}. Specified external
     * tools is {@code -DexternalTools=org1:artifact1:version,org2:artifact2:veresion:zip:classifier}. The {@code artifact1} is
     * jar file, and will be copied to {@code EXT_HOME/org1-artifact1-version}. The {@code artifact2} is a zip file, and
     * will be unzipped to {@code EXT_HOME/org2-artifact2-version-zip-classifier}.
     * <p>
     * In summary, there are two constrains for this options:
     * <ol>
     *     <li>Each item should follow maven-dependency-plugin's idiom: {@code groupId:artifactId:version[:type[:classifier]]}</li>
     *     <li>{@link BaseCheckMojo#externalToolHome} must be set as well.</li>
     * </ol>
     */
    @Parameter
    protected List<String> externalTools;

    protected void prepareExternalTools() throws MojoFailureException, MojoExecutionException {
        String externalToolsProperty = System.getProperty("externalTools");
        if (externalToolsProperty != null && !externalToolsProperty.isEmpty()) {
            this.externalTools = Arrays.asList(externalToolsProperty.split(","));
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
                    //noinspection resource
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
    }

    protected File resolveOutputFile() throws MojoExecutionException {
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

    protected String[] buildArgs(File output, String format) {
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
        args.addAll(getTargets());
        return args.toArray(new String[0]);
    }

    protected abstract List<String> getTargets();

    private void param(List<String> args, String k, String v) {
        if (v != null && !"".equals(v)) {
            args.add(k);
            args.add(v);
        }
    }
}
