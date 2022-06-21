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
package org.eclipse.emt4j.common.rule.impl;

import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.classanalyze.ClassInspectorInstance;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.FileUtil;
import org.eclipse.emt4j.common.util.ClassUtil;

import java.util.*;

/**
 * When referring to a class, this rule will be called.
 */
@RuleImpl(type = "reference-class")
public class ReferenceClassRule extends ExecutableRule {

    /**
     * class or package white list
     */
    protected Set<String> classPackageSet = new HashSet<>();
    private String classPackageFile;

    /**
     * if true, check if the caller's class's bytecode reference this class.
     * Set to true can avoid some noise
     */
    private String mustContainInBytecode;

    /**
     * Can be "by-package" and "by-class".
     */
    private String matchType;


    public ReferenceClassRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        try {
            classPackageSet.addAll(FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + classPackageFile, false));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public CheckResult check(Dependency dependency) {
        Optional<String> matchPart = Optional.empty();
        String className = dependency.getTarget().asClass().getClassName();
        if ("by-package".equals(matchType)) {
            matchPart = ClassUtil.maxLongMatch(className, classPackageSet);
        } else if ("by-class".equals(matchType)) {
            if (classPackageSet.contains(className)) {
                matchPart = Optional.of(className);
            }
        } else {
            throw new RuntimeException("Unknown match-type: " + matchType);
        }

        if (matchPart.isPresent()) {
            if ("true".equals(mustContainInBytecode)
                    && !containInBytecode(dependency.getNonJdkCallerClass(), className)) {
                return CheckResult.PASS;
            } else {
                return CheckResult.fail();
            }
        } else {
            return CheckResult.PASS;
        }
    }

    private boolean containInBytecode(Class[] callerClass, String dependency) {
        if (callerClass != null) {
            //if the 'dependency' exist in anyone of possible caller
            //consider as contain in bytecode.
            for (int i = 0; i < callerClass.length; i++) {
                if (callerClass[i] != null) {
                    if (ClassInspectorInstance.getInstance().getReferenceClassSet(callerClass[i]).contains(dependency)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.CLASS == dependency.getDependType()
                || DependType.METHOD_TO_CLASS_DEEP_REFLECTION == dependency.getDependType();
    }

    public void setClassPackageFile(String classPackageFile) {
        this.classPackageFile = classPackageFile;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public void setMustContainInBytecode(String mustContainInBytecode) {
        this.mustContainInBytecode = mustContainInBytecode;
    }
}
