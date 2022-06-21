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
package org.eclipse.emt4j.agent.common;

/**
 * JDK Version dependent configs.
 * NOTE: Not play any non-JDK instance in this class, or else you will get classloader volatile exception
 */
public class JdkDependConfig {

    /**
     * Class names for rule implementation
     */
    private String[] ruleClasses;

    /**
     * Caller Provider class name
     */
    private String callerProviderClassName;

    private int fromVersion;

    /**
     * Agent lib path
     */
    private String agentLibPath;

    public JdkDependConfig(String[] ruleClasses, String callerProviderClassName, int fromVersion, String agentLibPath) {
        this.ruleClasses = ruleClasses;
        this.callerProviderClassName = callerProviderClassName;
        this.fromVersion = fromVersion;
        this.agentLibPath = agentLibPath;
    }

    public String[] getRuleClasses() {
        return ruleClasses;
    }

    public void setRuleClasses(String[] ruleClasses) {
        this.ruleClasses = ruleClasses;
    }

    public String getCallerProviderClassName() {
        return callerProviderClassName;
    }

    public void setCallerProviderClassName(String callerProviderClassName) {
        this.callerProviderClassName = callerProviderClassName;
    }

    public int getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(int fromVersion) {
        this.fromVersion = fromVersion;
    }

    public String getAgentLibPath() {
        return agentLibPath;
    }

    public void setAgentLibPath(String agentLibPath) {
        this.agentLibPath = agentLibPath;
    }
}
