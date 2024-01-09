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
package org.eclipse.emt4j.testjdk17;

import org.eclipse.emt4j.common.IssueContext;
import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.common.MainResultDetail;
import org.eclipse.emt4j.common.SubResultDetail;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

@TestConf(mode = TestConf.ModeEnum.CLASS, from = TestConf.RELEASE.JDK17, to = TestConf.RELEASE.JDK21)
public class DeprecatedAPITest extends SITBaseCase {

    public void run() {
        Thread thread = new Thread();
        thread.stop();
    }

    @Override
    public void verify(JsonReport jsonReport) {
        List<MainResultDetail> list = filter(jsonReport, "DEPRECATED_API");
        assertNotEmpty(list, "Not found list with DEPRECATED_API");
        boolean found = false;
        found:
        for (MainResultDetail mrd : list) {
            for (SubResultDetail srd : mrd.getSubResultDetailList()) {
                for (IssueContext ic : srd.getIssueContextList()) {
                    for (String contextItem : ic.getContextDesc()) {
                        if (contextItem.contains("stop")) {
                            found = true;
                            break found;
                        }
                    }
                }
            }
        }
        assertTrue(found, "Not find deprecated stop in context");
    }
}
