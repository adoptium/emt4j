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
import org.eclipse.emt4j.agent.common.rule.patterncompile.PatternCompileMethodVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

/**
 * The following code will throw exception in JDK11,but not in JDK8
 * <pre>
 *     <code>
 *      Pattern p = Pattern.compile("^x", 12313131);
 *     </code>
 * </pre>
 */
@RuleImpl(type = "pattern-compile", priority = 1)
public class PatternCompileRule extends ExecutableRule {

    private static final MethodDesc callMethod = new MethodDesc("java/util/regex/Pattern", "java.util.regex.Pattern", "compile", "(Ljava/lang/String;I)Ljava/util/regex/Pattern;");

    public PatternCompileRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        TransformerFactory.register(callMethod, (mvp) -> new PatternCompileMethodVisitor(mvp));
    }

    @Override
    public CheckResult check(Dependency dependency) {
        return callMethod.getMethodIdentifier().equals(dependency.getTarget().asMethod().toMethodIdentifier()) ? CheckResult.FAIL : CheckResult.PASS;
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
