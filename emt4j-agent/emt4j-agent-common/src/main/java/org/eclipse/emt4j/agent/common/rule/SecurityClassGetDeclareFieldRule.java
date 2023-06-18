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
import org.eclipse.emt4j.agent.common.rule.getdeclarefield.GetDeclareFieldVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.FileUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Some fields cannot be reflected in JDK17.
 * Intercept call to java/lang/Class.getField and java/lang/Class.getDeclaredField method,
 * if the current class is on the white list, it may have problems.
 */
@RuleImpl(type = "get-declare-field", priority = 1)
public class SecurityClassGetDeclareFieldRule extends ExecutableRule {

    private String classListFile;
    private Set<String> securityClassSet = new HashSet<>();

    private static final MethodDesc[] callMethods = new MethodDesc[]{
            new MethodDesc("java/lang/Class", "java.lang.Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;"),
            new MethodDesc("java/lang/Class", "java.lang.Class", "getField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;"),
            new MethodDesc("java/lang/Class", "java.lang.Class", "getDeclaredFields", "()[Ljava/lang/reflect/Field;"),
            new MethodDesc("java/lang/Class", "java.lang.Class", "getFields", "()[Ljava/lang/reflect/Field;")};

    public SecurityClassGetDeclareFieldRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        securityClassSet.addAll(FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + classListFile, false));
        for (MethodDesc methodQuad : callMethods) {
            TransformerFactory.register(methodQuad, (mvp) -> new GetDeclareFieldVisitor(mvp));
        }
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (Stream.of(callMethods).noneMatch((m) -> m.getMethodIdentifierNoDesc().equals(dependency.getTarget().asMethod().toMethodIdentifierNoDesc()))) {
            return CheckResult.PASS;
        } else {
            if (dependency.getContext() != null) {
                Object thisObject = dependency.getContext().get("thisObject");
                if (thisObject instanceof Class) {
                    String className = ((Class<?>) thisObject).getName();
                    if (securityClassSet.contains(className)) {
                        return CheckResult.fail("className", className);
                    }
                }
            }
            return CheckResult.PASS;
        }
    }

    public String getClassListFile() {
        return classListFile;
    }

    public void setClassListFile(String classListFile) {
        this.classListFile = classListFile;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
