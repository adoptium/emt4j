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
import org.eclipse.emt4j.common.CheckConfig;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.fileformat.FixedHeader;
import org.eclipse.emt4j.common.fileformat.VariableHeader;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BinaryFileOutputConsumer implements AnalysisOutputConsumer {
    private ObjectOutputStream out;

    public BinaryFileOutputConsumer(ObjectOutputStream out) {
        this.out = out;
    }

    @Override
    public void onBegin(CheckConfig checkConfig, List<Feature> featureList) throws IOException {
        out.writeObject(getFixHeader());
        out.writeObject(getVariableHeader(checkConfig, featureList));
        out.flush();
    }

    private FixedHeader getFixHeader() {
        FixedHeader fh = new FixedHeader();
        fh.setMagic(FixedHeader.MAGIC);
        fh.setVersion(FixedHeader.VERSION);
        return fh;
    }

    private VariableHeader getVariableHeader(CheckConfig checkConfig, List<Feature> featureList) {
        VariableHeader vh = new VariableHeader();
        vh.setFromVersion(checkConfig.getFromVersion());
        vh.setToVersion(checkConfig.getToVersion());
        vh.setDate(new Date());
        vh.setFeatures(featureList.stream().map((f) -> f.getId()).collect(Collectors.toList()));
        return vh;
    }

    @Override
    public synchronized void onNewRecord(Dependency dependency, ReportCheckResult checkResult, ExecutableRule rule) throws IOException {
        BodyRecord br = new BodyRecord();
        br.setCheckResult(checkResult);
        br.setDependency(dependency);
        if (rule != null) {
            br.setFeature(rule.getConfRules().getFeature());
        }
        out.writeObject(br);
        out.flush();
    }
}
