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

import java.util.Map;
import java.util.Set;

/**
 * Symbols that used by a class.
 * This class not include all symbol of a classes.Only some we need.
 */
public class ClassSymbol {
    private Set<String> typeSet;
    private Set<DependTarget.Method> callMethodSet;
    private Set<String> constantPoolSet;
    private Map<String, Set<String>> invokeMap;
    private String className; // Internal class name

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Set<String> getTypeSet() {
        return typeSet;
    }

    public void setTypeSet(Set<String> typeSet) {
        this.typeSet = typeSet;
    }

    public Set<DependTarget.Method> getCallMethodSet() {
        return callMethodSet;
    }

    public void setCallMethodSet(Set<DependTarget.Method> callMethodSet) {
        this.callMethodSet = callMethodSet;
    }

    public Set<String> getConstantPoolSet() {
        return constantPoolSet;
    }

    public void setConstantPoolSet(Set<String> constantPoolSet) {
        this.constantPoolSet = constantPoolSet;
    }

    public Map<String, Set<String>> getInvokeMap() {
        return invokeMap;
    }

    public void setInvokeMap(Map<String, Set<String>> invokeMap) {
        this.invokeMap = invokeMap;
    }
}
