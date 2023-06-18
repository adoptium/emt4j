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
package org.eclipse.emt4j.analysis.report.external.impl;

import org.eclipse.emt4j.analysis.common.model.ExternalToolParam;
import org.eclipse.emt4j.analysis.common.util.JdkUtil;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * jdeps can analyse the JDK internal api usage.
 */
public class JdepTool extends CodeSourceAsCheckTargetTool {
    private static final Pattern INTERNAL_API = Pattern.compile("\\s+(.*)\\s+->\\s+(.*\\s+JDK internal API.*)$");

    @Override
    protected List<BodyRecord> parseOutput(File jarOrClass, String result) throws MalformedURLException {
        String[] lines = result.split("\n");
        if (lines.length == 0) {
            return Collections.emptyList();
        }

        List<BodyRecord> records = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            Matcher m = INTERNAL_API.matcher(lines[i]);
            if (m.matches()) {
                records.add(createRecord(jarOrClass, m.group(1), m.group(2)));
            }
        }
        return records;
    }

    @Override
    protected String[] getCommand(String toolPath, String jarOrClass) {
        return new String[]{toolPath, "-q", "--jdk-internals", jarOrClass};
    }

    @Override
    protected String getToolPath(ExternalToolParam etp) {
        return JdkUtil.getJdkToolPath(etp.getTargetJdkHome(), "jdeps");
    }

    private BodyRecord createRecord(File jarOrClass, String problemClass, String suggestion) throws MalformedURLException {
        BodyRecord br = new BodyRecord();
        br.setFeature(Feature.DEFAULT);
        br.setDependency(new Dependency(jarOrClass.toURI().toURL(), new DependTarget.Class(problemClass, DependType.CLASS), null, jarOrClass.getAbsolutePath()));

        ReportCheckResult checkResult = new ReportCheckResult(false);
        checkResult.setResultCode("JDK_INTERNAL");
        Map<String, Object> context = new HashMap<>();
        context.put("suggestion", suggestion);
        checkResult.setContext(context);

        br.setCheckResult(checkResult);
        return br;
    }

    @Override
    public String name() {
        return "jdeps";
    }
}
