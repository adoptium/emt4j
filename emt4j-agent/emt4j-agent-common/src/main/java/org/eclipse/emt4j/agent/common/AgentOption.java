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

import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;

import java.util.Locale;

/**
 * option when running java with the current agent
 */
public class AgentOption {

    /**
     * where to write the output file
     */
    private String outputFile;

    /**
     * From which version
     */
    private int fromVersion;

    /**
     * To which version
     */
    private int toVersion;

    /**
     * Locale when writing the result to file
     */
    private Locale locale = Locale.ENGLISH;

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
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

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void check() {
        if (ConfRuleFacade.findWays(fromVersion, toVersion) == null) {
            throw new JdkMigrationException("Not support from " + fromVersion + " to " + toVersion);
        }
    }
}
