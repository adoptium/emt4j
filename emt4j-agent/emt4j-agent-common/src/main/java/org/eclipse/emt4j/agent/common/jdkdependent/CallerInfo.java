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
package org.eclipse.emt4j.agent.common.jdkdependent;

/**
 * Caller and Callee related information.
 */
public class CallerInfo {
    private Class callerClass;
    private String callerMethod;
    private StackTraceElement[] stacktrace;
    private String calleeClass;
    private String calleeMethod;

    public Class getCallerClass() {
        return callerClass;
    }

    public void setCallerClass(Class callerClass) {
        this.callerClass = callerClass;
    }

    public String getCallerMethod() {
        return callerMethod;
    }

    public void setCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
    }

    public StackTraceElement[] getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(StackTraceElement[] stacktrace) {
        this.stacktrace = stacktrace;
    }

    public String getCalleeClass() {
        return calleeClass;
    }

    public void setCalleeClass(String calleeClass) {
        this.calleeClass = calleeClass;
    }

    public String getCalleeMethod() {
        return calleeMethod;
    }

    public void setCalleeMethod(String calleeMethod) {
        this.calleeMethod = calleeMethod;
    }
}
