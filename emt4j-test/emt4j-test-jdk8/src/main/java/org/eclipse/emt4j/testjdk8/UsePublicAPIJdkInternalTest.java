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
package org.eclipse.emt4j.testjdk8;

import org.eclipse.emt4j.common.IssueContext;
import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.common.MainResultDetail;
import org.eclipse.emt4j.common.SubResultDetail;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@TestConf(mode = TestConf.ModeEnum.AGENT, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class UsePublicAPIJdkInternalTest extends SITBaseCase {

    public void run() {
        doGetWithSerializeFunction("http://www.taobao.com");
    }

    @Override
    public void verify(JsonReport jsonReport) {
        //the result include some other class that not our test object
        //so need filter them.
        List<MainResultDetail> list = jsonReport.getResultDetailList().stream().filter((p) -> p.getMainResultCode().equals("JDK_INTERNAL")).collect(Collectors.toList());
        boolean found = false;
        found:
        for (MainResultDetail mrd : list) {
            for (SubResultDetail srd : mrd.getSubResultDetailList()) {
                for (IssueContext issue : srd.getIssueContextList()) {
                    for (String contextDesc : issue.getContextDesc()) {
                        if (contextDesc.indexOf(UsePublicAPIJdkInternalTest.class.getName()) != -1) {
                            found = true;
                            break found;
                        }
                    }
                }
            }
        }

        assertFalse(found, "Expect no JDK_INTERNAL,but actually found");
    }

    private void doGetWithSerializeFunction(String url) {
        InputStreamReader isr = null;
        InputStreamReader esr = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int statusCode = conn.getResponseCode();
            isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        } catch (Throwable ex) {
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ex) {
                }
            }
            if (esr != null) {
                try {
                    esr.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
