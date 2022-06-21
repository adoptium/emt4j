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
package org.eclipse.emt4j.common;

public class CheckConfig {

    private boolean verbose = false;

    private int toVersion = 11;

    private int fromVersion = 8;

    private Feature checkMode = Feature.DEFAULT;

    public int getToVersion() {
        return toVersion;
    }

    public void setToVersion(int toVersion) {
        this.toVersion = toVersion;
    }

    public int getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(int fromVersion) {
        this.fromVersion = fromVersion;
    }

    public Feature getCheckMode() {
        return checkMode;
    }

    public void setCheckMode(Feature checkMode) {
        this.checkMode = checkMode;
    }

    public void copyFrom(CheckConfig from) {
        this.verbose = from.verbose;
        this.fromVersion = from.fromVersion;
        this.toVersion = from.toVersion;
        this.checkMode = from.checkMode;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
