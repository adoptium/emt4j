/********************************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.common.rule;

import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.classanalyze.ClassInspectorInstance;
import org.eclipse.emt4j.common.ClassSymbol;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedWriter;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class TestClassMethodsAccessor {
    private Queue e = new PriorityQueue();
    private static final Set j = new HashSet();

    @Test
    public void testGetReferenceClassSet() {
        Set<String> classesSet = ClassInspectorInstance.getInstance().getReferenceClassSet(TestClassMethodsAccessor.class);
        String[] expectedClasses = new String[]{
                "java.io.BufferedWriter",
                "java.util.concurrent.atomic.AtomicBoolean",
                "java.lang.ClassNotFoundException",
                "java.io.File",
                "java.time.LocalDate",
                "java.lang.Class",
                "java.time.Period",
                "java.text.DateFormat",
                "java.text.SimpleDateFormat",
                "java.util.SortedMap",
                "org.eclipse.emt4j.common.rule.TestClassMethodsAccessor",
                "java.util.Queue",
                "java.util.PriorityQueue",
                "java.util.Set",
                "java.util.HashSet",
                "java.util.Calendar",
                "java.util.TimeZone",
                "java.util.Locale",
                "java.util.HashMap"

        };
        for (String expected : expectedClasses) {
            assertTrue(expected + " not found!Found classes is : " + String.join(",", classesSet), classesSet.contains(expected));
        }

        ClassSymbol symbol = ClassInspectorInstance.getInstance().getSymbolInClass(TestClassMethodsAccessor.class);
        assertTrue(symbol.getConstantPoolSet().contains("java.time.LocalDate"));
        assertTrue(symbol.getConstantPoolSet().contains("a.txt"));
        assertTrue(symbol.getConstantPoolSet().contains("yyyy"));
        assertTrue(symbol.getConstantPoolSet().contains("nashorn"));

        assertTrue(symbol.getCallMethodSet().contains(new DependTarget.Method("java.lang.Class", "forName", DependType.METHOD)));
        assertTrue(symbol.getCallMethodSet().contains(new DependTarget.Method("java.util.Queue", "size", DependType.METHOD)));
        assertTrue(symbol.getCallMethodSet().contains(new DependTarget.Method("javax.script.ScriptEngineManager", "getEngineByName", DependType.METHOD)));
    }

    private File foo(BufferedWriter a, AtomicBoolean b) throws ClassNotFoundException {
        //by reflection
        Class c = Class.forName("java.time.LocalDate");
        //by invoke method
        Period d = Period.ofDays(123);
        //by new a instance
        DateFormat e = new SimpleDateFormat("yyyy");
        //by new instance array
        SortedMap[] f = new SortedMap[2];
        //access field
        TestClassMethodsAccessor h = new TestClassMethodsAccessor();
        int i = h.e.size();
        //access static field
        i += j.size();
        //method parameter
        Calendar k = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        HashMap[][] l = new HashMap[3][10];

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        return new File("a.txt");
    }
}
