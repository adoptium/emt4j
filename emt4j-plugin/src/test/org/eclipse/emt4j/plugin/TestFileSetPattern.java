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
package org.eclipse.emt4j.plugin;


import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestFileSetPattern {

    @Test
    public void testMatch() {
        assertTrue(new FileSetPattern("**/java/*").matches("a/b/c/java/A.java"));
        assertTrue(new FileSetPattern("a/b/c/d/e/f.java").matches("a/b/c/d/e/f.java"));

        assertTrue(new FileSetPattern("a/*Test*.java").matches("a/OneTest.java"));
        assertTrue(new FileSetPattern("a/*Test*.java").matches("a/Test.java"));
        assertTrue(new FileSetPattern("a/*Test*.java").matches("a/OneTestUnit.java"));

        assertTrue(new FileSetPattern("a/b/c/").matches("a/b/c/d/e.java"));
        assertTrue(new FileSetPattern("**/CVS/*").matches("CVS/Repository"));
        assertTrue(new FileSetPattern("**/CVS/*").matches("org/apache/CVS/Entries"));
        assertTrue(new FileSetPattern("**/CVS/*").matches("org/apache/jakarta/tools/ant/CVS/Entries"));
        assertFalse(new FileSetPattern("**/CVS/*").matches("org/apache/CVS/foo/bar/Entries"));

        assertTrue(new FileSetPattern("org/apache/jakarta/**").matches("org/apache/jakarta/tools/ant/docs/index.html"));
        assertTrue(new FileSetPattern("org/apache/jakarta/**").matches("org/apache/jakarta/test.xml"));
        assertFalse(new FileSetPattern("org/apache/jakarta/**").matches("org/apache/xyz.java"));

        assertTrue(new FileSetPattern("org/apache/**/CVS/*").matches("org/apache/CVS/Entries"));
        assertTrue(new FileSetPattern("org/apache/**/CVS/*").matches("org/apache/jakarta/tools/ant/CVS/Entries"));
        assertFalse(new FileSetPattern("org/apache/**/CVS/*").matches("org/apache/CVS/foo/bar/Entries"));
    }
}
