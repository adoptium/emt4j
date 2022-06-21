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
package org.eclipse.emt4j.common;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

/**
 * <code>Dependency</code> is the core model of JDK migration tool.
 * It describes what depends on what, and the type and the context surrounding the dependency.
 */
public class Dependency implements Serializable {
    private String locationExternalForm;
    private DependTarget target;

    private transient Class callerClass;
    private String callerMethod;
    private transient Class[] nonJdkCallerClass;
    private StackTraceElement[] stacktrace;
    private transient Map<String, Object> context;
    private transient byte[] currClassBytecode;
    private transient ClassSymbol classSymbol;

    public Dependency(URL location, DependTarget target, StackTraceElement[] stacktrace) {
        this.target = target;
        this.stacktrace = stacktrace;
        if (location != null) {
            locationExternalForm = location.toExternalForm();
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dependency that = (Dependency) o;

        if (locationExternalForm != null ? !locationExternalForm.equals(that.locationExternalForm) : that.locationExternalForm != null)
            return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (callerClass != null ? !callerClass.equals(that.callerClass) : that.callerClass != null) return false;
        if (callerMethod != null ? !callerMethod.equals(that.callerMethod) : that.callerMethod != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(nonJdkCallerClass, that.nonJdkCallerClass)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(stacktrace, that.stacktrace)) return false;
        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (!Arrays.equals(currClassBytecode, that.currClassBytecode)) return false;
        return classSymbol != null ? classSymbol.equals(that.classSymbol) : that.classSymbol == null;
    }

    @Override
    public int hashCode() {
        int result = locationExternalForm != null ? locationExternalForm.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (callerClass != null ? callerClass.hashCode() : 0);
        result = 31 * result + (callerMethod != null ? callerMethod.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(nonJdkCallerClass);
        result = 31 * result + Arrays.hashCode(stacktrace);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(currClassBytecode);
        result = 31 * result + (classSymbol != null ? classSymbol.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "locationExternalForm='" + locationExternalForm + '\'' +
                ", target=" + target +
                ", callerClass=" + callerClass +
                ", callerMethod='" + callerMethod + '\'' +
                ", nonJdkCallerClass=" + Arrays.toString(nonJdkCallerClass) +
                ", stacktrace=" + Arrays.toString(stacktrace) +
                ", context=" + context +
                ", currClassBytecode=" + Arrays.toString(currClassBytecode) +
                ", classSymbol=" + classSymbol +
                '}';
    }
}
