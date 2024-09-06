/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * When reference to a method in a method list.
 * Compared to TouchedMethodRule, NameBasedTouchedMethodRule only checks method names,
 * not method descriptions, and uses regular expressions for matching.
 */
@RuleImpl(type = "name-based-touched-method")
public class NameBasedTouchedMethodRule extends ExecutableRule {
    private String methodListFile;
    protected List<Pattern> callMethods;

    public String getMethodListFile() {
        return methodListFile;
    }

    public void setMethodListFile(String methodListFile) {
        this.methodListFile = methodListFile;
    }

    public NameBasedTouchedMethodRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        List<String> lines = FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + methodListFile, false);
        callMethods = lines.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    @Override
    public CheckResult check(Dependency dependency) {
        DependTarget.Method method = dependency.getTarget().asMethod();
        String name = method.getClassName() + "." + method.getMethodName();
        return callMethods.stream().noneMatch(pattern -> pattern.matcher(name).matches()) ? CheckResult.PASS : CheckResult.FAIL;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
