/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.testMavenPlugin;

import org.eclipse.emt4j.test.common.TestConf;

import static org.eclipse.emt4j.test.common.TestConf.ModeEnum;

@TestConf(mode = {ModeEnum.MAVEN_PLUGIN}, option = "process-test-classes org.eclipse.emt4j:emt4j-maven-plugin:${version}:check -Dpriority=p1 -Dautofix=true -DfixedReportFileName=fixedReport.html -DunfixedReportFileName=unfixedReport.html -DautofixFile=fixed.patch -DoutputFile=report.html-Dcheck=false")
public class AutofixButNoCheckTest extends BaseMavenPluginSITCase {

    @Override
    protected String getTestProject() {
        return "fullApplication";
    }

    @Override
    protected void verify() {
        asserFileExist("fixed.patch");
        asserFileExist("fixedReport.html");
        asserFileExist("unfixedReport.html");
        asserFileNotExist("report.html");

        checkFixedReport();
        checkFixedPatch();
    }

    private void checkFixedReport() {
        String fixedReport = getFileContent("fixedReport.html");

        assertTrue(fixedReport.contains("maven-compiler-plugin"));
        assertTrue(fixedReport.contains("org.projectlombok"));
        assertTrue(fixedReport.contains("maven-surefire-plugin"));
        assertTrue(fixedReport.contains("lombok.experimental"));
    }

    private void checkFixedPatch() {
        String patch = getFileContent("fixed.patch");

        assertTrue(patch.contains("lombok.version"));
        assertTrue(patch.contains("maven-compiler-plugin"));
        assertTrue(!patch.contains("pinyin4j"));
        assertTrue(patch.contains("Base64"));

        assertApplyFixedPatchSucceed("fixed.patch");
    }
}
