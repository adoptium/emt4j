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

import org.eclipse.emt4j.common.util.ClassUtil;

import java.util.Objects;

/**
 * Describe a given method in string format.
 */
public class MethodDesc {
    String internalClassName;
    String className;
    String methodName;
    String desc;
    String methodIdentifier;
    String methodIdentifierNoDesc;

    public MethodDesc(String internalClassName, String className, String methodName, String desc) {
        this.internalClassName = internalClassName;
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        this.methodIdentifier = ClassUtil.buildMethodIdentifier(className, methodName, desc);
        this.methodIdentifierNoDesc = ClassUtil.buildMethodIdentifierNoDesc(className, methodName);
    }

    public String getInternalClassName() {
        return internalClassName;
    }

    public void setInternalClassName(String internalClassName) {
        this.internalClassName = internalClassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMethodIdentifier() {
        return methodIdentifier;
    }

    public String getMethodIdentifierNoDesc() {
        return methodIdentifierNoDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDesc that = (MethodDesc) o;
        return Objects.equals(internalClassName, that.internalClassName) &&
                Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(desc, that.desc) &&
                Objects.equals(methodIdentifier, that.methodIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalClassName, className, methodName, desc, methodIdentifier);
    }
}
