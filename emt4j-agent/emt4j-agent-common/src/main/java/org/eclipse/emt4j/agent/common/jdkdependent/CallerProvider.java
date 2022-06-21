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
package org.eclipse.emt4j.agent.common.jdkdependent;

import java.util.Optional;

/**
 * Provide caller info by different ways.
 * Get caller is depend on JDK,so there exist JDK dependent implementation.
 */
public interface CallerProvider {

    /**
     * Guess the nontrivial caller.
     *
     * @return
     */
    Optional<GuessCallerInfo> guessCallers(int maxCallerNum);

    /**
     * Get caller by depth
     *
     * @param callerDepth
     * @return
     */
    Optional<Class> getNonJdkCallerByDepth(int callerDepth);

    /**
     * fill caller info with all necessary information.include caller and callee method.
     *
     * @param callerClass
     * @param from
     * @return
     */
    Optional<CallerInfo> getCallerInfo(Class callerClass, int from);
}
