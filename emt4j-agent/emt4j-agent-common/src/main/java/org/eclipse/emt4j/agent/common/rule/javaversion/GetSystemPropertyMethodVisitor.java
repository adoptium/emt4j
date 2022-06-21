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
package org.eclipse.emt4j.agent.common.rule.javaversion;

import org.eclipse.emt4j.agent.common.MethodVisitorParam;
import org.eclipse.emt4j.agent.common.methodvisitor.AbstractAgentMethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class GetSystemPropertyMethodVisitor extends AbstractAgentMethodVisitor {
    public GetSystemPropertyMethodVisitor(MethodVisitorParam mvp) {
        super(mvp);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }

    @Override
    public void visitCode() {
        mv.visitCode();
        // static method and the first parameter,so the var is 0
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "org/eclipse/emt4j/agent/common/rule/javaversion/GetJavaVersionCallback",
                "check",
                "(Ljava/lang/String;)V", false);
    }
}
