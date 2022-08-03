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

import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.util.ClassURL;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A Builder class provide miscellaneous build* method
 */
public class DependencyBuilder {

    public static Dependency buildJvmOption(List<String> arguments) {
        return new Dependency(null, new DependTarget.VMOption(join(arguments)), null, null);
    }

    public static Dependency buildLoadClass(String className, StackTraceElement[] stacktrace, ProtectionDomain protectionDomain) {
        URL url = getJar(protectionDomain);
        Dependency dependency = new Dependency(url, new DependTarget.Class(className, DependType.CLASS), stacktrace,
                url != null ? url.getFile() : null);
        return dependency;
    }

    private static URL getJar(ProtectionDomain protectionDomain) {
        if (protectionDomain != null && protectionDomain.getCodeSource() != null && protectionDomain.getCodeSource().getLocation() != null) {
            return protectionDomain.getCodeSource().getLocation();
        }
        return null;
    }

    public static Dependency buildCodeSource(URL location) {
        return new Dependency(null, new DependTarget.Location(location), null, location.getFile());
    }

    public static Dependency buildMethod(CallerInfo callerInfo, String className, String method) {
        Dependency dependency = new Dependency(ClassURL.create(callerInfo.getCallerClass().getName(), callerInfo.getCallerMethod()),
                new DependTarget.Method(className, method, DependType.METHOD), callerInfo.getStacktrace(), getFile(callerInfo.getCallerClass()));
        dependency.setCallerClass(callerInfo.getCallerClass());
        dependency.setCallerMethod(callerInfo.getCallerMethod());
        return dependency;
    }

    private static String getFile(Class callerClass) {
        if (callerClass != null) {
            ProtectionDomain pd = callerClass.getProtectionDomain();
            if (pd != null) {
                CodeSource cs = pd.getCodeSource();
                if (cs != null) {
                    URL url = cs.getLocation();
                    if (url != null) {
                        return url.getFile();
                    }
                }
            }
        }
        return null;
    }

    public static Dependency buildMethod(CallerInfo callerInfo, String className, String method, Map<String, Object> context) {
        Dependency dependency = new Dependency(ClassURL.create(callerInfo.getCallerClass().getName(), callerInfo.getCallerMethod()),
                new DependTarget.Method(className, method, DependType.METHOD), callerInfo.getStacktrace(), getFile(callerInfo.getCallerClass()));
        dependency.setCallerClass(callerInfo.getCallerClass());
        dependency.setCallerMethod(callerInfo.getCallerMethod());
        dependency.setContext(context);
        return dependency;
    }

    public static Dependency buildDeepReflection(CallerInfo callerInfo, String className, StackTraceElement[] stackTrace) {
        return new Dependency(ClassURL.create(callerInfo.getCallerClass().getName(), callerInfo.getCallerMethod()),
                new DependTarget.Class(className, DependType.METHOD_TO_CLASS_DEEP_REFLECTION), stackTrace, getFile(callerInfo.getCallerClass()));
    }

    private static String join(List<String> arguments) {
        if (null == arguments || arguments.isEmpty()) {
            return "";
        } else {
            return arguments.stream().collect(Collectors.joining(" "));
        }
    }
}
