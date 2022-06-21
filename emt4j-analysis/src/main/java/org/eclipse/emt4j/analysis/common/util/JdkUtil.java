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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdkUtil {

    private static final Map<File, Integer> path2JdkVersion = new ConcurrentHashMap();

    public static String getJdkToolPath(String jdkHome, String toolName) {
        String osName = System.getProperty("os.name");
        boolean windows = osName != null && osName.toLowerCase().indexOf("windows") != -1;
        return jdkHome + File.separator + "bin" + File.separator + toolName + (windows ? ".exe" : "");
    }

    /**
     * Search all possible JDK in the following paths:
     * <ul>
     *     <li>User provide jdk home </li>
     *     <li>Environment variable : JAVA_HOME</li>
     *     <li>Environment variable : PATH</li>
     * </ul>
     */
    public static File searchTargetJdk(String targetJdkHome, int targetJdkVersion, boolean verbose) throws IOException, InterruptedException {
        String osName = System.getProperty("os.name");
        boolean windows = osName != null && osName.toLowerCase().indexOf("windows") != -1;
        List<File> candidate = new ArrayList<>();
        for (String mayBeJavaHome : new String[]{targetJdkHome, SystemUtils.getJavaHome().getCanonicalPath()}) {
            if (StringUtils.isNotEmpty(mayBeJavaHome)) {
                File javaFile = getJavaBinPath(mayBeJavaHome, windows);
                if (javaFile != null) {
                    candidate.add(javaFile);
                }
            }
        }

        String envPath = System.getenv("PATH");
        if (StringUtils.isNotEmpty(envPath)) {
            String[] paths = envPath.split(File.pathSeparator);
            for (String path : paths) {
                File javaFile = getJavaBinPath(path,windows);
                if (javaFile != null) {
                    candidate.add(javaFile);
                }
            }
        }


        File toRunClass = prepareGetJavaVersionClass();
        for (File f : candidate) {
            int version = getMajorVersion(verbose, f, toRunClass);
            if (version != -1 && version == targetJdkVersion) {
                return f.getParentFile().getParentFile();
            }
        }
        return null;
    }

    /**
     * Extract the "GetJavaVersion16.class" from the resource to a temporary file
     */
    private static File prepareGetJavaVersionClass() throws IOException {
        Path dir = Files.createTempDirectory("emt4j");
        File target = new File(dir.toFile(), "GetJavaVersion16.class");
        if (target.exists()) {
            return target;
        }

        //why use .classfile instead of .class? because the .class not add to VCS.
        try (InputStream in = JdkUtil.class.getResourceAsStream("/GetJavaVersion16.classfile");
             OutputStream os = new FileOutputStream(target)) {
            byte[] buffer = new byte[4096];
            int len = in.read(buffer);
            while (len != -1) {
                os.write(buffer, 0, len);
                len = in.read(buffer);
            }
            os.flush();
        } finally {
            target.deleteOnExit();
        }
        return target;
    }

    private static File getJavaBinPath(String javaHome, boolean windows) {
        File javaFile = new File(javaHome + File.separator + "bin" + File.separator + "java" + (windows ? ".exe" : ""));
        return javaFile.exists() && javaFile.isFile() ? javaFile : null;
    }


    private static int getMajorVersion(boolean verbose, File javaPath, File toRunClass) throws IOException {
        if (path2JdkVersion.containsKey(javaPath)) {
            return path2JdkVersion.get(javaPath);
        } else {
            int version = getVersionByRunProcess(verbose, javaPath, toRunClass);
            path2JdkVersion.put(javaPath, version);
            return version;
        }
    }

    /**
     * It seems a little strange to the java version.
     * Normally we run the 'java -version' to get the version, but for different JDK vendors,
     * the output of 'java -version' is also not the same.
     * Parsing the output of 'java -version' is error-prone.
     * But getting the java version from java code is simple and stable,
     * so compile the "GetJavaVersion16.java" with JDK 1.6 and save the class file to resources.
     */
    private static int getVersionByRunProcess(boolean verbose, File javaPath, File toRunClass) throws IOException {
        String output = ProcessUtil.run(javaPath.getCanonicalPath(), "-cp",
                toRunClass.getParentFile().getCanonicalPath(), "GetJavaVersion16");
        Pattern p = Pattern.compile("^\\[(\\d+)\\]$");
        Matcher m = p.matcher(output.replace('\n', ' ').trim());
        if (m.matches()) {
            if (verbose) {
                System.out.println("Output of java version :" + javaPath.getCanonicalPath() + "," + output + "Parsed java version:" + m.group(1));
            }
            return Integer.parseInt(m.group(1));
        } else {
            if (verbose) {
                System.out.println("Output of java version :" + javaPath.getCanonicalPath() + "," + output + ".Cannot get java version");
            }
            return -1;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(searchTargetJdk(null, 8, true));
    }
}
