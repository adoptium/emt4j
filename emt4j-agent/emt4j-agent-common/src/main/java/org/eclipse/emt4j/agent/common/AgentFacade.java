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
package org.eclipse.emt4j.agent.common;

import org.eclipse.emt4j.agent.common.file.BinaryFileWriter;
import org.eclipse.emt4j.agent.common.file.Recorder;
import org.eclipse.emt4j.agent.common.file.ReportRecorder;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerProvider;
import org.eclipse.emt4j.agent.common.jdkdependent.GuessCallerInfo;
import org.eclipse.emt4j.common.*;
import org.eclipse.emt4j.common.classanalyze.ClassInspectorInstance;
import org.eclipse.emt4j.common.rule.InstanceRuleManager;
import org.eclipse.emt4j.common.util.ClassURL;

import java.io.File;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provide public API to Agent's other part.
 * <ul>
 *     <li>Initialize all components of agent</li>
 *     <li>Provide record* API </li>
 *     <li>Provide get CallerProvider</li>
 * </ul>
 */
public class AgentFacade {

    private static Recorder recorder;

    private static LoadedJarRecorder loadedJarRecorder;

    private static AgentOption agentOption;

    private static CallerProvider callerProvider;

    private static final int GUESS_CALLER_NUM = 3;

    /**
     * initialize all components of agent
     * <ul>
     *     <li>Parse arguments</li>
     *     <li>Initialize Caller Provider</li>
     *     <li>Initialize rules</li>
     *     <li>Initialize Reporter</li>
     * </ul>
     */
    static synchronized void init(String args, JdkDependConfig jdkDependConfig) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        parseArgs(args, jdkDependConfig.getFromVersion());
        initCallerProvider(jdkDependConfig.getCallerProviderClassName());
        initInstanceRules(jdkDependConfig.getRuleClasses());
        CheckConfig checkConfig = new CheckConfig();
        checkConfig.setCheckMode(Feature.DEFAULT);
        checkConfig.setFromVersion(agentOption.getFromVersion());
        checkConfig.setToVersion(agentOption.getToVersion());
        checkConfig.setPriority(agentOption.getPriority());
        List<String> features = new ArrayList<>();
        //now agent only support arch independent check. so we set a default
        features.add(Feature.DEFAULT.getId());

        recorder = new ReportRecorder(new BinaryFileWriter(getOutputFile(), agentOption.getFromVersion(), agentOption.getToVersion(), features));
        recorder.init();
        loadedJarRecorder = new LoadedJarRecorder(recorder);
        SystemClassLoaderIReference.init();
        ClassURL.registerUrlProtocolHandler();
    }

    private static void initCallerProvider(String callerProviderClassName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        AgentFacade.callerProvider = (CallerProvider) Class.forName(callerProviderClassName).newInstance();
    }

    private static void initInstanceRules(String[] classList) {
        InstanceRuleManager.init(classList, new Feature[]{Feature.DEFAULT}, new String[]{"agent"},
                agentOption.getFromVersion(), agentOption.getToVersion(), agentOption.getPriority());
    }

    private static void parseArgs(String args, int fromVersion) {
        agentOption = new AgentOption();
        agentOption.setFromVersion(fromVersion);
        if (args != null && !"".equals(args)) {
            String[] paramArray = args.split(",");
            if (paramArray != null && paramArray.length > 0) {
                for (String param : paramArray) {
                    String[] kv = param.split("=");
                    if (kv == null || kv.length != 2) {
                        throw new RuntimeException("Illegal agent parameters for : [" + param + "]");
                    }
                    switch (kv[0]) {
                        case "file":
                            agentOption.setOutputFile(kv[1]);
                            break;
                        case "to":
                            agentOption.setToVersion(Integer.parseInt(kv[1]));
                            break;
                        case "locale":
                            agentOption.setLocale(new Locale(kv[1]));
                            break;
                        case "priority":
                            agentOption.setPriority(kv[1]);
                            break;
                        default:
                            throw new RuntimeException("Illegal agent parameters for : [" + param + "]");
                    }
                }
            }
        }
        agentOption.check();
    }

    public static void record(Dependency dependency) {
        try {
            recorder.record(dependency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void recordLoadJar(ProtectionDomain protectionDomain) throws InterruptedException {
        loadedJarRecorder.recordJar(protectionDomain);
    }

    public static void recordLoadClass(String className, ProtectionDomain protectionDomain, byte[] classContent) throws InterruptedException {
        Optional<GuessCallerInfo> callerInfo = getCallerProvider().guessCallers(GUESS_CALLER_NUM);
        if (callerInfo.isPresent()) {
            Dependency dependency = DependencyBuilder.buildLoadClass(className, callerInfo.isPresent() ? callerInfo.get().getStacktrace() : null, protectionDomain);
            dependency.setNonJdkCallerClass(callerInfo.get().getCallerClasses());
            dependency.setCurrClassBytecode(classContent);
            recorder.record(dependency);

            dependency = dependency.clone();
            dependency.setTarget(new DependTarget.Class(className, DependType.WHOLE_CLASS));
            dependency.setClassSymbol(ClassInspectorInstance.getInstance().getSymbolInClass(classContent));
            recorder.record(dependency);
        }
    }

    private static File getOutputFile() {
        if (agentOption.getOutputFile() != null) {
            verify(agentOption.getOutputFile());
            return new File(agentOption.getOutputFile());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return new File("emt4j-" + sdf.format(new Date()) + ".dat");
    }

    private static void verify(String file) {
        File f = new File(file);
        if (f.exists() && f.isDirectory()) {
            throw new RuntimeException("The file :" + file + "cannot be a existing directory!");
        }
        if (f.getParentFile() != null && !f.getParentFile().exists()) {
            if (f.getParentFile().mkdirs()) {
                throw new RuntimeException("Create parent directory for file : " + file + " failed!");
            }
        }
    }

    public static CallerProvider getCallerProvider() {
        return callerProvider;
    }
}
