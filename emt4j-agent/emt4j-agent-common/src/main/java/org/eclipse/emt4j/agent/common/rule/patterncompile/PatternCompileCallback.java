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
package org.eclipse.emt4j.agent.common.rule.patterncompile;

import org.eclipse.emt4j.agent.common.AgentFacade;
import org.eclipse.emt4j.agent.common.InstrumentCodeCallback;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.agent.common.methodvisitor.BaseEnterCallback;
import org.eclipse.emt4j.agent.common.Constant;
import org.eclipse.emt4j.agent.common.DependencyBuilder;

import java.util.Optional;

import static org.eclipse.emt4j.agent.common.DependencyBuilder.buildMethod;

@InstrumentCodeCallback
public class PatternCompileCallback extends BaseEnterCallback {

    public static void checkPatternCompile(int flags) {
        if ((flags & ~ALL_FLAGS) != 0) {
            Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
            if (callerClass.isPresent()) {
                Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
                if (callerInfo.isPresent()) {
                    AgentFacade.record(DependencyBuilder.buildMethod(callerInfo.get(), callerInfo.get().getCalleeClass(), callerInfo.get().getCalleeMethod()));
                }
            }
        }
    }

    private static final int UNIX_LINES = 0x01;
    private static final int CASE_INSENSITIVE = 0x02;
    private static final int COMMENTS = 0x04;
    private static final int MULTILINE = 0x08;
    private static final int LITERAL = 0x10;
    private static final int DOTALL = 0x20;
    private static final int UNICODE_CASE = 0x40;
    private static final int CANON_EQ = 0x80;
    private static final int UNICODE_CHARACTER_CLASS = 0x100;
    private static final int ALL_FLAGS = CASE_INSENSITIVE | MULTILINE |
            DOTALL | UNICODE_CASE | CANON_EQ | UNIX_LINES | LITERAL |
            UNICODE_CHARACTER_CLASS | COMMENTS;
}
