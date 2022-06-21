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
package org.eclipse.emt4j.common.fileformat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VariableHeader implements Serializable {
    private int fromVersion;
    private int toVersion;
    private String vmOption;
    private Date date;
    private List<String> features = new ArrayList<>();

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

    public String getVmOption() {
        return vmOption;
    }

    public void setVmOption(String vmOption) {
        this.vmOption = vmOption;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    @Override
    public String toString() {
        return "VariableHeader{" +
                "fromVersion=" + fromVersion +
                ", toVersion=" + toVersion +
                ", vmOption='" + vmOption + '\'' +
                ", date=" + date +
                ", features=" + features +
                '}';
    }
}
