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

/**
 * Feature tries to describe different aspects of JDK incompatible problems.
 * Now only support a default, it means generic.
 * Other possible features like architecture-specific, like AArch64.
 * Each feature contains a collection of resources named with the feature name located at the root of resources of the emt4j-common module.
 * The layout is like this:
 * <pre>
 * ${feature name}
 *  |-- i18n
 *      |-- common.properties
 *      |-- other.properties
 * |-- rule
 *      |-8to11
 *          |-- rule.xml
 *      |-11to17
 *          |-- rule.xml
 * </pre>
 */
public enum Feature {
    DEFAULT("default");
    private String id;

    Feature(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static Feature getFeatureByCommandLineText(String commandLineTxt) {
        for (Feature featureEnum : Feature.values()) {
            if (featureEnum.getId().equals(commandLineTxt)) {
                return featureEnum;
            }
        }
        return null;
    }

    public String getRuleBasePath(int fromVersion, int toVersion) {
        switch (this) {
            case DEFAULT:
                return "/default/rule/" + fromVersion + "to" + toVersion;
        }
        throw new RuntimeException("should not reach here");
    }
}
