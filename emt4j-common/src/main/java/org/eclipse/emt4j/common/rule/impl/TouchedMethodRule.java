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

import java.util.ArrayList;
import java.util.List;


/**
 * When reference to a method in a method list.
 */
@RuleImpl(type = "touched-method")
public class TouchedMethodRule extends ExecutableRule {
    private String methodListFile;
    protected List<MethodDesc> callMethods;

    public String getMethodListFile() {
        return methodListFile;
    }

    public void setMethodListFile(String methodListFile) {
        this.methodListFile = methodListFile;
    }

    public TouchedMethodRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        List<String> lines = FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + methodListFile, false);
        callMethods = new ArrayList<>(lines.size());
        for (String line : lines) {
            String className = line.substring(0, line.indexOf('.'));
            String methodName = line.substring(line.indexOf('.') + 1, line.indexOf('('));
            String desc = line.substring(line.indexOf('('));
            callMethods.add(new MethodDesc(className, className.replace('/', '.'), methodName, desc));
        }
    }

    @Override
    public CheckResult check(Dependency dependency) {
        return callMethods.stream().noneMatch((m) -> m.getMethodIdentifierNoDesc().equals(dependency.getTarget().asMethod().toMethodIdentifierNoDesc()))
                ? CheckResult.PASS : CheckResult.FAIL;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
