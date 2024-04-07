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
package org.eclipse.emt4j.test.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Only a Test class contains a annotation TestConf regard as SIT Testcase.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestConf {
    ModeEnum[] mode();

    RELEASE from() default RELEASE.JDK8;

    RELEASE to() default RELEASE.JDK11;

    String option() default "";

    enum ModeEnum {
        AGENT, CLASS, DYNAMIC, MAVEN_PLUGIN
    }

    enum RELEASE {
        JDK8("8"), JDK11("11"), JDK17("17"), JDK21("21");
        private String value;

        RELEASE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
