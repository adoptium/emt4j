/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.testjdk11;

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.common.MainResultDetail;
import org.eclipse.emt4j.common.SubResultDetail;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

import java.util.List;

@TestConf(mode = TestConf.ModeEnum.AGENT, from = TestConf.RELEASE.JDK11, to = TestConf.RELEASE.JDK17, option = "-XX:+UseConcMarkSweepGC")
public class JavaOptionTest extends SITBaseCase {

    public void run() {
        //we only check java option,so no need do anything
    }

    @Override
    public void verify(JsonReport jsonReport) {
        List<MainResultDetail> list = filter(jsonReport, "VM_OPTION");
        assertNotEmpty(list, "Should find VM_OPTION error,but actually not found!");
        boolean checkSuggestionOK = false;
        found:
        for (MainResultDetail mrd : list) {
            for (SubResultDetail srd : mrd.getSubResultDetailList()) {
                for (String solution : srd.getHowToFix()) {
                    if (solution.indexOf("-XX:+UseConcMarkSweepGC") != -1) {
                        checkSuggestionOK = true;
                        break found;
                    }
                }
            }
        }
        assertTrue(checkSuggestionOK, "Solution should contain -XX:+UseConcMarkSweepGC,but actually not found!");
    }
}
