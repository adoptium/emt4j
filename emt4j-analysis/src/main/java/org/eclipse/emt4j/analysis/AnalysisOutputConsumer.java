/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis;

import org.eclipse.emt4j.common.CheckConfig;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.SourceInformation;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;

import java.io.IOException;
import java.util.List;

/**
 * Define how to consume the output of analysis
 */
public interface AnalysisOutputConsumer {

    /**
     * called when start to analysis.
     */
    void onBegin(CheckConfig checkConfig, List<Feature> featureList) throws IOException;

    /**
     * called when a new incompatible problem found
     */
    void onNewRecord(Dependency dependency, ReportCheckResult checkResult, ExecutableRule rule) throws IOException;


    default void onNewRecord(Dependency dependency, ReportCheckResult checkResult, ExecutableRule rule, SourceInformation sourceInformation) throws IOException {
        dependency.setSourceInformation(sourceInformation);
        onNewRecord(dependency, checkResult, rule);
    }
}
