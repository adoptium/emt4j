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
import org.eclipse.emt4j.agent.common.rule.cldr.CLDRDateFormatVisitor;
import org.eclipse.emt4j.common.*;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

import java.util.stream.Stream;

/**
 * When a process A running with JDK8,it format a string with default formatter,
 * then this process send to another process B running with JDK11.If the process B
 * use the parse with default formatter,it may have problems.Because the default formatter in JDK8 and JDK11
 * has a subtle difference.
 */
@RuleImpl(type = "cldr-date-format", priority = 1)
public class CLDRDateFormatRule extends ExecutableRule {

    private static final MethodDesc[] callMethods = new MethodDesc[]{
            new MethodDesc("java/text/SimpleDateFormat", "java.text.SimpleDateFormat", "format", "(Ljava/util/Date;Ljava/lang/StringBuffer;Ljava/text/Format$FieldDelegate;)Ljava/lang/StringBuffer;"),
            new MethodDesc("java/text/SimpleDateFormat", "java.text.SimpleDateFormat", "parse", "(Ljava/lang/String;Ljava/text/ParsePosition;)Ljava/util/Date;")};

    public CLDRDateFormatRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        for (MethodDesc methodQuad : callMethods) {
            TransformerFactory.register(methodQuad, (mvp) -> new CLDRDateFormatVisitor(mvp));
        }
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (Stream.of(callMethods).noneMatch((m) -> m.getMethodIdentifier().equals(dependency.getTarget().asMethod().toMethodIdentifier()))) {
            return CheckResult.PASS;
        } else {
            return CheckResult.FAIL;
        }
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD == dependency.getDependType();
    }
}
