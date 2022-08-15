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
import org.eclipse.emt4j.agent.common.rule.cldr.CLDRNumberFormatVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

import java.util.stream.Stream;

/**
 * <pre>
 *     <code>
 *         BigDecimal value = new BigDecimal("1.99");
 * Locale locale = new Locale("zh_CN");
 * for (String currencyCode :new String[]{"CNY","EUR","USD"} ) {
 *     NumberFormat format = NumberFormat.getCurrencyInstance(locale);
 *     Currency currency = Currency.getInstance(currencyCode);
 *     format.setCurrency(currency);
 *     String result = format.format(value);
 *     System.out.println("Result: " + result);
 * }
 *     </code>
 *     When running in JDK8,it print:
 *     <pre>
 * Result: CNY 1.99
 * Result: EUR 1.99
 * Result: USD 1.99
 *     </pre>
 *     ,but running in JDK11,it print
 *     <pre>
 * Result: CN¥ 1.99
 * Result: € 1.99
 * Result: US$ 1.99
 *     </pre>
 * </pre>
 * <p>
 * It a process A running in JDK8,send result to a process running JDK11,it may contain potential problem when parse it.
 */
@RuleImpl(type = "cldr-number-format", priority = 1)
public class CLDRNumberFormatRule extends ExecutableRule {

    private static final MethodDesc[] callMethods = new MethodDesc[]{
            new MethodDesc("java/text/DecimalFormat", "java.text.DecimalFormat", "format", "(DLjava/lang/StringBuffer;Ljava/text/Format$FieldDelegate;)Ljava/lang/StringBuffer;"),
            new MethodDesc("java/text/DecimalFormat", "java.text.DecimalFormat", "format", "(JLjava/lang/StringBuffer;Ljava/text/Format$FieldDelegate;)Ljava/lang/StringBuffer;"),
            new MethodDesc("java/text/DecimalFormat", "java.text.DecimalFormat", "format", "(Ljava/math/BigDecimal;Ljava/lang/StringBuffer;Ljava/text/Format$FieldDelegate;)Ljava/lang/StringBuffer;"),
            new MethodDesc("java/text/DecimalFormat", "java.text.DecimalFormat", "format", "(Ljava/math/BigInteger;Ljava/lang/StringBuffer;Ljava/text/Format$FieldDelegate;Z)Ljava/lang/StringBuffer;"),
            new MethodDesc("java/text/DecimalFormat", "java.text.DecimalFormat", "parse", "(Ljava/lang/String;Ljava/text/ParsePosition;)Ljava/lang/Number;")};

    public CLDRNumberFormatRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        for (MethodDesc methodQuad : callMethods) {
            TransformerFactory.register(methodQuad, (mvp) -> new CLDRNumberFormatVisitor(mvp));
        }
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (Stream.of(callMethods).noneMatch((m) -> m.getMethodIdentifierNoDesc().equals(dependency.getTarget().asMethod().toMethodIdentifierNoDesc()))) {
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
