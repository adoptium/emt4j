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
package org.eclipse.emt4j.agent.jdk8;

import org.eclipse.emt4j.agent.common.Constant;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerProvider;
import org.eclipse.emt4j.agent.common.jdkdependent.GuessCallerInfo;
import sun.reflect.Reflection;

import java.util.Arrays;
import java.util.Optional;

import static org.eclipse.emt4j.agent.common.Constant.*;
import static org.eclipse.emt4j.common.util.JdkClassUtil.isJdkClass;

/**
 * JDK 8 implementation of CallerProvider
 */
public class Java8CallerProvider implements CallerProvider {
    @Override
    public Optional<GuessCallerInfo> guessCallers(int maxCallerNum) {
        if (Constant.AGENT_REPORT_WRITE_THREAD.equals(Thread.currentThread().getName())) {
            return Optional.empty();
        }

        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        int foundNum = 0;
        String[] callerClassName = new String[maxCallerNum];
        for (int i = 0; i < stacktrace.length; i++) {
            String cn = stacktrace[i].getClassName();
            if (cn.startsWith(AGENT_PACKAGE) || cn.startsWith(COMMON_PACKAGE) || isJdkClass(cn)) {
                continue;
            } else {
                if (foundNum < maxCallerNum) {
                    callerClassName[foundNum++] = stacktrace[i].getClassName();
                } else {
                    break;
                }
            }
        }

        if (foundNum == 0) {
            return Optional.empty();
        } else {
            GuessCallerInfo guessCallerInfo = new GuessCallerInfo(foundNum);

            //Thread.getStackTrace() result not one-to-one with Reflection.getCallerClass
            int matchedNum = 0;
            for (int i = 0; ; i++) {
                Class callerClass = Reflection.getCallerClass(i);
                if (callerClass == null) {
                    return Optional.empty();
                } else if (inList(callerClass.getName(), callerClassName)) {
                    guessCallerInfo.getCallerClasses()[matchedNum++] = callerClass;
                    if (matchedNum >= foundNum) {
                        break;
                    }
                }
            }
            guessCallerInfo.setStacktrace(stacktrace);
            return Optional.of(guessCallerInfo);
        }
    }

    private boolean inList(String name, String[] callerClassName) {
        for (int i = 0; i < callerClassName.length; i++) {
            if (callerClassName[i] != null && callerClassName[i].equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Class> getNonJdkCallerByDepth(int callerDepth) {
        if (Constant.AGENT_REPORT_WRITE_THREAD.equals(Thread.currentThread().getName())) {
            return Optional.empty();
        }

        Class callerClass = Reflection.getCallerClass(callerDepth);
        if (callerClass == null || isJdkClass(callerClass.getName())) {
            return Optional.empty();
        }
        return Optional.of(callerClass);
    }

    @Override
    public Optional<CallerInfo> getCallerInfo(Class callerClass, int from) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement st : stacktrace) {
            if (st.getClassName().equals(callerClass.getName())) {
                CallerInfo callerInfo = new CallerInfo();
                callerInfo.setCallerClass(callerClass);
                callerInfo.setCallerMethod(st.getMethodName());
                callerInfo.setCalleeClass(stacktrace[from].getClassName());
                callerInfo.setCalleeMethod(stacktrace[from].getMethodName());
                callerInfo.setStacktrace(Arrays.copyOfRange(stacktrace, from, stacktrace.length));
                return Optional.of(callerInfo);
            }
        }
        return Optional.empty();
    }
}
