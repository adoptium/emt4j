/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.agent.jdk17;

import org.eclipse.emt4j.agent.common.JdkDependConfig;
import org.eclipse.emt4j.agent.common.methodvisitor.TransformerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static org.eclipse.emt4j.agent.common.Constant.*;

/**
 * Entry for agent running in JDK 17
 */
public class MainAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        versionCheck();
        // add agent path to bootstrap so that we can modify jdk class
        String agentPath = null;
        if (MainAgent.class.getProtectionDomain() != null && MainAgent.class.getProtectionDomain().getCodeSource() != null
                && MainAgent.class.getProtectionDomain().getCodeSource().getLocation() != null) {
            agentPath = MainAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (null == agentPath || !agentPath.endsWith(".jar")) {
                System.err.println("MainAgent must be load with .jar file.Current Path is :" + agentPath);
                return;
            }
        }

        try {
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(new File(agentPath)));
            JdkDependConfig jdkDependConfig = new JdkDependConfig(RULE_CLASS,
                    "org.eclipse.emt4j.agent.jdk17.Java17CallerProvider", 17, agentPath);
            Class<?> agentInit = MainAgent.class.getClassLoader().loadClass(INIT_CLASS);
            Method initMethod = agentInit.getMethod("init", String.class, Instrumentation.class, JdkDependConfig.class);
            initMethod.invoke(null, args, instrumentation, jdkDependConfig);

            Class[] loadedClasses = instrumentation.getAllLoadedClasses();
            List<Class> toRetransformClass = new ArrayList<>();
            for (Class loadedClass : loadedClasses) {
                //skip agent's classes
                if (loadedClass.getName() != null
                        && (loadedClass.getName().startsWith(AGENT_PACKAGE) || loadedClass.getName().startsWith(COMMON_PACKAGE))) {
                    continue;
                }
                if (TransformerFactory.needTransform(loadedClass.getName())) {
                    toRetransformClass.add(loadedClass);
                }
            }
            instrumentation.retransformClasses(toRetransformClass.toArray(new Class[toRetransformClass.size()]));
        } catch (IOException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException | UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    private static void versionCheck() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        if (!version.equals("17")) {
            throw new RuntimeException("Current agent can only run with java17,but actually run with :  " + version);
        }
    }

    private static String[] RULE_CLASS = new String[]{
            "org.eclipse.emt4j.common.rule.impl.AddExportsRule",
            "org.eclipse.emt4j.common.rule.impl.IncompatibleJarRule",
            "org.eclipse.emt4j.common.rule.impl.JvmOptionRule",
            "org.eclipse.emt4j.common.rule.impl.ReferenceClassRule",
            "org.eclipse.emt4j.common.rule.impl.TouchedMethodRule",
            "org.eclipse.emt4j.agent.common.rule.AddOpensRule",
            "org.eclipse.emt4j.agent.common.rule.AgentReferenceClassRule",
            "org.eclipse.emt4j.agent.common.rule.AgentTouchedMethodRule",
            "org.eclipse.emt4j.agent.common.rule.ArraysAsListToArrayRule",
            "org.eclipse.emt4j.agent.common.rule.CLDRCalendarFirstDayOfWeekRule",
            "org.eclipse.emt4j.agent.common.rule.CLDRDateFormatRule",
            "org.eclipse.emt4j.agent.common.rule.CLDRNumberFormatRule",
            "org.eclipse.emt4j.agent.common.rule.GetJavaVersionRule",
            "org.eclipse.emt4j.agent.common.rule.PatternCompileRule",
            "org.eclipse.emt4j.agent.common.rule.SystemClassLoaderRule",
            "org.eclipse.emt4j.agent.common.rule.SecurityClassGetDeclareFieldRule",
            "org.eclipse.emt4j.common.rule.impl.DeprecatedAPIRule"
    };
}
