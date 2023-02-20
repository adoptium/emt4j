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

import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraphFactory;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.util.List;
import java.util.Set;

public class CastSystemClassLoaderToURLClassLoaderAnalyzer extends BaseAnalyzer {

    @Override
    public String rule() {
        return "cast-system-classloader-to-url-classloader";
    }

    boolean doAnalyze(SootMethod method) {
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        ExceptionalUnitGraph graph = ExceptionalUnitGraphFactory.createExceptionalUnitGraph(body);

        SimpleLocalDefs localDefs = new SimpleLocalDefs(graph);
        for (Unit unit : body.getUnits()) {
            List<ValueBox> boxes = unit.getUseBoxes();
            for (ValueBox box : boxes) {
                Value value = box.getValue();
                if (value instanceof JCastExpr) {
                    Type castType = ((JCastExpr) value).getCastType();
                    if (!(castType instanceof RefType) || !((RefType) castType).getClassName().equals("java.net.URLClassLoader")) {
                        continue;
                    }
                    Local local = (Local) ((JCastExpr) value).getOpBox().getValue();
                    if (isTarget(localDefs, unit, local)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isTarget(SimpleLocalDefs localDefs, Unit unit, Local local) {
        Set<Value> defs = getDefValues(localDefs, unit, local);
        for (Value def : defs) {
            if (def instanceof JStaticInvokeExpr) {
                JStaticInvokeExpr invoke = (JStaticInvokeExpr) def;
                SootMethod method = invoke.getMethod();
                if (method.getDeclaringClass().getName().equals("java.lang.ClassLoader")
                    && method.getName().equals("getSystemClassLoader")) {
                    return true;
                }
            }
        }
        return false;
    }
}
