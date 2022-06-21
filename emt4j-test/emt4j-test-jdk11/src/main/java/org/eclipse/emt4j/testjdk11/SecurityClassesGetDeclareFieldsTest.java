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

import java.lang.reflect.*;


import static org.eclipse.emt4j.test.common.TestConf.ModeEnum;
import static org.eclipse.emt4j.test.common.TestConf.RELEASE;

@TestConf(mode = ModeEnum.AGENT, from = RELEASE.JDK11, to = RELEASE.JDK17)
public class SecurityClassesGetDeclareFieldsTest extends SITBaseCase {
    public void run() {
        Class[] securityClasses = new Class[]{
                AccessibleObject.class,
                Class.class,
                ClassLoader.class,
                Constructor.class,
                Field.class,
                Method.class,
                Module.class,
                System.class
        };

        for (Class c : securityClasses) {
            Field[] fields = c.getDeclaredFields();
        }
    }

    public void verify(JsonReport jsonReport) {
        assertTrue(matchAny(jsonReport, "SECURITY_CLASS_CANNOT_GET_FIELD"));
    }
}
