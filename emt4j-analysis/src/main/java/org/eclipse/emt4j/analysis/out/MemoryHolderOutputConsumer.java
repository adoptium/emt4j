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
package org.eclipse.emt4j.analysis.out;

import org.eclipse.emt4j.analysis.AnalysisOutputConsumer;
import org.eclipse.emt4j.analysis.common.ReportInputProvider;
import org.eclipse.emt4j.common.CheckConfig;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.fileformat.VariableHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryHolderOutputConsumer implements AnalysisOutputConsumer {
    private final CheckConfig checkConfig = new CheckConfig();
    private List<BodyRecord> recordList = new ArrayList<>();
    private final List<String> features = new ArrayList<>();

    @Override
    public void onBegin(CheckConfig paramCheckConfig, List<Feature> featureList) throws IOException {
        this.checkConfig.copyFrom(paramCheckConfig);
        features.addAll(featureList.stream().map((f) -> f.getId()).collect(Collectors.toList()));
    }

    @Override
    public void onNewRecord(Dependency dependency, ReportCheckResult checkResult, ExecutableRule rule) throws IOException {
        BodyRecord br = new BodyRecord();
        br.setCheckResult(checkResult);
        br.setDependency(dependency);
        if (rule != null) {
            br.setFeature(rule.getConfRules().getFeature());
        }
        recordList.add(br);
    }

    public ReportInputProvider getInputProvider() {
        return new ReportInputProvider() {
            @Override
            public List<BodyRecord> getRecords() throws IOException, ClassNotFoundException {
                return recordList;
            }

            @Override
            public VariableHeader getHeader() {
                VariableHeader vh = new VariableHeader();
                vh.setFromVersion(checkConfig.getFromVersion());
                vh.setToVersion(checkConfig.getToVersion());
                vh.setFeatures(features);
                return vh;
            }
        };
    }
}
