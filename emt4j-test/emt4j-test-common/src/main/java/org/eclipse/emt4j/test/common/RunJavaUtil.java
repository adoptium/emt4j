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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RunJavaUtil {
    public static String getJavaExePath(RunningTestParam testParam, String selectJdk) {
        if (testParam.jdkVersionToHome.containsKey(selectJdk)) {
            return testParam.jdkVersionToHome.get(selectJdk) + File.separator + "bin" + File.separator + "java";
        } else {
            throw new RuntimeException("Cannot find java home for version: " + selectJdk);
        }
    }

    public static void runProcess(List<String> arguments) throws IOException, InterruptedException {
        runProcess(arguments, null, null, null);
    }

    public static void runProcess(List<String> arguments, File workingDirectory, File stdout, Map<String,String> environment) throws IOException, InterruptedException {
        System.out.println("run process: " + String.join(" ", arguments));
        ProcessBuilder pb = new ProcessBuilder(arguments);
        if (workingDirectory != null) {
            pb.directory(workingDirectory);
        }
        if (stdout == null) {
            pb.inheritIO();
        } else {
            pb.redirectOutput(stdout);
        }
        if (environment != null) {
            Map<String, String> processEnvironment = pb.environment();
            processEnvironment.putAll(environment);
        }
        Process p = pb.start();
        int ret = p.waitFor();
        if (ret != 0) {
            throw new RuntimeException("Return code: [" + ret + "] with " + String.join(" ", arguments));
        }
    }
}
