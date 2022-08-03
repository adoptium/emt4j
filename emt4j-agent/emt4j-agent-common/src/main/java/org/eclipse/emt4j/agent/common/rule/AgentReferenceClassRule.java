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

import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.classanalyze.ClassInspectorInstance;
import org.eclipse.emt4j.common.rule.impl.ReferenceClassRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.ClassURL;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.emt4j.common.util.JdkClassUtil.isJdkClass;

/**
 * Resolved classes.
 */
@RuleImpl(type = "reference-class", priority = 1)
public class AgentReferenceClassRule extends ReferenceClassRule {
    private String[] omitClassPrefix = new String[]{"sun.reflect.GeneratedMethodAccessor",
            "sun.reflect.GeneratedConstructorAccessor",
            "sun.reflect.GeneratedSerializationConstructorAccessor"
    };

    public AgentReferenceClassRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (shouldOmit(dependency.getTarget().asClass().getClassName())) {
            return CheckResult.PASS;
        }
        return super.check(dependency);
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.CLASS == dependency.getDependType()
                || DependType.METHOD_TO_CLASS_DEEP_REFLECTION == dependency.getDependType();
    }

    private boolean shouldOmit(String normalized) {
        for (String classPrefix : omitClassPrefix) {
            if (normalized.startsWith(classPrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If there is a method named foo use a removed class in a later JDK version, but the method is not called
     * when running agent. The removed class cannot be detected.
     * For finding more potential problems, we should find such cases.
     * So we read the byte code, then analyze the non-public JDK class.
     *
     * @param dependency
     */
    @Override
    public List<Dependency> propagate(Dependency dependency) {
        //only non-jdk class we need analysis reference class in dependency
        if (!isJdkClass(dependency.getTarget().asClass().getClassName()) && dependency.getCurrClassBytecode() != null) {
            Set<String> classSet = ClassInspectorInstance.getInstance().getReferenceClassSet(dependency.getCurrClassBytecode());
            classSet.retainAll(classPackageSet);
            return classSet.stream().map((c) -> new Dependency(ClassURL.create(dependency.getLocationExternalForm(), dependency.getTarget().asClass().getClassName(), null),
                    new DependTarget.Class(c, DependType.CLASS), null,dependency.getTargetFilePath())).collect(Collectors.toList());
        } else {
            return super.propagate(dependency);
        }
    }
}
