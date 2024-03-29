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
import org.eclipse.emt4j.agent.common.rule.cldr.CLDRGetFirstDayOfWeekVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

/**
 * java.util.Calendar.getFirstDayOfWeek may have problem if change default locale data to CLDR with some specific locale.
 */
@RuleImpl(type = "cldr-calendar-getfirstdayofweek", priority = 1)
public class CLDRCalendarFirstDayOfWeekRule extends ExecutableRule {

    private static final MethodDesc callMethod = new MethodDesc("java/util/Calendar", "java.util.Calendar", "getFirstDayOfWeek", "()I");

    public CLDRCalendarFirstDayOfWeekRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        TransformerFactory.register(callMethod, (mvp) -> new CLDRGetFirstDayOfWeekVisitor(mvp));
    }

    @Override
    public CheckResult check(Dependency dependency) {
        return callMethod.getMethodIdentifierNoDesc().equals(dependency.getTarget().asMethod().toMethodIdentifierNoDesc()) ? CheckResult.FAIL : CheckResult.PASS;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
