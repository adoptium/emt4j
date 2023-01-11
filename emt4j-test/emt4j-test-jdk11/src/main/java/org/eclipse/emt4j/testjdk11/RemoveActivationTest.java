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
package org.eclipse.emt4j.testjdk11;

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

@TestConf(mode = {TestConf.ModeEnum.AGENT, TestConf.ModeEnum.CLASS}, from = TestConf.RELEASE.JDK11, to = TestConf.RELEASE.JDK17)
public class RemoveActivationTest extends SITBaseCase {
    public void run() {
        java.util.Properties props = new java.util.Properties();
        java.rmi.activation.ActivationGroupDesc.CommandEnvironment ace = null;
        new java.rmi.activation.ActivationGroupDesc(props, ace);
    }

    @Override
    public void verify(JsonReport jsonReport) {
        assertTrue(matchAny(jsonReport, "REMOVE_ACTIVATION"));
    }
}
