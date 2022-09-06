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
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.objectweb.asm.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM9;

/**
 * ASM implementation of ClassMethodsAccessor.
 * If for a class, read the class's bytecode first, then use ASM to parse it.
 */
public class AsmClassMethodsAccessor implements ClassMethodsAccessor {

    static AtomicReference<String> currentMethod = new AtomicReference<>();

    @Override
    public void visitGivenMethodList(Class targetClass, List<String> methodNameList, MethodVisitor methodVisitor) {
        readClass(targetClass, (b) -> {
            visit(b, methodNameList, methodVisitor);
        });
    }

    private void readClass(Class targetClass, Consumer<byte[]> consumer) {
        String name = targetClass.getName();
        // also works well for non-package classes
        name = name.substring(name.lastIndexOf(".") + 1);
        try (InputStream in = targetClass.getResourceAsStream(name + ".class")) {
            if (in != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                consumer.accept(bos.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String visit(byte[] bytecode, List<String> methodNameList, MethodVisitor methodVisitor) {
        ClassReader cr = new ClassReader(bytecode);
        cr.accept(new ClassVisitor(ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                String resultingParams = retrieveParameters(descriptor);
                String methodFullName;
                if (resultingParams.isEmpty()) {
                    methodFullName = name;
                } else {
                    methodFullName = name + "(" + resultingParams + ")";
                }
                currentMethod.set(methodFullName);
                if (null == methodNameList || methodNameList.contains(name)) {
                    return methodVisitor;
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        return cr.getClassName();
    }

    private static String retrieveParameters(String descriptor) {
        int endPos = descriptor.lastIndexOf(")");
        String params = descriptor.substring(1, endPos);
        int paraStartPos = 0;
        int paraEndPos;
        StringBuilder resultingParams = new StringBuilder();
        int dimension = 0;
        while (paraStartPos < params.length()) {
            if (params.charAt(paraStartPos) == 'L') {
                paraEndPos = params.indexOf(";", paraStartPos);
                String param = params.substring(paraStartPos + 1, paraEndPos).replace('/', '.');
                resultingParams.append(param);
                paraStartPos = paraEndPos + 1;
            } else {
                char primitiveType = params.charAt(paraStartPos++);
                switch (primitiveType) {
                    case 'Z':
                        resultingParams.append("boolean");
                        break;
                    case 'I':
                        resultingParams.append("int");
                        break;
                    case 'F':
                        resultingParams.append("float");
                        break;
                    case 'D':
                        resultingParams.append("double");
                        break;
                    case 'C':
                        resultingParams.append("char");
                        break;
                    case 'B':
                        resultingParams.append("byte");
                        break;
                    case 'J':
                        resultingParams.append("long");
                        break;
                    case 'S':
                        resultingParams.append("short");
                        break;
                    case '[':
                        dimension++;
                        continue;
                    default:
                        throw new RuntimeException("Unknown type " + primitiveType + " in parameter types descriptor " + descriptor);
                }
            }
            while (dimension > 0) {
                resultingParams.append("[]");
                dimension--;
            }
            resultingParams.append(";");
        }
        // delete the last ';'
        if (resultingParams.length() > 0) {
            resultingParams.deleteCharAt(resultingParams.length() - 1);
        }
        return resultingParams.toString();
    }

    @Override
    public Set<String> getReferenceClassSet(Class targetClass) {
        RecordSymbolMethodVisitor methodVisitor = new RecordSymbolMethodVisitor();
        readClass(targetClass, (b) -> {
            visit(b, null, methodVisitor);
        });
        return methodVisitor.getTypeSet();
    }

    @Override
    public Set<String> getReferenceClassSet(byte[] bytecode) {
        RecordSymbolMethodVisitor methodVisitor = new RecordSymbolMethodVisitor();
        visit(bytecode, null, methodVisitor);
        return methodVisitor.getTypeSet();
    }

    @Override
    public ClassSymbol getSymbolInClass(Class targetClass) {
        RecordSymbolMethodVisitor methodVisitor = new RecordSymbolMethodVisitor();
        readClass(targetClass, (b) -> {
            visit(b, null, methodVisitor);
        });
        return createClassSymbol(methodVisitor, targetClass.getName());
    }

    private static ClassSymbol createClassSymbol(RecordSymbolMethodVisitor methodVisitor, String className) {
        ClassSymbol classSymbol = new ClassSymbol();
        classSymbol.setCallMethodSet(methodVisitor.callMethodSet);
        classSymbol.setConstantPoolSet(methodVisitor.constantPoolSet);
        classSymbol.setTypeSet(methodVisitor.typeSet);
        classSymbol.setInvokeMap(methodVisitor.invokeMap);
        classSymbol.setClassName(className);
        return classSymbol;
    }

    private static class RecordSymbolMethodVisitor extends MethodVisitor {
        Set<String> typeSet = new HashSet<>();
        Set<DependTarget.Method> callMethodSet = new HashSet<>();
        Set<String> constantPoolSet = new HashSet<>();
        Map<String, Set<String>> invokeMap = new HashMap<>();

        public RecordSymbolMethodVisitor() {
            super(ASM9);
        }

        public Set<String> getTypeSet() {
            return typeSet;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            add(parseInternalForms(descriptor));
            return super.visitAnnotation(descriptor, visible);
        }

        void add(String maybeClass) {
            if (maybeClass != null) {
                typeSet.add(maybeClass);
            }
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            add(descriptor);
            return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
            add(descriptor);
            return super.visitParameterAnnotation(parameter, descriptor, visible);
        }

        @Override
        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            if (numLocal > 0 && local != null) {
                for (Object o : local) {
                    if (o instanceof String) {
                        addLocalAndStack((String) o);
                    }
                }
            }
            if (numStack > 0 && stack != null) {
                for (Object o : stack) {
                    if (o instanceof String) {
                        addLocalAndStack((String) o);
                    }
                }
            }

            super.visitFrame(type, numLocal, local, numStack, stack);
        }

        private void addLocalAndStack(String o) {
            if (o.length() > 1) {
                if ((o.charAt(0) == '[' || o.charAt(0) == 'L') && o.charAt(o.length() - 1) == ';') {
                    add(parseInternalForms(o));
                } else {
                    add(normalize(o));
                }
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            add(normalize(type));
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            add(normalize(owner));
            add(parseInternalForms(descriptor));
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            add(normalize(owner));
            add(parseMethodDescriptor(descriptor));
            //Not taking descriptor as a part of a method is deliberate.
            //in agent, we need intercept some JDK methods, when the method was called,it will call
            //out callback methods.In the callback methods, get the descriptor of the intercepted method is difficult.
            //Also, we can pass the descriptor as a parameter to the callback method,but it lead core dump at C2.
            //So we use the stack trace to get the intercepted method,but in stack trace we only get method name.
            //For simply,we omit the descriptor of method all in jdk migration tool.
            //Omit the descriptor have no problem for functional.
            DependTarget.Method dependTarget = new DependTarget.Method(normalize(owner), name, descriptor, DependType.METHOD);
            callMethodSet.add(dependTarget);
            String method = currentMethod.get();
            invokeMap.compute(method, (k, v) -> {
                        if (v == null) {
                            v = new HashSet<>();
                        }
                        v.add(dependTarget.toMethodIdentifierNoDesc());
                        return v;
                    }
            );
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        private void add(List<String> classNames) {
            for (String className : classNames) {
                add(className);
            }
        }

        private String normalize(String internal) {
            if (null != internal) {
                return internal.replace('/', '.').replace('$', '.');
            } else {
                return null;
            }
        }

        private List<String> parseMethodDescriptor(String descriptor) {
            if (null == descriptor) {
                return null;
            } else {
                String paramPart = descriptor.substring(descriptor.indexOf('(') + 1, descriptor.indexOf(')'));
                String returnPart = descriptor.substring(descriptor.indexOf(')') + 1);
                List<String> classNames = new ArrayList<>();
                classNames.addAll(parseInternalForms(paramPart));
                classNames.addAll(parseReturnPart(returnPart));
                return classNames;
            }
        }

        private Collection<? extends String> parseReturnPart(String returnPart) {
            if ("V".equals(returnPart)) {
                return Collections.emptyList();
            } else {
                return parseInternalForms(returnPart);
            }
        }

        private List<String> parseInternalForms(String internalForm) {
            List<String> classNames = new ArrayList<>();
            char[] chars = internalForm.toCharArray();

            boolean findingClass = false;
            StringBuilder classNameSb = new StringBuilder(64);
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (findingClass) {
                    if (c == ';') {
                        classNames.add(normalize(normalize(classNameSb.toString())));
                        classNameSb.setLength(0);
                        findingClass = false;
                    } else {
                        classNameSb.append(c);
                    }
                } else {
                    if (c == 'B' || c == 'C' || c == 'D' || c == 'F' || c ==
                            'I' || c == 'J' || c == 'S' || c == 'Z' || c == '[') {
                        continue;
                    } else if (c == 'L') {
                        findingClass = true;
                    } else {
                        throw new RuntimeException("Unknown descriptor: " + internalForm);
                    }
                }
            }
            return classNames;
        }

        /**
         * For array type, the dimension can > 2, but considering the normal case,
         * we only process the dimension <= 2.
         *
         * @param value
         */
        @Override
        public void visitLdcInsn(Object value) {
            if (value instanceof String) {
                add((String) value);
                constantPoolSet.add((String) value);
            } else if (value instanceof Type) {
                Type type = (Type) value;
                int sort = type.getSort();
                if (sort == Type.OBJECT) {
                    add(type.getClassName());
                    constantPoolSet.add(type.getClassName());
                } else if (sort == Type.ARRAY) {
                    Type elementType = type.getElementType();
                    if (elementType.getSort() == Type.OBJECT) {
                        add(elementType.getClassName());
                        constantPoolSet.add(elementType.getClassName());
                    } else if (elementType.getSort() == Type.ARRAY) {
                        Type elementElementType = elementType.getElementType();
                        if (elementElementType.getSort() == Type.OBJECT) {
                            add(elementElementType.getClassName());
                            constantPoolSet.add(elementElementType.getClassName());
                        }
                    }
                }
            }
            super.visitLdcInsn(value);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            add(parseInternalForms(descriptor));
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            add(parseInternalForms(descriptor));
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }
    }

    @Override
    public ClassSymbol getSymbolInClass(byte[] bytecode) {
        RecordSymbolMethodVisitor methodVisitor = new RecordSymbolMethodVisitor();
        String className = visit(bytecode, null, methodVisitor);
        return createClassSymbol(methodVisitor, className);
    }
}
