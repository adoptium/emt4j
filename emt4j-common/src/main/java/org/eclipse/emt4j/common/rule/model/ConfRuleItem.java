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

import java.util.ArrayList;
import java.util.List;

public class ConfRuleItem {
    private String desc;
    private String type;
    private String resultCode;
    private String subResultCode;
    private List<String> supportModes = new ArrayList<>();
    private List<String[]> userDefineAttrs;
    private String priority;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getSubResultCode() {
        return subResultCode;
    }

    public void setSubResultCode(String subResultCode) {
        this.subResultCode = subResultCode;
    }

    public List<String> getSupportModes() {
        return supportModes;
    }

    public void setSupportModes(List<String> supportModes) {
        this.supportModes = supportModes;
    }

    public List<String[]> getUserDefineAttrs() {
        return userDefineAttrs;
    }

    public void setUserDefineAttrs(List<String[]> userDefineAttrs) {
        this.userDefineAttrs = userDefineAttrs;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
