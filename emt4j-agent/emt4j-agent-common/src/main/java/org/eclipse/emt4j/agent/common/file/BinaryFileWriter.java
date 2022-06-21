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
package org.eclipse.emt4j.agent.common.file;

import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.fileformat.FixedHeader;
import org.eclipse.emt4j.common.fileformat.VariableHeader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

public class BinaryFileWriter implements CheckResultFileWriter {
    private final File output;
    private final int fromVersion;
    private final int toVersion;
    private ObjectOutputStream oos;
    private List<String> features;

    public BinaryFileWriter(File output, int fromVersion, int toVersion, List<String> features) {
        this.output = output;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.features = features;
    }

    @Override
    public void begin() throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(getFixHeader());
        oos.writeObject(getVariableHeader());
        oos.flush();
    }

    @Override
    public void write(Dependency dependency, ReportCheckResult checkResult, ExecutableRule rule) throws IOException {
        BodyRecord br = new BodyRecord();
        br.setCheckResult(checkResult);
        br.setDependency(dependency);
        if (rule != null) {
            br.setFeature(rule.getConfRules().getFeature());
        }
        oos.writeObject(br);
        oos.flush();
    }

    private VariableHeader getVariableHeader() {
        VariableHeader vh = new VariableHeader();
        vh.setFromVersion(fromVersion);
        vh.setToVersion(toVersion);
        vh.setDate(new Date());
        vh.setVmOption(getVmOption());
        vh.setFeatures(features);
        return vh;
    }

    private String getVmOption() {
        return String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    private FixedHeader getFixHeader() {
        FixedHeader fh = new FixedHeader();
        fh.setMagic(FixedHeader.MAGIC);
        fh.setVersion(FixedHeader.VERSION);
        return fh;
    }

    @Override
    public void close() throws IOException {
        oos.close();
    }
}
