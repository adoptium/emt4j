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
package org.eclipse.emt4j.test.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * emt4j-agent records necessary information by java-agent.
 * When test the emt4j-agent, it need running a java process with a emt4j-agent.
 * Running a new java process with emt4j-agent, and the java process call the run method of the testcase.
 */
public class RunWithAgentMain {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, InterruptedException {
        Class testClass = Class.forName(args[0]);
        Method runMethod = testClass.getDeclaredMethod("run");
        Object o = testClass.newInstance();
        runMethod.invoke(o);

        //There a background thread in emt4j-agent that write the found problems to file. If not wait a while
        //,it may lose some data.
        Thread.sleep(3000);
    }
}
