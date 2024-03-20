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
import org.openrewrite.xml.tree.Xml;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.emt4j.analysis.common.Constant.*;

public interface XmlTagMatcher {
    boolean matches(Xml.Tag tag);

    @AllArgsConstructor
    class GAXmlTagMatcher implements XmlTagMatcher {
        private String groupId;
        private String artifactId;

        @Override
        public boolean matches(Xml.Tag tag) {
            if (tag == null) {
                return false;
            }
            Optional<String> g = tag.getChildValue(POM_GROUP_ID);
            Optional<String> a = tag.getChildValue(POM_ARTIFACT_ID);
            if (!g.isPresent() || !a.isPresent()) {
                return false;
            }
            return groupId.equals(g.get()) && artifactId.equals(a.get());
        }
    }

    class GAGroupXmlTagMatcher implements XmlTagMatcher {
        private Set<String> gas;

        public GAGroupXmlTagMatcher(Collection<String> gatvs) {
            gas = gatvs.stream().map(gatv -> {
                int firstColonIndex = gatv.indexOf(":");
                int secondColonIndex = gatv.indexOf(":", firstColonIndex + 1);
                return gatv.substring(0, secondColonIndex);
            }).collect(Collectors.toSet());
        }

        @Override
        public boolean matches(Xml.Tag tag) {
            if (tag == null) {
                return false;
            }
            Optional<String> g = tag.getChildValue(POM_GROUP_ID);
            Optional<String> a = tag.getChildValue(POM_ARTIFACT_ID);
            if (!g.isPresent() || !a.isPresent()) {
                return false;
            }
            String key = g.get() + ":" + a.get();
            return gas.contains(key);
        }
    }
}
