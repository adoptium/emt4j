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
package org.eclipse.emt4j.agent.common.rule.cldr;

import org.eclipse.emt4j.agent.common.AgentFacade;
import org.eclipse.emt4j.agent.common.InstrumentCodeCallback;
import org.eclipse.emt4j.agent.common.jdkdependent.CallerInfo;
import org.eclipse.emt4j.agent.common.methodvisitor.BaseEnterCallback;
import org.eclipse.emt4j.agent.common.Constant;
import org.eclipse.emt4j.agent.common.DependencyBuilder;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

import static org.eclipse.emt4j.agent.common.DependencyBuilder.buildMethod;

@InstrumentCodeCallback
public class CLDRSensitiveEnterCallback extends BaseEnterCallback {

    //private static final String DEFAULT_DATETIME_FMT = "MMM d, yyyy h:mm:ss a";
    private static final SimpleDateFormat enUsFormat
            = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    private static final SimpleDateFormat localFormat
            = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);


    public static void dateTimeEnter(SimpleDateFormat sdf) {
        if (isDefaultFormat(sdf)) {
            Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
            if (callerClass.isPresent()) {
                Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
                if (callerInfo.isPresent()) {
                    AgentFacade.record(DependencyBuilder.buildMethod(callerInfo.get(), callerInfo.get().getCalleeClass(), callerInfo.get().getCalleeMethod()));
                }
            }
        }
    }

    private static boolean isDefaultFormat(SimpleDateFormat sdf) {
        return sdf != null && (enUsFormat.toPattern().equals(sdf.toPattern())
                || localFormat.toPattern().equals(sdf.toPattern()));
    }

    public static void getDayOfFirstWeekEnter() {
        Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
        if (callerClass.isPresent()) {
            Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
            if (callerInfo.isPresent()) {
                AgentFacade.record(DependencyBuilder.buildMethod(callerInfo.get(), callerInfo.get().getCalleeClass(), callerInfo.get().getCalleeMethod()));
            }
        }
    }

    public static void numberFormatEnter(DecimalFormat decimalFormat) {
        // when format with currency.the suffix and prefix not empty.
        if (decimalFormat != null && !(isEmpty(decimalFormat.getPositivePrefix()) && isEmpty(decimalFormat.getPositiveSuffix()))) {
            Optional<Class> callerClass = AgentFacade.getCallerProvider().getNonJdkCallerByDepth(Constant.FIRST_NON_AGENT_CALLER_INDEX);
            if (callerClass.isPresent()) {
                Optional<CallerInfo> callerInfo = AgentFacade.getCallerProvider().getCallerInfo(callerClass.get(), Constant.CALLEE_INDEX);
                if (callerInfo.isPresent()) {
                    AgentFacade.record(DependencyBuilder.buildMethod(callerInfo.get(), callerInfo.get().getCalleeClass(), callerInfo.get().getCalleeMethod()));
                }
            }
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }
}
