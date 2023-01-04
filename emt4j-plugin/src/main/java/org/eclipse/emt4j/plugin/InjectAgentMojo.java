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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "inject-agent", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.TEST)
public class InjectAgentMojo extends BaseMojo {

    @Parameter(defaultValue = "${project.build.directory}/emt4j.dat", required = true, readonly = true)
    private File dat;

    /**
     * Map of plugin artifacts.
     */
    @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
    Map<String, Artifact> pluginArtifactMap;

    @Override
    protected void doExecute() {
        String key = "argLine";
        Properties properties = project.getProperties();
        StringBuilder fv = new StringBuilder();
        String original = properties.getProperty(key);
        if (original != null) {
            fv.append(original);
        }
        fv.append(" ").append(buildAgentOption());
        project.getProperties().setProperty("argLine", fv.toString());
    }

    private String agentPath() {
        return pluginArtifactMap.get("org.eclipse.emt4j:emt4j-agent-jdk" + fromVersion).getFile().getAbsolutePath();
    }

    private String buildAgentOption() {
        StringBuilder opt = new StringBuilder();
        opt.append("-javaagent:")
           .append(agentPath())
           .append('=').append("to=").append(toVersion)
           .append(',').append("file=").append(dat.getAbsolutePath());
        if (priority != null && !priority.isEmpty()) {
            opt.append(',').append("priority=").append(priority);
        }
        return opt.toString();
    }
}
