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
import org.eclipse.emt4j.agent.common.rule.touchmethod.MethodEnterVisitor;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.impl.TouchedMethodRule;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

/**
 * A method was called after transformed.
 */
@RuleImpl(type = "touched-method", priority = 1)
public class AgentTouchedMethodRule extends TouchedMethodRule {
    public AgentTouchedMethodRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        super.init();
        for (MethodDesc methodQuad : super.callMethods) {
            TransformerFactory.register(methodQuad, (mvp) -> new MethodEnterVisitor(mvp));
        }
    }
}
