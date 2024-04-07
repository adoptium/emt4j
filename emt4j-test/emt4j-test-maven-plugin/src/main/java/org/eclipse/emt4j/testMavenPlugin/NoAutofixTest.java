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

@TestConf(mode = {ModeEnum.MAVEN_PLUGIN}, option = "process-test-classes org.eclipse.emt4j:emt4j-maven-plugin:${version}:process -Dpriority=p1 -Dautofix=false -DfixedReportFileName=fixedReport.html -DunfixedReportFileName=unfixedReport.html -DautofixFile=fixed.patch -DoutputFile=report.html")
public class NoAutofixTest extends BaseMavenPluginSITCase {

    @Override
    protected String getTestProject() {
        return "fullApplication";
    }

    @Override
    protected void verify() {
        asserFileNotExist("fixed.patch");
        asserFileNotExist("fixedReport.html");
        asserFileNotExist("unfixedReport.html");
        asserFileExist("report.html");
    }
}
