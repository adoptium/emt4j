/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TestConf(mode = {TestConf.ModeEnum.AGENT, TestConf.ModeEnum.CLASS}, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class ArraysToListToArrayNoBugTest extends SITBaseCase {

    public void run() {
        URI[] uriArray = new URI[3];
        try {
            uriArray[0] = new URI("http://www.foo.com");
            uriArray[1] = new URI("http://www.bar1.com");
            uriArray[2] = new URI("http://www.bar2.com");
            List<URI> uriList = Arrays.asList(uriArray);
            Collections.shuffle(uriList);
            Object[] metastoreUris = uriList.toArray();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void verify(JsonReport jsonReport) {
        assertTrue(!jsonReport.getResultDetailList().stream().filter((d) -> d.getMainResultCode().equals("ARRAYS_AS_LIST_TO_ARRAY"))
                .findAny().isPresent(), "Expect not found ARRAYS_AS_LIST_TO_ARRAY,but actually found");
    }
}
