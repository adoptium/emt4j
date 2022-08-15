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
package org.eclipse.emt4j.common.rule;

import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;
import org.mvel2.templates.TemplateRuntime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <code>ExecutableRule</code> is the core of jdk migration tool.
 * It abstract each incompatible issue to a rule.The rule defined in rule.xml,then convert it
 * <p>
 * to a ExecutableRule when runtime.
 */
public abstract class ExecutableRule {
    protected final ConfRuleItem confRuleItem;
    protected final ConfRules confRules;

    public ExecutableRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        this.confRuleItem = confRuleItem;
        this.confRules = confRules;
    }

    /**
     * initialization
     */
    public abstract void init();

    /**
     * After executing this rule, it may find more dependencies.
     * For example, when a class A resolved. The classes reference in class A can also be found.
     *
     * @param dependency
     * @return
     */
    public List<Dependency> propagate(Dependency dependency) {
        return Collections.emptyList();
    }

    /**
     * Check the dependency, then convert it to ReportCheckResult.
     *
     * @param dependency
     * @return
     */
    public ReportCheckResult execute(Dependency dependency) {
        CheckResult checkResult = check(dependency);
        if (checkResult.isPass()) {
            return ReportCheckResult.PASS;
        } else {
            ReportCheckResult reportCheckResult = new ReportCheckResult(false);
            reportCheckResult.setPriority(confRuleItem.getPriority());
            reportCheckResult.setContext(checkResult.getContext());
            reportCheckResult.setResultCode(evalIfNeed(confRuleItem.getResultCode(), checkResult.getContext()));
            reportCheckResult.setSubResultCode(evalIfNeed(confRuleItem.getSubResultCode(), checkResult.getContext()));
            reportCheckResult.setPropagated(checkResult.getPropagated());
            return reportCheckResult;
        }
    }

    private String evalIfNeed(String maybeMvel2Expr, Map<String, Object> context) {
        if (null == maybeMvel2Expr || "".equals(maybeMvel2Expr) || null == context || context.isEmpty()) {
            return maybeMvel2Expr;
        } else {
            return (String) TemplateRuntime.eval(maybeMvel2Expr, context);
        }
    }

    protected abstract CheckResult check(Dependency dependency);

    public abstract boolean accept(Dependency dependency);

    public ConfRuleItem getConfRuleItem() {
        return confRuleItem;
    }

    public ConfRules getConfRules() {
        return confRules;
    }
}
