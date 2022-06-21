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
import org.eclipse.emt4j.common.MethodDesc;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Provide a factory that registers and queries who and how to transform.
 */
public class TransformerFactory {
    /**
     * class set should be transformed.
     */
    private static Set<String> needTransformClassSet = new HashSet<>();
    /**
     * internal form of class that should be transformed.
     */
    private static Set<String> needTransformClassInternalSet = new HashSet<>();

    /**
     * Get the ASM <code>MethodVisitor</code> by classname+methodName+desc
     */
    private static Map<String, Function<MethodVisitorParam, MethodVisitor>> methodToVisitorFactory = new HashMap<>();

    /**
     * register a method with MethodVisitor.
     * This method must be called before call <code>org.eclipse.emt4j.agent.common.InspectTransformer</code>
     *
     * @param methodQuad
     * @param methodVisitorFactory
     */
    public static void register(MethodDesc methodQuad, Function<MethodVisitorParam, MethodVisitor> methodVisitorFactory) {
        needTransformClassSet.add(methodQuad.getClassName());
        needTransformClassInternalSet.add(methodQuad.getInternalClassName());
        methodToVisitorFactory.put(key(methodQuad.getInternalClassName(), methodQuad.getMethodName(), methodQuad.getDesc()), methodVisitorFactory);
    }

    /**
     * test if this class may be transformed.
     *
     * @param className
     * @return
     */
    public static boolean needTransform(String className) {
        return needTransformClassSet.contains(className);
    }

    /**
     * test if this class may be transformed.
     *
     * @param className
     * @return
     */
    public static boolean needTransformByInternalClassName(String className) {
        return needTransformClassInternalSet.contains(className);
    }

    /**
     * Get the <i>ASM</i> <code>MethodVisitor</code> by className+methodName+desc
     *
     * @param internalClassName
     * @param methodName
     * @param desc
     * @return
     */
    public static Function<MethodVisitorParam, MethodVisitor> getMethodVisitor(String internalClassName, String methodName, String desc) {
        return methodToVisitorFactory.get(key(internalClassName, methodName, desc));
    }

    private static String key(String internalClassName, String methodName, String desc) {
        return internalClassName + "_" + methodName + "_" + desc;
    }
}
