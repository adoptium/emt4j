/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.emt4j.common.staticanalysis.impl;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JVirtualInvokeExpr;

import java.util.List;

public class CLDRCalendarFirstDayOfWeekAnalyzer extends BaseAnalyzer {
    @Override
    public String rule() {
        return "cldr-calendar-getfirstdayofweek";
    }

    boolean doAnalyze(SootMethod method) {
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();

        for (Unit unit : body.getUnits()) {
            List<ValueBox> boxes = unit.getUseBoxes();
            for (ValueBox box : boxes) {
                Value value = box.getValue();
                if (value instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr invoke = (JVirtualInvokeExpr) value;
                    SootMethod invokeMethod = invoke.getMethod();
                    if (invokeMethod.getDeclaringClass().getName().equals("java.util.Calendar")
                            && invokeMethod.getName().equals("getFirstDayOfWeek")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
