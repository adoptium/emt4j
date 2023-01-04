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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.common.JdkMigrationException;

abstract class BaseMojo extends AbstractMojo  {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession session;

    /**
     * Define the source JDK version, support: 8, 11
     */
    @Parameter(property = "fromVersion", defaultValue = "8")
    protected int fromVersion;

    /**
     * Define the target JDK version, support 11,17
     */
    @Parameter(property = "toVersion", defaultValue = "11")
    protected int toVersion;

    @Parameter(property = "priority")
    protected String priority;

    @Parameter(property = "verbose", defaultValue = "false")
    protected boolean verbose;

    @Parameter(property = "outputFormat", defaultValue = "html")
    protected String outputFormat;

    @Parameter(property = "outputFile", defaultValue = "report.html")
    protected String outputFile;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (fromVersion >= toVersion) {
            throw new JdkMigrationException("fromVersion should less than toVersion");
        }
        doExecute();
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;
}
