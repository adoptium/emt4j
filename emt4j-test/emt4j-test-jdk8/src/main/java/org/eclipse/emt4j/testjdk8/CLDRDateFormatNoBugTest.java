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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@TestConf(mode = {TestConf.ModeEnum.CLASS}, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class CLDRDateFormatNoBugTest extends SITBaseCase {
    public void run() {
        SimpleDateFormat USformat = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US);
        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy h:mm:ss a");

        SimpleDateFormat USformat1 = new SimpleDateFormat("yyyy-M-d H:mm:ss", Locale.US);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-M-d H:mm:ss");
        SimpleDateFormat format2 = (SimpleDateFormat) DateFormat.getDateInstance(1);
        System.out.println(USformat.format(new Date()));
        System.out.println(format.format(new Date()));
        System.out.println(USformat1.format(new Date()));
        System.out.println(format1.format(new Date()));
        try {
            System.out.println(format2.parse(format2.format(new Date())));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void verify(JsonReport jsonReport) {
        assertTrue(!jsonReport.getResultDetailList().stream().filter((d) -> d.getMainResultCode().equals("CLDR_CLDR_DATE_FORMAT"))
                .findAny().isPresent(), "Expect not found CLDR_CLDR_DATE_FORMAT,but actually found");
    }
}

