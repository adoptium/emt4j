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

import org.eclipse.emt4j.common.*;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.FileUtil;
import org.mvel2.MVEL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Some rules not only need a method or a field but require all symbols in a class.
 * Read all methods,types,constant pools, then execute the MVEL2 expression to check it.
 */
@RuleImpl(type = "whole-class")
public class WholeClassRule extends ExecutableRule {
    private String mvel2RuleFile;
    private String mvel2Rule;

    public WholeClassRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        mvel2Rule = String.join(" ", FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + mvel2RuleFile, false));
    }

    @Override
    protected CheckResult check(Dependency dependency) {
        Map<String, Object> mvelMap = new HashMap<>();
        mvelMap.put("typeSet", dependency.getClassSymbol().getTypeSet());
        mvelMap.put("methodSet", toMethodIdentifierSet(dependency.getClassSymbol().getCallMethodSet()));
        mvelMap.put("cpSet", dependency.getClassSymbol().getConstantPoolSet());
        Object result = MVEL.eval(mvel2Rule, mvelMap);
        if (result instanceof Boolean) {
            return ((Boolean) result) ? CheckResult.FAIL : CheckResult.PASS;
        } else {
            throw new JdkMigrationException("Mvel2 rule file" + mvel2RuleFile + " must return a boolean result!Now result type is : " + result.getClass());
        }
    }

    private Set<String> toMethodIdentifierSet(Set<DependTarget.Method> callMethodSet) {
        if (callMethodSet == null || callMethodSet.isEmpty()) {
            return Collections.emptySet();
        } else {
            return callMethodSet.stream().map((m) -> m.toMethodIdentifier()).collect(Collectors.toSet());
        }
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.WHOLE_CLASS == dependency.getDependType();
    }

    public void setMvel2RuleFile(String mvel2RuleFile) {
        this.mvel2RuleFile = mvel2RuleFile;
    }

    public void setMvel2Rule(String mvel2Rule) {
        this.mvel2Rule = mvel2Rule;
    }
}
