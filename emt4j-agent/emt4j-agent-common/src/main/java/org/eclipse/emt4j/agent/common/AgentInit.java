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
package org.eclipse.emt4j.agent.common;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

public class AgentInit {
    public static void init(String agentArgs, Instrumentation inst, JdkDependConfig jdkDependConfig) {
        try {
            AgentFacade.init(agentArgs, jdkDependConfig);
            AgentFacade.record(DependencyBuilder.buildJvmOption(ManagementFactory.getRuntimeMXBean().getInputArguments()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        inst.addTransformer(new InspectTransformer(), inst.isRetransformClassesSupported());
    }
}
