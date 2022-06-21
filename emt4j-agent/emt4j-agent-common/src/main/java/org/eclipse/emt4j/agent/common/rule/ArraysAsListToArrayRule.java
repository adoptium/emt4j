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
import org.eclipse.emt4j.agent.common.rule.arrayslistoarray.ToArrayMethodVisitor;
import org.eclipse.emt4j.agent.common.rule.arrayslistoarray.ArraysToArrayVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

import java.util.Collections;

/**
 * <p>
 * java.util.Arrays$ArrayList.toArray should return Object array,but there is a bug in JDK8.
 * </p>
 */
@RuleImpl(type = "arrays-as-list-to-array", priority = 1)
public class ArraysAsListToArrayRule extends ExecutableRule {
    private static final MethodDesc callMethod = new MethodDesc("java/util/Arrays$ArrayList", "java.util.Arrays$ArrayList", "toArray", "()[Ljava/lang/Object;");

    public ArraysAsListToArrayRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        TransformerFactory.register(callMethod, (mvp) -> new ArraysToArrayVisitor(mvp));
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (dependency.getTarget().asMethod().toMethodIdentifier().equals(callMethod.getMethodIdentifier())) {
            if (dependency.getCallerClass() != null && dependency.getCallerMethod() != null) {
                // if the caller's bytecode not contain CHECKCAST Object[] instruction
                // it may be a problem.
                ToArrayMethodVisitor visitor = new ToArrayMethodVisitor();
                ClassInspectorInstance.getInstance().visitGivenMethodList(dependency.getCallerClass(),
                        Collections.singletonList(dependency.getCallerMethod()), visitor);
                return visitor.isIncompatible() ? CheckResult.FAIL : CheckResult.PASS;
            }
            return CheckResult.FAIL;
        } else {
            return CheckResult.PASS;
        }
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
