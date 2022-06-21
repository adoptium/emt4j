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

import java.util.List;

public class ConfRules {
    private List<ConfRuleItem> ruleItems;
    private String ruleDataPathPrefix;
    private String feature;
    private int fromVersion;
    private int toVersion;

    public List<ConfRuleItem> getRuleItems() {
        return ruleItems;
    }

    public void setRuleItems(List<ConfRuleItem> ruleItems) {
        this.ruleItems = ruleItems;
    }

    public String getRuleDataPathPrefix() {
        return ruleDataPathPrefix;
    }

    public void setRuleDataPathPrefix(String ruleDataPathPrefix) {
        this.ruleDataPathPrefix = ruleDataPathPrefix;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public int getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(int fromVersion) {
        this.fromVersion = fromVersion;
    }

    public int getToVersion() {
        return toVersion;
    }

    public void setToVersion(int toVersion) {
        this.toVersion = toVersion;
    }
}
