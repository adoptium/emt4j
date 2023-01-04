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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.AnalysisMain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "check-with-agent", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class CheckWithAgentMojo extends BaseCheckMojo {
    @Override
    protected List<String> getTargets() {
        List<MavenProject> projects = session.getProjects();
        List<String> targets = new ArrayList<>();
        for (MavenProject project : projects) {
            File file = new File(project.getBuild().getDirectory(), "emt4j.dat");
            if (file.exists() && !file.isDirectory()) {
                System.out.println(file.getAbsolutePath());
                targets.add(file.getAbsolutePath());
            }
        }
        return targets;
    }

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        List<MavenProject> projects = session.getProjects();
        if (!project.equals(projects.get(projects.size() - 1))) {
            System.out.println("Skip");
            return;
        }

        prepareExternalTools();

        try {
            AnalysisMain.main(buildArgs(resolveOutputFile(), outputFormat));
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
