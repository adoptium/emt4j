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
package org.eclipse.emt4j.testjdk8;

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.test.common.SITBaseCase;
import org.eclipse.emt4j.test.common.TestConf;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

@TestConf(mode = TestConf.ModeEnum.AGENT, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class NettyTest extends SITBaseCase {

    public void run() throws IOException, ClassNotFoundException {
        File nettyJarFile = new File(System.getProperty("java.io.tmpdir"), "netty-all-4.0.0.final.jar");
        try (InputStream in = NettyTest.class.getResourceAsStream("/netty-all-4.0.0.final.testdata");
             OutputStream os = new FileOutputStream(nettyJarFile)) {
            byte[] buffer = new byte[4096];
            int len = in.read(buffer);
            while (len != -1) {
                os.write(buffer, 0, len);
                len = in.read(buffer);
            }
            os.flush();
            URLClassLoader ucl = new URLClassLoader(new URL[]{nettyJarFile.toURI().toURL()});
            //load any class in netty,so trigger load the netty jar
            ucl.loadClass("io.netty.bootstrap.Bootstrap");
        }
    }

    @Override
    public void verify(JsonReport jsonReport) {
        matchAny(jsonReport, "INCOMPATIBLE_JAR_NETTY-ALL");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        NettyTest nettyTest = new NettyTest();
        nettyTest.run();
    }
}
