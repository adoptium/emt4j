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

import java.lang.reflect.Method;

/**
 * When test target is dynamic,should call this class' main.
 */
public class RunWithDynamicTestTargetMain {

    /**
     * args[0]: The test class that generate the dynamic jar or classes
     * args[1]: The work dir that save the dynamic jar or classes
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Class checkClass = Class.forName(args[0]);
        String workDir = args[1];
        Method method = checkClass.getMethod("prepareDynamicTestTarget", String.class);
        method.invoke(checkClass.getDeclaredConstructor().newInstance(), workDir);
    }
}
