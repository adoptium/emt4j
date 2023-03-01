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

import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

import java.util.List;
import java.util.Set;

public class CLDRNumberFormatAnalyzer extends BaseAnalyzer {
    @Override
    public String rule() {
        return "cldr-number-format";
    }

    boolean doAnalyze(SootMethod method) {
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        ExceptionalUnitGraph graph = ExceptionalUnitGraphFactory.createExceptionalUnitGraph(body);

        SimpleLocalDefs localDefs = new SimpleLocalDefs(graph);
        for (Unit unit : body.getUnits()) {
            List<ValueBox> boxes = unit.getUseBoxes();
            for (ValueBox box : boxes) {
                Value value = box.getValue();
                if (value instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr invoke = (JVirtualInvokeExpr) value;
                    SootMethod invokeMethod = invoke.getMethod();
                    String className = invokeMethod.getDeclaringClass().getName();
                    if ((className.equals("java.text.NumberFormat") || className.equals("java.text.DecimalFormat") || className.equals("java.text.Format"))
                            && (invokeMethod.getName().equals("format") || invokeMethod.getName().equals("parse"))
                            && invokeMethod.getParameterCount() > 0) {
                        List<ValueBox> useBoxes = invoke.getUseBoxes();
                        Local local = (Local) useBoxes.get(0).getValue();
                        if (isTarget(localDefs, unit, local, graph)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isTarget(SimpleLocalDefs localDefs, Unit unit, Local local, UnitGraph unitGraph) {
        Set<Unit> units = getDefUnits(localDefs, unit, local);
        for (Unit unit1 : units) {
            SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
            List<UnitValueBoxPair> pairList = localUses.getUsesOf(unit1);
            for (UnitValueBoxPair pair : pairList) {
                List<ValueBox> boxes = pair.unit.getUseBoxes();
                for (ValueBox box : boxes) {
                    Value value = box.getValue();
                    if (value instanceof JVirtualInvokeExpr) {
                        JVirtualInvokeExpr invoke = (JVirtualInvokeExpr) value;
                        SootMethod invokeMethod = invoke.getMethod();
                        String className = invokeMethod.getDeclaringClass().getName();
                        if ((className.equals("java.text.NumberFormat") ||
                                className.equals("java.text.DecimalFormat") ||
                                className.equals("java.text.Format"))
                                && invokeMethod.getName().equals("setCurrency")
                                && invokeMethod.getParameterCount() > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
