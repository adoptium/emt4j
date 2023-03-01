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

package org.eclipse.emt4j.testjdk8;

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@TestConf(mode = {TestConf.ModeEnum.CLASS}, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class CLDRDateFormatGlobalNoBugTest extends SITBaseCase {
    private static final SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(1);
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-d H:mm:ss");

    public void run() {
        System.out.println(dateFormat.format(new Date()));
        System.out.println(simpleDateFormat.format(new Date()));
    }

    public void verify(JsonReport jsonReport) {
        assertTrue(!jsonReport.getResultDetailList().stream().filter((d) -> d.getMainResultCode().equals("CLDR_CLDR_DATE_FORMAT"))
                .findAny().isPresent(), "Expect not found CLDR_CLDR_DATE_FORMAT,but actually found");
    }
}
