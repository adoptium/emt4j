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
package org.eclipse.emt4j.agent.common.rule.accessible;

import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.agent.common.methodvisitor.BaseEnterCallback;
import org.eclipse.emt4j.agent.common.AgentFacade;
import org.eclipse.emt4j.agent.common.Constant;
import org.eclipse.emt4j.agent.common.DependencyBuilder;
import org.eclipse.emt4j.agent.common.InstrumentCodeCallback;

import java.lang.reflect.*;
import java.util.Optional;

import static org.eclipse.emt4j.common.util.JdkClassUtil.isJdkClass;

/**
 * Callback when enter method:java.lang.reflect.AccessibleObject.setAccessible()
 */
@InstrumentCodeCallback
public class AccessibleCallback extends BaseEnterCallback {

    public static void recordStaticAccessible(AccessibleObject[] array, boolean flag) {
        if (flag) {
            Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
            if (callerClass.isPresent()) {
                Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
                if (callerInfo.isPresent()) {
                    for (AccessibleObject accessibleObject : array) {
                        recordAccessible(accessibleObject, callerInfo.get());
                    }
                }
            }
        }
    }

    public static void recordInstanceAccessible(AccessibleObject accessibleObject, boolean flag) {
        if (flag) {
            Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
            if (callerClass.isPresent()) {
                Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
                recordAccessible(accessibleObject, callerInfo.get());
            }
        }
    }

    private static void recordAccessible(AccessibleObject accessible, CallerInfo callerInfo) {
        Class<?> declaringClass = null;
        //Test if access a non-public field.
        if (accessible instanceof Executable) {
            declaringClass = ((Executable) accessible).getDeclaringClass();
            if (accessible instanceof Constructor) {
                Constructor constructor = (Constructor) accessible;
                if (Modifier.isPublic(constructor.getModifiers())) {
                    return;
                }
            } else if (accessible instanceof Method) {
                Method method = (Method) accessible;
                if (Modifier.isPublic(method.getModifiers())) {
                    return;
                }
            }
        } else if (accessible instanceof Field) {
            Field field = (Field) accessible;
            if (Modifier.isPublic(field.getModifiers())) {
                return;
            }
            declaringClass = ((Field) accessible).getDeclaringClass();
        }
        if (declaringClass != null && !declaringClass.isAnonymousClass() && !declaringClass.isSynthetic()) {
            //we only pay attention to jdk class so that help generate --add-opens to JVM option.
            if (isJdkClass(declaringClass.getName())) {
                try {
                    if (!callerInfo.getCallerClass().getName().startsWith(Constant.AGENT_PACKAGE)
                            && !callerInfo.getCallerClass().getName().startsWith(Constant.COMMON_PACKAGE)) {
                        AgentFacade.record(DependencyBuilder.buildDeepReflection(callerInfo,
                                declaringClass.getName(), callerInfo.getStacktrace()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
