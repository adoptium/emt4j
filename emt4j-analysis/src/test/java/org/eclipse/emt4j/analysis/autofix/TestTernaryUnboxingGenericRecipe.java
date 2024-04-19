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

import org.eclipse.emt4j.analysis.autofix.recipe.TernaryUnboxingGenericRecipe;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class TestTernaryUnboxingGenericRecipe implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new TernaryUnboxingGenericRecipe());
    }

    @Test
    public void positiveCases() {
        rewriteRun(
                java("import java.util.HashMap;\n" +
                                "\n" +
                                "public class Main<T> {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        Integer integer1 = true ? new HashMap<Integer,Integer>().get(1) : 2;\n" +
                                "        Integer integer2 = true ? 2 : new HashMap<Integer,Integer>().get(1);\n" +
                                "        Long long1 = true ? 2L : new HashMap<Integer,Long>().get(1);\n" +
                                "        Byte byte1 = true ? 1 : new HashMap<Integer,Byte>().get(1);\n" +
                                "        byte byte2 = true ? 1 : new HashMap<Integer,Byte>().get(1);\n" +
                                "        long long2 = true ? 1L : new HashMap<Integer,Integer>().get(1);\n" +
                                "        Long long3 = true ? 1L : new HashMap<Integer,Integer>().get(1);\n" +
                                "    }\n" +
                                "}",
                        "import java.util.HashMap;\n" +
                                "\n" +
                                "public class Main<T> {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        Integer integer1 = true ? new HashMap<Integer,Integer>().get(1) : Integer.valueOf(2);\n" +
                                "        Integer integer2 = true ? Integer.valueOf(2) : new HashMap<Integer,Integer>().get(1);\n" +
                                "        Long long1 = true ? Long.valueOf(2L) : new HashMap<Integer,Long>().get(1);\n" +
                                "        Byte byte1 = true ? Byte.valueOf((byte) 1) : new HashMap<Integer,Byte>().get(1);\n" +
                                "        byte byte2 = true ? Byte.valueOf((byte) 1) : new HashMap<Integer,Byte>().get(1);\n" +
                                "        long long2 = true ? Long.valueOf(1L) : new HashMap<Integer,Integer>().get(1);\n" +
                                "        Long long3 = true ? Long.valueOf(1L) : new HashMap<Integer,Integer>().get(1);\n" +
                                "    }\n" +
                                "}")
        );
    }

    @Test
    public void negativeCases() {
        rewriteRun(
                java("import java.util.HashMap;\n" +
                        "\n" +
                        "public class Main<T> {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Integer integer1 = false ? 1 : 2;\n" +
                        "        Integer integer2 = false ? 1 : new HashMap<>().size() + 1;\n" +
                        "        Integer integer3 = false ? Integer.valueOf(1) : Integer.valueOf(2);\n" +
                        "        Long long1 = true ? Long.valueOf(1) : new HashMap<Integer,Long>().get(1);\n" +
                        "    }\n" +
                        "}")
        );

    }
}
