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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.emt4j.analysis.AnalysisMain;

import java.util.Arrays;
import java.util.List;

@Mojo(name = "direct-check", requiresProject = false)
public class DirectCheckMojo extends BaseCheckMojo {

    /**
     * Specify the targets separated by ',' to check
     */
    @Parameter(property = "targets", required = true)
    private String targets;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        prepareExternalTools();

        try {
            AnalysisMain.main(buildArgs(resolveOutputFile(), outputFormat));
        } catch (Throwable t) {
            throw new MojoExecutionException("Failed to execute direct-check", t);
        }
    }

    @Override
    protected List<String> getTargets() {
        return Arrays.asList(targets.split(","));
    }
}
