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
package org.eclipse.emt4j.test.common;

import java.util.Map;

/**
 * Provide more runtime information before running a test case.
 * Include which jdk will running the testcase, class path for agent ,class path for analysis.
 */
public class RunningTestParam {
    String agentVersion;
    Map<String, String> jdkVersionToHome;
    String testCommonClassPath;
    String agentLibDir;
    String analysisLibDir;
}
