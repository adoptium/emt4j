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

import org.objectweb.asm.MethodVisitor;

/**
 * all a ASM MethodVisitor need
 */
public class MethodVisitorParam {

    private MethodVisitor methodVisitor;

    private String className;

    private String method;

    private String desc;

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public String getClassName() {
        return className;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public MethodVisitorParam(MethodVisitor methodVisitor, String className, String method, String desc) {
        this.methodVisitor = methodVisitor;
        this.className = className;
        this.method = method;
        this.desc = desc;
    }
}
