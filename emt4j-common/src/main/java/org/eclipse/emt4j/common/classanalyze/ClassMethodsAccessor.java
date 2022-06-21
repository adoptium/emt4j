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
package org.eclipse.emt4j.common.classanalyze;

import org.eclipse.emt4j.common.ClassSymbol;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Set;

/**
 * Bytecode visits exist in many places, so need an interface that accesses bytecode.
 */
public interface ClassMethodsAccessor {

    /**
     * For each methods in <code>methodNameList</code> of class <code>targetClass</code>,then
     * accept with <code>methodVisitor</code>
     *
     * @param targetClass
     * @param methodNameList
     * @param methodVisitor
     */
    void visitGivenMethodList(Class targetClass, List<String> methodNameList, MethodVisitor methodVisitor);

    /**
     * Get all reference classes in targetClass
     *
     * @param targetClass
     * @return
     */
    Set<String> getReferenceClassSet(Class targetClass);

    /**
     * Get all reference classes in a byte array of the class file.
     *
     * @param bytecode
     * @return
     */
    Set<String> getReferenceClassSet(byte[] bytecode);

    /**
     * Get all symbols in targetClass
     *
     * @param targetClass
     * @return
     */
    ClassSymbol getSymbolInClass(Class targetClass);

    /**
     * Get all symbols in a byte array of the class file.
     *
     * @param bytecode
     * @return
     */
    ClassSymbol getSymbolInClass(byte[] bytecode);
}
