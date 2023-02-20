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
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;

public class PatternCompileAnalyzer extends BaseAnalyzer {

    @Override
    public String rule() {
        return "pattern-compile";
    }

    @Override
    boolean doAnalyze(SootMethod method) {
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        for (Unit unit : body.getUnits()) {
            Stmt stmt = (Stmt) unit;
            if (!stmt.containsInvokeExpr()) {
                continue;
            }

            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            if (!(invokeExpr instanceof StaticInvokeExpr)) {
                continue;
            }

            SootMethod callee = invokeExpr.getMethod();
            if (callee.getDeclaringClass().getName().equals("java.util.regex.Pattern") && callee.getName().equals("compile")) {
                if (invokeExpr.getArgCount() != 2) {
                    continue;
                }
                Value arg = invokeExpr.getArg(1);
                if (arg instanceof IntConstant) {
                    int flags = ((IntConstant) arg).value;
                    if ((flags & ~ALL_FLAGS) != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static final int UNIX_LINES = 0x01;
    private static final int CASE_INSENSITIVE = 0x02;
    private static final int COMMENTS = 0x04;
    private static final int MULTILINE = 0x08;
    private static final int LITERAL = 0x10;
    private static final int DOTALL = 0x20;
    private static final int UNICODE_CASE = 0x40;
    private static final int CANON_EQ = 0x80;
    private static final int UNICODE_CHARACTER_CLASS = 0x100;
    private static final int ALL_FLAGS = CASE_INSENSITIVE | MULTILINE |
                                         DOTALL | UNICODE_CASE | CANON_EQ | UNIX_LINES | LITERAL |
                                         UNICODE_CHARACTER_CLASS | COMMENTS;

}
