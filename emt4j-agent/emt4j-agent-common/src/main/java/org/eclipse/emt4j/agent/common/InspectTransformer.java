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

import org.eclipse.emt4j.agent.common.methodvisitor.TransformerFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Transform a class if need.
 * Also record touched classes and touched jars.
 */
public class InspectTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //anonymous class ?
        if (className == null || className.startsWith(Constant.AGENT_PACKAGE_INNER) || className.startsWith(Constant.COMMON_PACKAGE_INNER)) {
            return null;
        }
        try {
            if (protectionDomain != null && protectionDomain.getCodeSource() != null && protectionDomain.getCodeSource().getLocation() != null) {
                AgentFacade.recordLoadJar(protectionDomain);
            }

            AgentFacade.recordLoadClass(className.replaceAll("/", "."), protectionDomain, classfileBuffer);
            if (TransformerFactory.needTransformByInternalClassName(className)) {
                return getInstrumentBytes(loader, classfileBuffer, className);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }

        return null;
    }

    private byte[] getInstrumentBytes(ClassLoader loader, byte[] originByteCode, String className) {
        try {
            ClassReader cr = new ClassReader(originByteCode);
            ClassWriter cw = new ClassWriter(cr, 0);
            InspectClassVisitor cv = new InspectClassVisitor(cw, className);
            cr.accept(cv, 0);
            return cw.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
