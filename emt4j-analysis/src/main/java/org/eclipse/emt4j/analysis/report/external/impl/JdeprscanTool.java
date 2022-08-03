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
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emt4j.common.util.ClassURL;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * jdeprscan can analyse the deprecated API usage in classes.
 */
public class JdeprscanTool extends CodeSourceAsCheckTargetTool {

    private static final String CLASS_FLAG = "class ";
    private static final String CLASS_METHOD_SEPARATOR = "::";

    @Override
    protected List<BodyRecord> parseOutput(File jarOrClass, String result) throws MalformedURLException {
        if (StringUtils.isEmpty(result)) {
            return Collections.emptyList();
        }
        String[] lines = result.split("\n");
        List<BodyRecord> list = new ArrayList<>();
        for (String line : lines) {
            //class org/springframework/scripting/config/ScriptBeanDefinitionParser uses deprecated method java/lang/Long::<init>(Ljava/lang/String;)V
            if (line.startsWith(CLASS_FLAG)) {
                int classEnd = line.indexOf(' ', CLASS_FLAG.length());
                if (classEnd != -1) {
                    list.add(newRecord(jarOrClass.toURI().toURL(), line.substring(CLASS_FLAG.length(), classEnd), line.substring(classEnd + 1)));
                }
            }
        }
        return list;
    }

    /**
     * the format of deprecatedMethod like this:
     * overrides deprecated method java/lang/Object::finalize()V
     */
    private BodyRecord newRecord(URL jarOrClass, String className, String jdeprOutput) {
        int j = jdeprOutput.indexOf(CLASS_METHOD_SEPARATOR);
        BodyRecord br = new BodyRecord();
        br.setFeature(Feature.DEFAULT.getId());
        if (j != -1) {
            int i = jdeprOutput.lastIndexOf(' ', j);
            int k = jdeprOutput.indexOf('(', j + CLASS_METHOD_SEPARATOR.length());
            if (i != -1 && k != -1) {
                br.setDependency(new Dependency(ClassURL.create(jarOrClass.getFile(), className, null),
                        new DependTarget.Method(jdeprOutput.substring(i, j), jdeprOutput.substring(j + CLASS_METHOD_SEPARATOR.length(), k), DependType.METHOD), null, jarOrClass.getFile()));
            }
        } else {
            br.setDependency(new Dependency(ClassURL.create(jarOrClass.getFile(), className, null),
                    new DependTarget.Method(null, jdeprOutput, DependType.METHOD), null, jarOrClass.getFile()));
        }

        ReportCheckResult checkResult = new ReportCheckResult(false);
        checkResult.setResultCode("DEPRECATED_API");
        br.setCheckResult(checkResult);
        return br;
    }

    @Override
    protected String[] getCommand(String toolPath, String jarOrClass) {
        return new String[]{toolPath, jarOrClass};
    }

    @Override
    protected String getToolPath(ExternalToolParam etp) {
        return JdkUtil.getJdkToolPath(etp.getTargetJdkHome(), "jdeprscan");
    }

    @Override
    public String name() {
        return "jdeprscan";
    }
}
