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
package org.eclipse.emt4j.agent.common.rule.systemclassloader;

import org.eclipse.emt4j.agent.common.AgentFacade;
import org.eclipse.emt4j.agent.common.InstrumentCodeCallback;
import org.eclipse.emt4j.agent.common.SystemClassLoaderIReference;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.agent.common.methodvisitor.BaseEnterCallback;
import org.eclipse.emt4j.agent.common.*;

import java.util.Optional;

import static org.eclipse.emt4j.agent.common.DependencyBuilder.buildMethod;

@InstrumentCodeCallback
public class GetSystemClassLoaderCallback extends BaseEnterCallback {

    public static void enter(ClassLoader classLoader) {
        if (classLoader != null && SystemClassLoaderIReference.isSystemClassLoader(classLoader)) {
            Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
            if (callerClass.isPresent()) {
                Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
                if (callerInfo.isPresent()) {
                    AgentFacade.record(DependencyBuilder.buildMethod(callerInfo.get(), callerInfo.get().getCalleeClass(), callerInfo.get().getCalleeMethod()));
                }
            }
        }
    }
}
