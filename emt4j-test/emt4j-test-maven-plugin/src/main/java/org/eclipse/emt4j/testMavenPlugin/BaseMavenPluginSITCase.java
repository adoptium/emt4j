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
package org.eclipse.emt4j.testMavenPlugin;

import org.apache.commons.io.FileUtils;
import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.test.common.RunJavaUtil;
import org.eclipse.emt4j.test.common.SITBaseCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public abstract class BaseMavenPluginSITCase extends SITBaseCase {
    @Override
    public final void prepareDynamicTestTarget(String workDir) throws IOException {
        Path fromPath = Paths.get(workDir, "..", "tmp-classes", "projects", getTestProject());
        FileUtils.copyDirectory(fromPath.toFile(), new File(workDir));
    }


    @Override
    public final void run() throws Exception {
        // we will run plugin in TestCaseSuite
    }

    @Override
    public final void verify(JsonReport ignore) {
        verify();
    }

    protected abstract String getTestProject();

    protected abstract void verify();

    protected String getFileContent(String file) {
        try {
            asserFileExist(file);
            return FileUtils.readFileToString(new File(file), "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getStdout() {
        return getFileContent("stdout");
    }

    protected void asserFileExist(String file) {
        File file1 = new File(file);
        assertTrue(file1.exists(), "file " + file1.getAbsolutePath() + " does not exist!");
    }

    protected void asserFileNotExist(String file) {
        File file1 = new File(file);
        assertTrue(!file1.exists(), "file " + file1.getAbsolutePath() + " exists!");
    }

    protected void assertApplyFixedPatchSucceed(String patch) {
        asserFileExist(patch);
        try {
            // git apply command does not require a git repository
            String command = "git apply " + patch;
            RunJavaUtil.runProcess(Arrays.asList(command.split(" +")));
        } catch (Exception e) {
            System.out.println(patch + " content:");
            System.out.println(getFileContent(patch));
            throw new RuntimeException("fail to apply git patch", e);
        }
    }

    protected void assertContainsInContinuousLines(String[] text, List<String> find) {
        boolean pass = containsInContinuousLines(text, find);
        if (!pass) {
            printFindStringFailLog(text, find);
            assertTrue(false);
        }
    }

    private void printFindStringFailLog(String[] text, List<String> find) {
        System.out.println("Test fail: can not find\n");
        for (String s : find) {
            System.out.println(s);
        }
        System.out.println("\nin text:\n");
        for (String s : text) {
            System.out.println(s);
        }
    }

    protected boolean containsInContinuousLines(String[] text, List<String> find) {
        out:
        for (int i = 0; i < text.length; i++) {
            for (int j = 0; j < find.size(); j++) {
                if (i + j >= text.length) {
                    break out;
                }
                if (!text[i + j].contains(find.get(j))) {
                    continue out;
                }
            }
            return true;
        }
        return false;
    }

    protected boolean containsInOrder(String[] text, List<String> find) {
        int textIndex = 0;
        nextFind:
        for (String s : find) {
            while (textIndex < text.length) {
                boolean contains = text[textIndex].contains(s);
                textIndex++;
                if (contains) {
                    continue nextFind;
                }
            }
            return false;
        }
        return true;
    }

    protected void assertContainsInOrder(String[] text, List<String> find) {
        boolean pass = containsInOrder(text, find);
        if (!pass) {
            printFindStringFailLog(text, find);
            assertTrue(false);
        }
    }

    protected String[] getPomDependencyManagement(String[] pom) {
        int begin = -1, end = -1;
        for (int i = 0; i < pom.length; i++) {
            String line = pom[i];
            if (line.contains("<dependencyManagement")) {
                begin = i;
            }
            if (line.contains("</dependencyManagement")) {
                end = i + 1;
                return Arrays.copyOfRange(pom, begin, end);
            }
        }
        return null;
    }

    protected String[] getPomDependencies(String[] pom) {
        int begin = -1, end = -1;
        int ignore = 0;
        for (int i = 0; i < pom.length; i++) {
            String line = pom[i];
            if (line.contains("<profiles") || line.contains("<dependencyManagement")) {
                ignore++;
            }
            if (line.contains("</profiles") || line.contains("</dependencyManagement")) {
                ignore--;
            }
            if (ignore == 0 && line.contains("<dependencies")) {
                begin = i;
            }
            if (ignore == 0 && line.contains("</dependencies")) {
                end = i + 1;
                return Arrays.copyOfRange(pom, begin, end);
            }
        }
        return null;
    }
}
