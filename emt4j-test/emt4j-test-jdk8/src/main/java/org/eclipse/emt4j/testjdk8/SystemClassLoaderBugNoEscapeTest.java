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

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.common.MainResultDetail;
import org.eclipse.emt4j.common.SubResultDetail;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

import java.net.URLClassLoader;
import java.util.List;

@TestConf(mode = TestConf.ModeEnum.AGENT, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class SystemClassLoaderBugNoEscapeTest extends SITBaseCase {

    public void run() {
        URLClassLoader urlClassLoader = (URLClassLoader) SystemClassLoaderBugNoEscapeTest.class.getClassLoader();
        ClassLoader classLoader = SystemClassLoaderBugNoEscapeTest.class.getClassLoader();
        //do some trivial operation
        int count = 0;
        for (int i = 0; i < 10; i++) {
            URLClassLoader ucl = (URLClassLoader) classLoader;
            count += ucl.getURLs().length;
        }

        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            cl.getParent();
        } catch (NullPointerException npe) {
            count++;
        } catch (RuntimeException runtimeException) {
            count--;
        }
    }

    @Override
    public void verify(JsonReport jsonReport) {
        for (MainResultDetail mainResultDetail : jsonReport.getResultDetailList()) {
            if (mainResultDetail.getMainResultCode().equals("SYSTEM_CLASSLOADER_TO_URLCLASSLOADER")) {
                List<SubResultDetail> subResultDetailList = jsonReport.getResultDetailList().get(0).getSubResultDetailList();
                assertTrue(subResultDetailList != null);
                assertTrue(subResultDetailList.size() == 1);
                return;
            }
        }
        assertTrue(false, "Should found SYSTEM_CLASSLOADER_TO_URLCLASSLOADER,but actually not found!");
    }
}
