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
package org.eclipse.emt4j.analysis.autofix;

import org.eclipse.emt4j.analysis.autofix.recipe.MigrateSecurityManagerCheck;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class TestMigrateSecurityManager implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateSecurityManagerCheck());
    }

    @Test
    public void checkAwtEventQueueAccess() {
        rewriteRun(
                java("public class SecurityManagerProblem {\n" +
                        "    void checkAwtEventQueueAccess(SecurityManager securityManager) {\n" +
                        "        try {\n" +
                        "            securityManager.checkAwtEventQueueAccess();\n" +
                        "        } catch (Exception e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", "public class SecurityManagerProblem {\n" +
                        "    void checkAwtEventQueueAccess(SecurityManager securityManager) {\n" +
                        "        try {\n" +
                        "            securityManager.checkPermission(sun.security.util.SecurityConstants.AWT.CHECK_AWT_EVENTQUEUE_PERMISSION);\n" +
                        "        } catch (Exception e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
        );
    }

    @Test
    public void checkSystemClipboardAccess() {
        rewriteRun(
                java("public class SecurityManagerProblem {\n" +
                        "    void checkSystemClipboardAccess(SecurityManager securityManager) {\n" +
                        "        try {\n" +
                        "            securityManager.checkSystemClipboardAccess();\n" +
                        "        } catch (Exception e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", "public class SecurityManagerProblem {\n" +
                        "    void checkSystemClipboardAccess(SecurityManager securityManager) {\n" +
                        "        try {\n" +
                        "            securityManager.checkPermission(sun.security.util.SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION);\n" +
                        "        } catch (Exception e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
        );
    }
}
