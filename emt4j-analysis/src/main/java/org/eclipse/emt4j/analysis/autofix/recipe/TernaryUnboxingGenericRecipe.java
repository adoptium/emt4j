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

import org.openrewrite.*;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;


public class TernaryUnboxingGenericRecipe extends Recipe implements ReportingRecipe {

    @Override
    public String getDisplayName() {
        return "Fix null problem in Conditional Statement";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Convert code like `condition ? int : methodCall()` to `condition ? Integer.valueOf(int) : methodCall()` where methodCall() returns Integer type.";
    }

    @Override
    public RecipeFixReporter getReporter() {
        return new AbstractRecipeFixReporter.CountByFileRecipeFixReporter("autofix.java.ternaryUnboxingGeneric");
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.Ternary visitTernary(J.Ternary ternary, ExecutionContext executionContext) {
                ternary = super.visitTernary(ternary, executionContext);
                Expression primitivePart = null, boxingPart = null;
                if (ternary.getTruePart().getType() instanceof JavaType.Primitive) {
                    primitivePart = ternary.getTruePart();
                } else if (ternary.getTruePart().getType() instanceof JavaType.Class) {
                    boxingPart = ternary.getTruePart();
                }
                if (ternary.getFalsePart().getType() instanceof JavaType.Primitive) {
                    primitivePart = ternary.getFalsePart();
                } else if (ternary.getFalsePart().getType() instanceof JavaType.Class) {
                    boxingPart = ternary.getFalsePart();
                }

                if (primitivePart == null
                        || boxingPart == null
                        || !JavaCodeHelper.isPrimitive(((JavaType.Primitive) primitivePart.getType()).getKeyword())
                        || !JavaCodeHelper.isBoxing((((JavaType.Class) boxingPart.getType()).getFullyQualifiedName()))
                        || !(boxingPart instanceof J.MethodInvocation)
                    // we also need to check if boxing returns a generic type, but that is difficult
                ) {
                    return ternary;
                }

                String primitiveType = ((JavaType.Primitive) primitivePart.getType()).getKeyword();
                String boxingType = JavaCodeHelper.getBoxingFromPrimitive(primitiveType);
                String templateString = String.format("%s.valueOf(#{any(%s)})", boxingType.substring(boxingType.lastIndexOf(".") + 1), primitiveType);
                JavaTemplate template = JavaTemplate.builder(templateString)
                        .imports(boxingType)
                        .build();
                J.MethodInvocation conversion = template.apply(new Cursor(getCursor(), primitivePart), primitivePart.getCoordinates().replace(), primitivePart);
                if (primitivePart == ternary.getTruePart()) {
                    ternary = ternary.withTruePart(conversion);
                } else if (primitivePart == ternary.getFalsePart()) {
                    ternary = ternary.withFalsePart(conversion);
                }

                return ternary;
            }
        };
    }
}
