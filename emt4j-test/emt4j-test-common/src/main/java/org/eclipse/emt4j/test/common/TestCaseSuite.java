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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.eclipse.emt4j.test.common.RunJavaUtil.*;


/**
 * A collection of test cases, and provide how to run each test case.
 * A test case can be run with a java agent or analysis directly. TestCaseSuite
 * acts as a bridge between these java processes.
 */
class TestCaseSuite {

    private static final boolean MAVEN_INSTALLED = checkCommandSuccess("mvn --version");
    private static final boolean GIT_INSTALLED = checkCommandSuccess("git --version");

    public static final String JDK_VERSION_RUN_ANALYSIS = "8";
    public static final String ANALYSIS_MAIN = "org.eclipse.emt4j.analysis.AnalysisMain";
    public static final String DYNAMIC_TARGET_MAIN = "org.eclipse.emt4j.test.common.RunWithDynamicTestTargetMain";
    File testcaseJar;
    List<TestCase> testCaseList;

    /**
     * @param testParam
     * @param playground
     */
    public void run(RunningTestParam testParam, File playground) throws IOException, InterruptedException {
        //why unzip? If not unzipped, the analysis will check the single jar. but we only want
        //check a given class in this jar.
        File unzipJarDir = unzip(testcaseJar, playground);
        System.out.println();
        List<String> succSummary = new ArrayList<>();
        List<String> failSummary = new ArrayList<>();
        for (TestCase testCase : testCaseList) {
            for (TestConf.ModeEnum mode : testCase.modes) {
                boolean success = false;
                try {
                    File testcasePlayground = new File(playground, testCase.className + mode.name());
                    if (!testcasePlayground.mkdir()) {
                        throw new RuntimeException("Create directory :" + testcasePlayground + " failed!");
                    }
                    switch (mode) {
                        case AGENT:
                            runWithAgent(testParam, testCase, testcasePlayground);
                            break;
                        case CLASS:
                            runWithClass(testParam, testCase, testcasePlayground, unzipJarDir);
                            break;
                        case DYNAMIC:
                            runWithDynamic(testParam, testCase, testcasePlayground);
                            break;
                        case MAVEN_PLUGIN:
                            runWithMavenPlugin(testParam, testCase, testcasePlayground);
                            break;
                        default:
                            throw new RuntimeException("Not support mode for TestConf annotation!" + mode);
                    }
                    success = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    String msg = "[" + mode + "] " + testCase.className + "[" + (success ? "OK" : "FAILED") + "]";
                    if (success) {
                        succSummary.add(msg);
                        ColorPrint.printGreen(msg);
                    } else {
                        failSummary.add(msg);
                        ColorPrint.printRed(msg);
                    }
                }
            }
        }

        System.out.println("==================== Summary ===============");
        for (String s : succSummary) {
            ColorPrint.printGreen(s);
        }
        for (String s : failSummary) {
            ColorPrint.printRed(s);
        }

        if (!failSummary.isEmpty()) {
            throw new RuntimeException("Some testcases execute failed!");
        }
    }

    private void runWithDynamic(RunningTestParam testParam, TestCase testCase, File testcasePlayground) throws IOException, InterruptedException {
        //1. generate dynamic test target
        File dynamicWorkDir = new File(testcasePlayground, "dynamic-test-target");
        if (!dynamicWorkDir.exists() && !dynamicWorkDir.mkdirs()) {
            throw new RuntimeException("Cannot create directory : " + dynamicWorkDir.getCanonicalPath());
        }
        runProcess(buildCallDynamicTarget(testParam, testCase, dynamicWorkDir));

        //2. run analysis
        File reportOutput = new File(testcasePlayground, "class-report.json");
        runProcess(buildAnalysisParamForClass(testParam, testCase, reportOutput, dynamicWorkDir.getCanonicalPath()));

        //3.run checker
        runProcess(buildCheckParam(testParam, testCase, reportOutput));
    }

