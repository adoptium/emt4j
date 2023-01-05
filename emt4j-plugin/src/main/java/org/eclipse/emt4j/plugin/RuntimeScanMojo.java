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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A mojo for dynamically scanning the incompatible issues existing in the project.
 */
@Mojo(name = "runtime-scan", defaultPhase = LifecyclePhase.VERIFY)
public class RuntimeScanMojo extends StaticScanMojo {

    /**
     * true means combine static and runtime scan.
     */
    @Parameter(property = "withStaticScan", defaultValue = "true")
    private boolean withStaticScan;

    @Override
    List<String> getScanTargets() {
        List<String> targets = new ArrayList<>();
        if (withStaticScan) {
            targets.addAll(super.getScanTargets());
        }
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

    @Override
    boolean preScan() throws Exception {
        if (withStaticScan) {
            return super.preScan();
        }
        List<MavenProject> projects = session.getProjects();
        return project.equals(projects.get(projects.size() - 1));
    }
}
