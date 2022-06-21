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
package org.eclipse.emt4j.test.common;

import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Find all SIT test cases from test jars,and write to a file that provide by parameter.
 * How to know a class is a SIT test case?
 * <ul>
 *     <li>Class name must end with Test</li>
 *     <li>Class must contain a annotation <code>TestConf</code></li>
 * </ul>
 *
 * @see org.eclipse.emt4j.test.common.TestConf
 */
public class FindAllTestcaseMain {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String testJarPath = args[0];
        String outputFilePath = args[1];
        List<TestConfObject> testConfObjectList = new ArrayList<>();
        try (JarFile jarFile = new JarFile(testJarPath)) {
            List<String> classList = readClassListFromJar(jarFile);
            for (String className : classList) {
                if (!className.endsWith("Test")) {
                    continue;
                }
                Class testClass = Class.forName(className);
                TestConf testConf = (TestConf) testClass.getAnnotation(TestConf.class);
                if (testConf != null) {
                    TestConfObject testConfObject = new TestConfObject();
                    testConfObject.modes = Arrays.stream(testConf.mode()).map((m) -> m.name()).collect(Collectors.toList());
                    testConfObject.fromVersion = testConf.from().name();
                    testConfObject.toVersion = testConf.to().name();
                    testConfObject.option = testConf.option();
                    testConfObject.testClassName = className;
                    testConfObjectList.add(testConfObject);
                }
            }
        }
        FileUtils.writeStringToFile(new File(outputFilePath), new Gson().toJson(testConfObjectList), "utf-8");
    }

    private static List<String> readClassListFromJar(JarFile jarFile) {
        List<String> classList = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                classList.add(normalize(jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length())));
            }
        }
        return classList;
    }

    private static String normalize(String internalName) {
        return internalName.replace('/', '.').replace('$', '.');
    }
}