    private List<String> buildCallDynamicTarget(RunningTestParam testParam, TestCase testCase, File dynamicWorkDir) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add(getJavaExePath(testParam, JDK_VERSION_RUN_ANALYSIS));
        arguments.add("-cp");
        arguments.add(testParam.testCommonClassPath + File.pathSeparator + testParam.analysisLibDir + File.separator + "*" + File.pathSeparator + testcaseJar);
        arguments.add(DYNAMIC_TARGET_MAIN);
        //args[0]
        arguments.add(testCase.className);
        //args[1]
        arguments.add(dynamicWorkDir.getCanonicalPath());
        return arguments;
    }

    private void runWithMavenPlugin(RunningTestParam testParam, TestCase testCase, File testcasePlayground) throws IOException, InterruptedException {
        if (!GIT_INSTALLED || !MAVEN_INSTALLED) {
            System.out.println("Skip " + testCase.className + " because maven or git is not installed");
            return;
        }

        File stdout = null;

        //1. generate dynamic test target
        runProcess(buildCallDynamicTarget(testParam, testCase, testcasePlayground));

        try {
            //2.run plugin
            stdout = Paths.get(testcasePlayground.getPath(), "stdout").toFile();
            Map<String, String> environment = new HashMap<>();
            environment.put("MAVEN_OPTS", "-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8");
            environment.put("JAVA_HOME", testParam.jdkVersionToHome.get(testCase.from.getValue()));
            runProcess(buildCallMavenPluginParams(testParam, testCase, testcasePlayground), testcasePlayground, stdout, environment);
        } catch (Exception e) {
            if (stdout != null && stdout.exists()) {
                // stdout may be too long. Print only if test failed
                System.out.println("test failed with stdout content:");
                System.out.println(FileUtils.readFileToString(stdout, "utf-8"));
            }
            throw e;
        }

        //3.run checker
        runProcess(buildCheckParam(testParam, testCase, null), testcasePlayground, null, null);
    }

    private List<String> buildCallMavenPluginParams(RunningTestParam testParam, TestCase testCase, File dynamicWorkDir) {
        String command = "mvn " + testCase.option.replace("${version}", testParam.projectVersion) + " -X";
        return Arrays.asList(command.split(" +"));
    }

    private File unzip(File testcaseJar, File playground) throws IOException, InterruptedException {
        File jarTmpDir = new File(playground, "tmp-classes");
        if (!jarTmpDir.mkdirs()) {
            throw new RuntimeException("Create directory : " + jarTmpDir + " failed!");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add("unzip");
        arguments.add(testcaseJar.getAbsolutePath());
        arguments.add("-d");
        arguments.add(jarTmpDir.getAbsolutePath());
        runProcess(arguments);
        return jarTmpDir;
    }

    private void runWithClass(RunningTestParam testParam, TestCase testCase, File testcasePlayground, File unzipJarDir) throws IOException, InterruptedException {
        //1.run the analysis
        File reportOutput = new File(testcasePlayground, "class-report.json");
        String targetFile = unzipJarDir.getAbsolutePath() + File.separator + toClassFile(testCase.className);
        runProcess(buildAnalysisParamForClass(testParam, testCase, reportOutput, targetFile));
        //2.run checker
        runProcess(buildCheckParam(testParam, testCase, reportOutput));
    }

    private void runWithAgent(RunningTestParam testParam, TestCase testCase, File testcasePlayground) throws IOException, InterruptedException {
        File agentOutput = new File(testcasePlayground, "agent-output.dat");
        File reportOutput = new File(testcasePlayground, "agent-report.json");
        runProcess(buildRunWithAgentParam(testParam, testCase, agentOutput));
        runProcess(buildAnalysisParamForAgent(testParam, testCase, agentOutput, reportOutput));
        runProcess(buildCheckParam(testParam, testCase, reportOutput));
    }

    private List<String> buildCheckParam(RunningTestParam testParam, TestCase testCase, File analysisOutput) {
        List<String> arguments = new ArrayList<>();
        arguments.add(getJavaExePath(testParam, testCase.from.getValue()));
        //class path
        arguments.add("-cp");
        arguments.add(testParam.testCommonClassPath + File.pathSeparator + testParam.analysisLibDir + File.separator + "*" + File.pathSeparator + testcaseJar);
        //main class
        arguments.add("org.eclipse.emt4j.test.common.RunWithCheckMain");
        //to run class
        arguments.add(testCase.className);
        //file to verify
        if (analysisOutput != null) {
            arguments.add(analysisOutput.getAbsolutePath());
        }
        return arguments;
    }

    private List<String> buildAnalysisParamForAgent(RunningTestParam testParam, TestCase testCase, File agentOutput, File analysisOutput) {
        List<String> arguments = new ArrayList<>();
        arguments.add(getJavaExePath(testParam, JDK_VERSION_RUN_ANALYSIS));
        arguments.add("-cp");
        arguments.add(testParam.analysisLibDir + File.separator + "*");
        arguments.add(ANALYSIS_MAIN);
        arguments.add("-o");
        arguments.add(analysisOutput.getAbsolutePath());
        arguments.add("-v");
        arguments.add("-p");
        arguments.add("json");

        testParam.jdkVersionToHome.forEach((ver, jdkHome) -> {
            if (ver.equals(testCase.to.getValue())) {
                arguments.add("-j");
                arguments.add(jdkHome);
            }
        });
        arguments.add(agentOutput.getAbsolutePath());
        return arguments;
    }

    private List<String> buildAnalysisParamForClass(RunningTestParam testParam, TestCase testCase, File output, String targetFile) {
        List<String> arguments = new ArrayList<>();
        arguments.add(getJavaExePath(testParam, JDK_VERSION_RUN_ANALYSIS));
        arguments.add("-cp");
        arguments.add(testParam.analysisLibDir + File.separator + "*");
        //main class
        arguments.add(ANALYSIS_MAIN);
        arguments.add("-o");
        arguments.add(output.getAbsolutePath());
        arguments.add("-v");
        arguments.add("-f");
        arguments.add(testCase.from.getValue());
        arguments.add("-t");
        arguments.add(testCase.to.getValue());
        arguments.add("-p");
        arguments.add("json");

        testParam.jdkVersionToHome.forEach((ver, jdkHome) -> {
            if (ver.equals(testCase.to.getValue())) {
                arguments.add("-j");
                arguments.add(jdkHome);
            }
        });

        //param
        arguments.add(targetFile);
        return arguments;
    }

    private String toClassFile(String className) {
        return className.replace('.', File.separatorChar) + ".class";
    }

    private List<String> buildRunWithAgentParam(RunningTestParam testParam, TestCase testCase, File agentOutput) {
        List<String> arguments = new ArrayList<>();
        arguments.add(getJavaExePath(testParam, testCase.from.getValue()));
        //class path
        arguments.add("-cp");
        arguments.add(testParam.testCommonClassPath + File.pathSeparator + testcaseJar + File.pathSeparator + testParam.analysisLibDir + File.separator + "*");
        //javaaent
        arguments.add(buildJavaAgentParam(testParam, testCase, agentOutput));
        if (StringUtils.isNotEmpty(testCase.option)) {
            arguments.add(testCase.option);
        }
        //main class
        arguments.add("org.eclipse.emt4j.test.common.RunWithAgentMain");
        //to run class
        arguments.add(testCase.className);
        return arguments;
    }

    private String buildJavaAgentParam(RunningTestParam testParam, TestCase testCase, File agentOutput) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("-javaagent:").append(testParam.agentLibDir + File.separator + "emt4j-agent-jdk" + testCase.from.getValue() + "-" + testParam.projectVersion + ".jar");
        sb.append('=');
        sb.append("file=").append(agentOutput.getAbsolutePath());
        sb.append(',').append("to=").append(testCase.to.getValue());
        return sb.toString();
    }

}
