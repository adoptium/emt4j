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

import org.eclipse.emt4j.common.staticanalysis.Analyzer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.CastExpr;
import soot.jimple.JimpleBody;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

abstract class BaseAnalyzer implements Analyzer {
    @Override
    public boolean analyze(SootClass clazz) {
        List<SootMethod> methods = clazz.getMethods();
        for (int i = 0; i < methods.size(); i++) {
            SootMethod method = methods.get(i);
            if (!method.isAbstract() && !method.isNative()) {
                if (doAnalyze(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    abstract boolean doAnalyze(SootMethod method);

    protected final static Set<Value> getDefValues(SimpleLocalDefs localDefs, Unit unit, Local local) {
        Set<Value> values = new HashSet<>();
        List<Unit> defs = localDefs.getDefsOfAt(local, unit);
        for (Unit def : defs) {
            if (def instanceof JAssignStmt) {
                JAssignStmt assign = (JAssignStmt) def;
                Value rightOp = assign.getRightOp();
                if (rightOp instanceof Local) {
                    values.addAll(getDefValues(localDefs, def, (Local) rightOp));
                } else if (rightOp instanceof CastExpr) {
                    values.addAll(getDefValues(localDefs, def, (Local) ((CastExpr) rightOp).getOp()));
                } else if (rightOp instanceof StaticFieldRef) {
                    StaticFieldRef ref = (StaticFieldRef) rightOp;
                    values.addAll(globalTarget(ref));
                } else {
                    values.add(rightOp);
                }
            }
        }
        return values;
    }

    protected final static Set<Unit> getDefUnits(SimpleLocalDefs localDefs, Unit unit, Local local) {
        Set<Unit> units = new HashSet<>();
        List<Unit> defs = localDefs.getDefsOfAt(local, unit);
        for (Unit def : defs) {
            units.add(def);
            if (def instanceof JAssignStmt) {
                JAssignStmt assign = (JAssignStmt) def;
                Value rightOp = assign.getRightOp();
                if (rightOp instanceof Local) {
                    units.addAll(getDefUnits(localDefs, def, (Local) rightOp));
                } else if (rightOp instanceof CastExpr) {
                    units.addAll(getDefUnits(localDefs, def, (Local) ((CastExpr) rightOp).getOp()));
                }
            }
        }
        return units;
    }

    private static Set<Value> globalTarget(StaticFieldRef fieldRef) {
        SootClass declaringClass = fieldRef.getField().getDeclaringClass();
        Optional<SootMethod> clinit = declaringClass.getMethods().stream().filter(m -> "<clinit>".equals(m.getName())).findFirst();
        if (!clinit.isPresent()) {
            return Collections.emptySet();
        }
        SootMethod method = clinit.get();
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        Local targetLocal = null;
        Unit targetUnit = null;
        for (Unit unit : body.getUnits()) {
            if (unit instanceof JAssignStmt) {
                JAssignStmt assignStmt = (JAssignStmt) unit;
                if (assignStmt.getLeftOp().equivTo(fieldRef)) {
                    if (assignStmt.getRightOp() instanceof Local) targetLocal = (Local) assignStmt.getRightOp();
                    targetUnit = unit;
                    break;
                }
            }
        }
        ExceptionalUnitGraph graph = ExceptionalUnitGraphFactory.createExceptionalUnitGraph(body);
        SimpleLocalDefs localDefs = new SimpleLocalDefs(graph);
        return getDefValues(localDefs, targetUnit, targetLocal);
    }
}
