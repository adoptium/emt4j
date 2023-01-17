/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.common;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <code>Dependency</code> is the core model of JDK migration tool.
 * It describes what depends on what, and the type and the context surrounding the dependency.
 */
public class Dependency implements Serializable, Cloneable {
    private String locationExternalForm;
    private DependTarget target;
    private String targetFilePath;

    private transient Class callerClass;
    private String callerMethod;
    private transient Class[] nonJdkCallerClass;
    private StackTraceElement[] stacktrace;
    private transient Map<String, Object> context;
    private transient byte[] currClassBytecode;
    private transient ClassSymbol classSymbol;

    private SourceInformation sourceInformation;

    private List<Integer> lines;

    public Dependency(URL location, DependTarget target, StackTraceElement[] stacktrace, String targetFilePath) {
        this.target = target;
        this.stacktrace = stacktrace;
        if (location != null) {
            locationExternalForm = location.toExternalForm();
        }
        this.targetFilePath = targetFilePath;
    }

    public Dependency() {
    }

    public DependType getDependType() {
        return target.type;
    }

    public DependTarget getTarget() {
        return target;
    }

    public String getLocationExternalForm() {
        return locationExternalForm;
    }

    public void setTarget(DependTarget target) {
        this.target = target;
    }

    public Class[] getNonJdkCallerClass() {
        return nonJdkCallerClass;
    }

    public void setNonJdkCallerClass(Class[] nonJdkCallerClass) {
        this.nonJdkCallerClass = nonJdkCallerClass;
    }

    public StackTraceElement[] getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(StackTraceElement[] stacktrace) {
        this.stacktrace = stacktrace;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Dependency clone() {
        Dependency cloned = new Dependency();
        cloned.target = target;
        cloned.nonJdkCallerClass = nonJdkCallerClass;
        cloned.stacktrace = stacktrace;
        cloned.context = context;
        cloned.callerClass = callerClass;
        cloned.callerMethod = callerMethod;
        cloned.locationExternalForm = locationExternalForm;
        cloned.targetFilePath = targetFilePath;
        cloned.sourceInformation = sourceInformation;
        cloned.lines = lines;
        return cloned;
    }

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

    public byte[] getCurrClassBytecode() {
        return currClassBytecode;
    }

    public void setCurrClassBytecode(byte[] currClassBytecode) {
        this.currClassBytecode = currClassBytecode;
    }

    public ClassSymbol getClassSymbol() {
        return classSymbol;
    }

    public void setClassSymbol(ClassSymbol classSymbol) {
        this.classSymbol = classSymbol;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    public void setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public SourceInformation getSourceInformation() {
        return sourceInformation;
    }

    public void setSourceInformation(SourceInformation sourceInformation) {
        this.sourceInformation = sourceInformation;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public void setLines(List<Integer> lines) {
        this.lines = lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(locationExternalForm, that.locationExternalForm) && Objects.equals(target, that.target) && Objects.equals(targetFilePath, that.targetFilePath) && Objects.equals(callerClass, that.callerClass) && Objects.equals(callerMethod, that.callerMethod) && Arrays.equals(nonJdkCallerClass, that.nonJdkCallerClass) && Arrays.equals(stacktrace, that.stacktrace) && Objects.equals(context, that.context) && Arrays.equals(currClassBytecode, that.currClassBytecode) && Objects.equals(classSymbol, that.classSymbol) && Objects.equals(sourceInformation, that.sourceInformation) && Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(locationExternalForm, target, targetFilePath, callerClass, callerMethod, context, classSymbol, sourceInformation, lines);
        result = 31 * result + Arrays.hashCode(nonJdkCallerClass);
        result = 31 * result + Arrays.hashCode(stacktrace);
        result = 31 * result + Arrays.hashCode(currClassBytecode);
        return result;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "locationExternalForm='" + locationExternalForm + '\'' +
                ", target=" + target +
                ", targetFilePath='" + targetFilePath + '\'' +
                ", callerClass=" + callerClass +
                ", callerMethod='" + callerMethod + '\'' +
                ", nonJdkCallerClass=" + Arrays.toString(nonJdkCallerClass) +
                ", stacktrace=" + Arrays.toString(stacktrace) +
                ", context=" + context +
                ", currClassBytecode=" + Arrays.toString(currClassBytecode) +
                ", classSymbol=" + classSymbol +
                ", sourceInformation=" + sourceInformation +
                ", lines=" + lines +
                '}';
    }
}
