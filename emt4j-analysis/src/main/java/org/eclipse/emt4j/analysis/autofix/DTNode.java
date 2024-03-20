/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis.autofix;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTNode {
    private DTNode parent;
    private List<DTNode> children = new ArrayList<>();

    private String groupId;
    private String artifactId;
    private String type;
    private String version;
    private String scope;

    public void addChild(DTNode child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return getGroupId() + ":" + getArtifactId() + ":" + getType()+ ":" + getVersion() + ":" + getScope();
    }

    public String toGATV() {
        return getGroupId() + ":" + getArtifactId() + ":" + getType()+ ":" + getVersion();
    }
}
