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

import org.eclipse.emt4j.common.JsonReport;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Running a testcase include 2 separated steps.
 * <ol>
 *     <li>Running analysis or Running with java agent.The result will write to immediate data file</li>
 *     <li>Checking if all expected problems are found which input file is the previous step's output file</li>
 * </ol>
 */
public class RunWithCheckMain {
    public static void main(String[] args) throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Class checkClass = Class.forName(args[0]);
        JsonReport report = null;
        if (args.length >= 2 && new File(args[1]).exists()) {
            String content = FileUtils.readFileToString(new File(args[1]));
            //Both running javaagent and analysis use json as immediate format.
            report = new Gson().fromJson(content, JsonReport.class);
        }
        Method checkMethod = checkClass.getMethod("verify", JsonReport.class);
        //Throw exception regard as failed
        checkMethod.invoke(checkClass.getDeclaredConstructor().newInstance(), report);
    }
}
