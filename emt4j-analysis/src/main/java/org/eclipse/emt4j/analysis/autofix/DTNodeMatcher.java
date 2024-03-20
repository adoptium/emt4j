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

import static org.eclipse.emt4j.analysis.common.Constant.ANY;

public interface DTNodeMatcher {
    public boolean matches(DTNode node);

    public static class GATVSNodeMatcher implements DTNodeMatcher{
        private String groupId;
        private String artifactId;
        private String type;
        private String version;
        private String scope;

        public GATVSNodeMatcher(String groupId, String artifactId) {
            this(groupId, artifactId, ANY, ANY, ANY);
        }

        public GATVSNodeMatcher(String groupId, String artifactId, String type, String version, String scope) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.type = type;
            this.version = version;
            this.scope = scope;
        }

        @Override
        public boolean matches(DTNode node) {
            return match(groupId, node.getGroupId()) && match(artifactId, node.getArtifactId()) &&
            match(type, node.getType()) && match(version, node.getVersion()) &&
            match(scope, node.getScope()) ;
        }

        private boolean match(String target, String current) {
            if (target == null || ANY.equals(target)) {
                return true;
            }
            return target.equals(current);
        }
    }

}
