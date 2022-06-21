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

public class Constant {
    public static final String AGENT_PACKAGE_INNER = "org/eclipse/emt4j/agent";
    public static final String COMMON_PACKAGE_INNER = "org/eclipse/emt4j/common";
    public static final String AGENT_PACKAGE = "org.eclipse.emt4j.agent";
    public static final String COMMON_PACKAGE = "org.eclipse.emt4j.common";
    public static final String INIT_CLASS = "org.eclipse.emt4j.agent.common.AgentInit";
    public static final int FIRST_NON_AGENT_CALLER_INDEX = 4;
    public static final int CALLEE_INDEX = FIRST_NON_AGENT_CALLER_INDEX - 1;
    public static final String AGENT_REPORT_WRITE_THREAD = "AgentReportWriteThread";
}
