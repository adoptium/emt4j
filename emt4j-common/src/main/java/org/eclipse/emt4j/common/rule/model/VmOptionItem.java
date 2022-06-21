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

public class VmOptionItem {
    private String option;
    private Integer deprecatedVersion;
    private Integer obsoleteVersion;
    private Integer expiredVersion;
    private String suggestion;

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Integer getDeprecatedVersion() {
        return deprecatedVersion;
    }

    public void setDeprecatedVersion(Integer deprecatedVersion) {
        this.deprecatedVersion = deprecatedVersion;
    }

    public Integer getObsoleteVersion() {
        return obsoleteVersion;
    }

    public void setObsoleteVersion(Integer obsoleteVersion) {
        this.obsoleteVersion = obsoleteVersion;
    }

    public Integer getExpiredVersion() {
        return expiredVersion;
    }

    public void setExpiredVersion(Integer expiredVersion) {
        this.expiredVersion = expiredVersion;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}