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
package org.eclipse.emt4j.agent.common.rule;

import org.eclipse.emt4j.agent.common.methodvisitor.TransformerFactory;
import org.eclipse.emt4j.common.classanalyze.ClassInspectorInstance;
import org.eclipse.emt4j.agent.common.rule.systemclassloader.GetClassLoaderVisitor;
import org.eclipse.emt4j.agent.common.rule.systemclassloader.SystemClassLoaderMethodVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

import java.util.Collections;
import java.util.stream.Stream;

/**
 * In JDK8, the system classloader is a subclass of URLClassLoader, so some java code
 * cast it to URLClassLoader, but the assumption is not true in JDK11.
 * We track the code that gets the system classloader, then check if the method contains an instruction that checks cast to
 * URLCLassLoader.
 * The solution is rough, a programmer may check if it's an instance of URLCLassLoader.But the cost is too high.
 * We may need to implement a simple interpreter to simulate the program's behaviour. Actually, in the previous version, I implement a
 * interpreter, but it's too slow.
 */
@RuleImpl(type = "system-classloader-not-a-urlclassloader", priority = 1)
public class SystemClassLoaderRule extends ExecutableRule {

    private static final MethodDesc[] callMethods = new MethodDesc[]{
            new MethodDesc("java/lang/Class", "java.lang.Class", "getClassLoader", "()Ljava/lang/ClassLoader;"),
            new MethodDesc("java/lang/ClassLoader", "java.lang.ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;"),
            new MethodDesc("java/lang/ClassLoader", "java.lang.ClassLoader", "getParent", "()Ljava/lang/ClassLoader;")};


    public SystemClassLoaderRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        for (MethodDesc methodQuad : callMethods) {
            TransformerFactory.register(methodQuad, (mvp) -> new GetClassLoaderVisitor(mvp));
        }
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (Stream.of(callMethods).noneMatch((m) -> m.getMethodIdentifierNoDesc().equals(dependency.getTarget().asMethod().toMethodIdentifierNoDesc()))) {
            return CheckResult.PASS;
        }
        if (dependency.getCallerClass() != null && dependency.getCallerMethod() != null) {
            SystemClassLoaderMethodVisitor visitor = new SystemClassLoaderMethodVisitor();
            ClassInspectorInstance.getInstance().visitGivenMethodList(dependency.getCallerClass(),
                    Collections.singletonList(dependency.getCallerMethod()), visitor);
            return visitor.isIncompatible() ? CheckResult.FAIL : CheckResult.PASS;
        }

        return CheckResult.FAIL;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
