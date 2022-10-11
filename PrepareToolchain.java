import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
public class PrepareToolchain {
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<toolchains>\n";
    private static final String BODY_TEMPLATE = "    <toolchain>\n" +
            "        <type>jdk</type>\n" +
            "        <provides>\n" +
            "            <version>%s</version>\n" +
            "            <vendor>openjdk</vendor>\n" +
            "        </provides>\n" +
            "        <configuration>\n" +
            "            <jdkHome>%s</jdkHome>\n" +
            "        </configuration>\n" +
            "    </toolchain>\n";
    private static final String FOOTER = "</toolchains>";

    public static void main(String[] args) throws IOException {
        final String varTag = "JAVA_HOME_";
        StringBuffer sb = new StringBuffer();
        sb.append(HEADER);
        System.getenv().forEach((k, v) -> {
            //JAVA_HOME_11_X64
            //JAVA_HOME_8_X64
            if (k.startsWith(varTag)) {
                String[] versionArch = k.substring(varTag.length()).split("_");
                if (versionArch.length == 2) {
                    sb.append(String.format(BODY_TEMPLATE, versionArch[0], v));
                }
            }
        });
        sb.append(FOOTER);

        String destFile = System.getenv("HOME") + File.separator + ".m2" + File.separator + "toolchains.xml";
        System.out.println("Generate " + " content of file : " + destFile);
        System.out.println(sb);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destFile))) {
            bw.write(sb.toString());
        }
    }
}
