/********************************************************************************
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.eclipse.emt4j.test.common.TestConf.ModeEnum;
import static org.eclipse.emt4j.test.common.TestConf.RELEASE;

@TestConf(mode = ModeEnum.AGENT, from = RELEASE.JDK11, to = RELEASE.JDK17)
public class SecurityClassesGetDeclareFieldTest extends SITBaseCase {
    public void run() {
        try {
            AccessibleObject.class.getDeclaredField("override");
            Class.class.getDeclaredField("module");
            ClassLoader.class.getDeclaredField("parent");
            Constructor.class.getDeclaredField("root");
            Field.class.getDeclaredField("name");
            Method.class.getDeclaredField("name");
            Module.class.getDeclaredField("layer");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void verify(JsonReport jsonReport) {
        assertTrue(matchAny(jsonReport, "SECURITY_CLASS_CANNOT_GET_FIELD"));
    }
}
