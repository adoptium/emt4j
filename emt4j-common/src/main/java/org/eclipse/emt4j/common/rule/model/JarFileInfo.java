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
package org.eclipse.emt4j.common.rule.model;

import java.util.Arrays;

public class JarFileInfo {
    private String version;

    private String[] orderedArtifactFragments;

    private String[] artifactFragments;

    private String jarFileName;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String[] getOrderedArtifactFragments() {
        return orderedArtifactFragments;
    }

    public void setOrderedArtifactFragments(String[] orderedArtifactFragments) {
        this.orderedArtifactFragments = orderedArtifactFragments;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public String[] getArtifactFragments() {
        return artifactFragments;
    }

    public void setArtifactFragments(String[] artifactFragments) {
        this.artifactFragments = artifactFragments;
    }

    @Override
    public String toString() {
        return "JarFileInfo{" +
                "version='" + version + '\'' +
                ", orderedArtifactFragments=" + Arrays.toString(orderedArtifactFragments) +
                ", artifactFragments=" + Arrays.toString(artifactFragments) +
                ", jarFileName='" + jarFileName + '\'' +
                '}';
    }
}
