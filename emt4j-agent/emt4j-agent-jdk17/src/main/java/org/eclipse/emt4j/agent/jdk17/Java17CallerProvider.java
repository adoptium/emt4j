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

import org.eclipse.emt4j.agent.common.Constant;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerProvider;
import org.eclipse.emt4j.agent.common.jdkdependent.GuessCallerInfo;
import org.eclipse.emt4j.common.util.MutableInteger;
import org.eclipse.emt4j.common.util.MutableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.eclipse.emt4j.agent.common.Constant.*;
import static org.eclipse.emt4j.common.util.JdkClassUtil.isJdkClass;

public class Java17CallerProvider implements CallerProvider {

    @Override
    public Optional<GuessCallerInfo> guessCallers(int maxCallerNum) {
        if (Constant.AGENT_REPORT_WRITE_THREAD.equals(Thread.currentThread().getName())) {
            return Optional.empty();
        }
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        List<StackTraceElement> stackTrace = new ArrayList<>();
        MutableInteger foundNum = new MutableInteger();
        Class[] callerClass = new Class[maxCallerNum];
        walker.forEach((f) -> {
            if (foundNum.getValue() < maxCallerNum) {
                if (!f.getClassName().startsWith(AGENT_PACKAGE)
                        && !f.getClassName().startsWith(COMMON_PACKAGE)
                        && !isJdkClass(f.getClassName())) {
                    callerClass[foundNum.getValue()] = f.getDeclaringClass();
                    foundNum.inc();
                }
            }
            stackTrace.add(f.toStackTraceElement());
        });
        if (foundNum.getValue() == 0) {
            return Optional.empty();
        } else {
            GuessCallerInfo guessCallerInfo = new GuessCallerInfo(foundNum.getValue());
            guessCallerInfo.setCallerClasses(callerClass);
            guessCallerInfo.setStacktrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
            return Optional.of(guessCallerInfo);
        }
    }

    @Override
    public Optional<Class> getNonJdkCallerByDepth(int callerDepth) {
        if (Constant.AGENT_REPORT_WRITE_THREAD.equals(Thread.currentThread().getName())) {
            return Optional.empty();
        }

        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        MutableInteger counter = new MutableInteger();
        MutableObject<Class> callerClass = new MutableObject<>();
        walker.forEach((f) -> {
            counter.inc();
            if (counter.getValue() == callerDepth) {
                callerClass.setValue(f.getDeclaringClass());
            }
        });
        if (callerClass.getValue() == null || isJdkClass(callerClass.getValue().getName())) {
            return Optional.empty();
        }
        return Optional.of(callerClass.getValue());
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
