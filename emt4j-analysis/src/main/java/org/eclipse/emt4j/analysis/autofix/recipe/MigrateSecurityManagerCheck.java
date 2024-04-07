/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis.autofix.recipe;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class MigrateSecurityManagerCheck extends Recipe {
    private static final MethodMatcher CHECK_AWT_EVENTQUEUE_PERMISSION = new MethodMatcher("java.lang.SecurityManager checkAwtEventQueueAccess()");
    private static final MethodMatcher CHECK_SYSTEM_CLIPBOARD_ACCESS = new MethodMatcher("java.lang.SecurityManager checkSystemClipboardAccess()");

    @Override
    public String getDisplayName() {
        return "Migrate SecurityManager checkAwtEventQueueAccess and checkSystemClipboardAccess";
    }

    @Override
    public String getDescription() {
        return "Use checkPermission instead of checkAwtEventQueueAccess and checkSystemClipboardAccess.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                method = super.visitMethodInvocation(method, ctx);

                String permmision = null;
                if (CHECK_AWT_EVENTQUEUE_PERMISSION.matches(method)) {
                    permmision = "sun.security.util.SecurityConstants.AWT.CHECK_AWT_EVENTQUEUE_PERMISSION";
                } else if (CHECK_SYSTEM_CLIPBOARD_ACCESS.matches(method)) {
                    permmision = "sun.security.util.SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION";
                }
                if (permmision != null) {
                    JavaTemplate t = JavaTemplate
                            .builder("#{any(java.lang.SecurityManager)}.checkPermission(" + permmision + ")")
                            .build();
                    method = t.apply(updateCursor(method), method.getCoordinates().replace(), method.getSelect());
                }
                return method;
            }
        };
    }
}
