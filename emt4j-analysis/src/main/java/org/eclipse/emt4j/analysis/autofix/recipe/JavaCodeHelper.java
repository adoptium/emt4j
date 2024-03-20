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

import java.util.*;

public class JavaCodeHelper {
    private JavaCodeHelper() {
    }

    private static final Map<String, String> PRIMITIVE_2_BOXING = new HashMap<>();
    private static final Map<String, String> BOXING_2_PRIMITIVE = new HashMap<>();

    static {
        PRIMITIVE_2_BOXING.put("boolean", "java.lang.Boolean");
        PRIMITIVE_2_BOXING.put("byte", "java.lang.Byte");
        PRIMITIVE_2_BOXING.put("char", "java.lang.Character");
        PRIMITIVE_2_BOXING.put("double", "java.lang.Double");
        PRIMITIVE_2_BOXING.put("float", "java.lang.Float");
        PRIMITIVE_2_BOXING.put("int", "java.lang.Integer");
        PRIMITIVE_2_BOXING.put("long", "java.lang.Long");
        PRIMITIVE_2_BOXING.put("short", "java.lang.Short");
        for (Map.Entry<String, String> entry : PRIMITIVE_2_BOXING.entrySet()) {
            BOXING_2_PRIMITIVE.put(entry.getValue(), entry.getKey());
        }
    }

    public static boolean isPrimitive(String type) {
        return PRIMITIVE_2_BOXING.containsKey(type);
    }
    public static boolean isBoxing(String type) {
        return BOXING_2_PRIMITIVE.containsKey(type);
    }

    public static String getPrimitiveFromBoxing(String boxing) {
        return BOXING_2_PRIMITIVE.get(boxing);
    }

    public static String getBoxingFromPrimitive(String primitive) {
        return PRIMITIVE_2_BOXING.get(primitive);
    }

}
