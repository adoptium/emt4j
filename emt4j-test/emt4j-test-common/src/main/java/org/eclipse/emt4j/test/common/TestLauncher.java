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

import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Process all details when running an SIT test case.
 * <ol>
 *     <li>Find a proper toolchain configuration</li>
 *     <li>Find all SIT test suites from test jars.</li>
 *     <li>Running each test suite.</li>
 * </ol>
 */
public class TestLauncher {
    public static void main(String[] args) throws IOException, InterruptedException {
        File[] testcaseJars = new File[0];
        RunningTestParam testParam = new RunningTestParam();
        List<String> candidateToolChainFile = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if ("--tool-chains".equals(args[i])) {
                candidateToolChainFile.add(args[++i]);
            } else if ("--test-cases".equals(args[i])) {
                testcaseJars = new File((args[++i])).listFiles((d, n) -> n.endsWith(".jar"));
            } else if ("--test-common-cp".equals(args[i])) {
                testParam.testCommonClassPath = args[++i];
            } else if ("--agent-lib-dir".equals(args[i])) {
                testParam.agentLibDir = args[++i];
            } else if ("--analysis-lib-dir".equals(args[i])) {
                testParam.analysisLibDir = args[++i];
            } else if ("--agent-version".equals(args[i])) {
                testParam.agentVersion = args[++i];
            } else {
                throw new RuntimeException("Unknown option : " + args[i]);
            }
        }
        findToolchains(testParam, candidateToolChainFile);

        File playgroundBase = Files.createTempDirectory("emt4j-test").toFile();
        for (File testcaseJar : testcaseJars) {
            File playground = new File(playgroundBase, testcaseJar.getName());
            if (!playground.mkdir()) {
                throw new RuntimeException("Create directory for file : " + playground + " failed!");
            }
            TestCaseSuite testcaseSuite = findTestcaseSuite(testParam, testcaseJar, playgroundBase);
            testcaseSuite.run(testParam, playground);
        }
    }

    static void findToolchains(RunningTestParam testParam, List<String> candidateToolChainFile) {
        //User provided toolchain has a higher priority.
        //This is a default toolchains.xml location.
        candidateToolChainFile.add(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "toolchains.xml");
        for (String toolchainFile : candidateToolChainFile) {
            File f = new File(toolchainFile);
            if (f.exists()) {
                System.out.println("Toolchains file found at : " + toolchainFile + "!");
                testParam.jdkVersionToHome = ToolchainConfReader.parseToolChainsConfig(toolchainFile);
                break;
            } else {
                System.out.println("Toolchains file not found at: " + toolchainFile + "!");
            }
        }
        if (null == testParam.jdkVersionToHome || testParam.jdkVersionToHome.isEmpty()) {
            throw new RuntimeException("Cannot find toolchains.xml! The file can be located at ~/.m2 or pass by parameter \"--tool-chains\"");
        }
    }

    private static TestCaseSuite findTestcaseSuite(RunningTestParam testParam, File testcaseJar, File playgroundBase) throws IOException, InterruptedException {
        TestCaseSuite testCaseSuite = new TestCaseSuite();
        testCaseSuite.testcaseJar = testcaseJar;
        try (JarFile jarFile = new JarFile(testcaseJar)) {
            String runJdkVersion = getRunJdkVersion(testParam, testcaseJar, jarFile);
            testCaseSuite.testCaseList = readTestcaseList(runJdkVersion, testcaseJar, testParam, playgroundBase);
        }

        return testCaseSuite;
    }

    private static List<TestCase> readTestcaseList(String runJdkVersion, File testcaseJar, RunningTestParam testParam, File playgroundBase) throws IOException, InterruptedException {
        File testConfTmpFile = File.createTempFile("testconf", ".txt", playgroundBase);
        RunJavaUtil.runProcess(buildRunFindAllTestcase(testParam, runJdkVersion, testcaseJar.getAbsolutePath(), testConfTmpFile.getAbsolutePath()));
        Type listType = new TypeToken<ArrayList<TestConfObject>>() {
        }.getType();
        List<TestConfObject> testConfObjectList = new Gson().fromJson(FileUtils.readFileToString(testConfTmpFile.getAbsoluteFile(), "utf-8"),
                listType);
        return testConfObjectList.stream().map((conf) -> {
            TestCase testCase = new TestCase();
            testCase.from = TestConf.RELEASE.valueOf(conf.fromVersion);
            testCase.to = TestConf.RELEASE.valueOf(conf.toVersion);
            testCase.className = conf.testClassName;
            testCase.option = conf.option;
            testCase.modes = conf.modes.stream().map((m) -> TestConf.ModeEnum.valueOf(m)).collect(Collectors.toList());
            return testCase;
        }).collect(Collectors.toList());
    }

    private static String getRunJdkVersion(RunningTestParam testParam, File testcaseJar, JarFile jarFile) throws IOException {
        String runJdkVersion = jarFile.getManifest().getMainAttributes().getValue("run-jdk-version");
        if (null == runJdkVersion || "".equals(runJdkVersion)) {
            throw new RuntimeException("Test jar" + testcaseJar.getName() + "'s mainfest should contain attribute with run-jdk-version");
        }
        if (!testParam.jdkVersionToHome.containsKey(runJdkVersion)) {
            throw new RuntimeException("Not found jdk home for version: " + runJdkVersion);
        }
        return runJdkVersion;
    }

    private static List<String> buildRunFindAllTestcase(RunningTestParam testParam, String jdkVersion, String testcaseJar, String outputFile) {
        List<String> arguments = new ArrayList<>();
        arguments.add(RunJavaUtil.getJavaExePath(testParam, jdkVersion));
        //class path
        arguments.add("-cp");
        arguments.add(testParam.testCommonClassPath + File.pathSeparator + testcaseJar + File.pathSeparator + testParam.analysisLibDir + File.separator + "*");
        //main class
        arguments.add("org.eclipse.emt4j.test.common.FindAllTestcaseMain");
        //to load jar
        arguments.add(testcaseJar);
        //where to write the result
        arguments.add(outputFile);
        return arguments;
    }
}