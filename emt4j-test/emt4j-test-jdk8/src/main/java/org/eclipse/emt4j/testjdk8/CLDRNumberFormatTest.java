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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

@TestConf(mode = {TestConf.ModeEnum.CLASS}, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class CLDRNumberFormatTest extends SITBaseCase {

    public void run() {
        BigDecimal value = new BigDecimal("1.99");
        Locale locale = new Locale("zh_CN");
        String[] str = new String[]{"CNY", "EUR", "USD"};

        for (String currencyCode : str) {
            NumberFormat format = NumberFormat.getCurrencyInstance(locale);
            Currency currency = Currency.getInstance(currencyCode);
            format.setCurrency(currency);
            String result = format.format(value);
            System.out.println("Result: " + result);
        }
    }

    public void verify(JsonReport jsonReport) {
        assertTrue(jsonReport.getResultDetailList().stream().filter((d) -> d.getMainResultCode().equals("CLDR_CLDR_NUMBER_FORMAT"))
                .findAny().isPresent(), "Expect found CLDR_CLDR_NUMBER_FORMAT,but actually not found");
    }

}
