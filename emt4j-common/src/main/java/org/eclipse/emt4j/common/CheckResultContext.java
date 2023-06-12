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

import org.eclipse.emt4j.common.rule.model.ReportCheckResult;

public class CheckResultContext {

    private final Feature feature;

    private ReportCheckResult reportCheckResult;

    private Dependency dependency;

    public ReportCheckResult getReportCheckResult() {
        return reportCheckResult;
    }

    public void setReportCheckResult(ReportCheckResult reportCheckResult) {
        this.reportCheckResult = reportCheckResult;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public Feature getFeature() {
        return feature;
    }

    public CheckResultContext(Feature feature, ReportCheckResult reportCheckResult, Dependency dependency) {
        this.feature = feature;
        this.reportCheckResult = reportCheckResult;
        this.dependency = dependency;
    }
}
