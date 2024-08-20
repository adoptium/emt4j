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

import org.eclipse.emt4j.analysis.autofix.recipe.StreamCountRecipe;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class TestStreamCount implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new StreamCountRecipe());
    }

    @Test
    public void checkStreamCount() {
        rewriteRun(
                java(
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int[] intStream = new int[]{1, 2, 3};\n" +
                        "        double[] doubleStream = new double[]{1, 2, 3};\n" +
                        "        long[] longStream = new long[]{1, 2, 3};\n" +
                        "        String[] refStream = new String[]{\"1\", \"2\", \"3\"};\n" +
                        "        long count;\n" +
                        "        count = Arrays.stream(intStream).peek(System.out::println).count();\n" +
                        "        count = Arrays.stream(doubleStream).peek(System.out::println).count();\n" +
                        "        count = Arrays.stream(longStream).peek(System.out::println).count();\n" +
                        "        count = Arrays.stream(refStream).peek(System.out::println).count();\n" +
                        "    }\n" +
                        "}",
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int[] intStream = new int[]{1, 2, 3};\n" +
                        "        double[] doubleStream = new double[]{1, 2, 3};\n" +
                        "        long[] longStream = new long[]{1, 2, 3};\n" +
                        "        String[] refStream = new String[]{\"1\", \"2\", \"3\"};\n" +
                        "        long count;\n" +
                        "        count = Arrays.stream(intStream).peek(System.out::println).mapToLong(_ignore -> 1).sum();\n" +
                        "        count = Arrays.stream(doubleStream).peek(System.out::println).mapToLong(_ignore -> 1).sum();\n" +
                        "        count = Arrays.stream(longStream).peek(System.out::println).map(_ignore -> 1).sum();\n" +
                        "        count = Arrays.stream(refStream).peek(System.out::println).mapToLong(_ignore -> 1).sum();\n" +
                        "    }\n" +
                        "}")
        );
    }
}
