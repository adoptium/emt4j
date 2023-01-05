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

/**
 * Base mojo of EMT4J
 */
abstract class BaseMojo extends AbstractMojo {

    /**
     * The current session
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /**
     * The current project
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Indicate the source Java version. 8 and 11 are supported.
     */
    @Parameter(property = "fromVersion", defaultValue = "8")
    protected int fromVersion;

    /**
     * Indicate the target Java version. 11 and 17 are supported.
     */
    @Parameter(property = "toVersion", defaultValue = "11")
    protected int toVersion;

    /**
     * Indicate the priority. p1, p2, p3 and p4 are supported.
     */
    @Parameter(property = "priority")
    protected String priority;

    /**
     * Show more detail messages if <code>true</code>.
     */
    @Parameter(property = "verbose", defaultValue = "false")
    protected boolean verbose;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (fromVersion >= toVersion) {
            throw new JdkMigrationException("fromVersion should less than toVersion");
        }
        doExecute();
    }

    abstract void doExecute() throws MojoExecutionException, MojoFailureException;
}
