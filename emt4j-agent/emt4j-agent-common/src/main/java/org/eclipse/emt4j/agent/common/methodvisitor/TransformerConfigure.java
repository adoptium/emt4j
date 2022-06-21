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
package org.eclipse.emt4j.agent.common.methodvisitor;

import org.eclipse.emt4j.agent.common.MethodVisitorParam;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Function;

/**
 * Define who(className+methodName+desc) and how(methodVisitorFactory) to transform by ASM.
 */
public class TransformerConfigure {
    String internalClassName;
    String className;
    String methodName;
    String desc;
    Function<MethodVisitorParam, MethodVisitor> methodVisitorFactory;

    public TransformerConfigure(String internalClassName, String className, String methodName, String desc, Function<MethodVisitorParam, MethodVisitor> methodVisitorFactory) {
        this.internalClassName = internalClassName;
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        this.methodVisitorFactory = methodVisitorFactory;
    }
}
