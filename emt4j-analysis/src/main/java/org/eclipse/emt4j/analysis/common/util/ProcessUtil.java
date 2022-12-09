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
package org.eclipse.emt4j.analysis.common.util;

import java.io.*;
import java.util.List;

public class ProcessUtil {
    public static String run(String... commands) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
        String s = null;
        StringBuffer sb = new StringBuffer();
        while ((s = stdError.readLine()) != null) {
            sb.append(s).append('\n');
        }
        while ((s = stdInput.readLine()) != null) {
            sb.append(s).append('\n');
        }
        return sb.toString();
    }

    /**
     * To avoid process blocking due to too many console output
     * @param commands
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int noBlockingRun(List<String> commands) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(commands);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        return pb.start().waitFor();
    }
}
