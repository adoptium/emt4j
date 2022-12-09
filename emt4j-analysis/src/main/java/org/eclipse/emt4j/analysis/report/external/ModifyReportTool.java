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
package org.eclipse.emt4j.analysis.report.external;

import org.eclipse.emt4j.analysis.common.model.ExternalToolParam;
import org.eclipse.emt4j.analysis.common.util.ProcessUtil;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.fileformat.BodyRecord;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For the external tools that may modify(delete and add) the EMT4J output report based on the its analysis results.
 * The tool's dependencies should be specified in the resource file externaltool/full.qualified.tool.class.name. Each line
 * of the resource file is one maven dependency, in the form of {@code [groupid]:[artifactid]:[version](:[classifier])}.
 */
public abstract class ModifyReportTool implements Tool {

    protected int leastJDKVersion;  // The least JDK version required to run the external tool
    /**
     * The class path for the external tool, should be prepared by {@link ModifyReportTool#resolveDependencies()}
     */
    protected String toolCP;

    /**
     * Execute the external tool with the given BodyRecord list and other parameters. The input BodyRecord list shall be
     * modified(both deletion and adding), and a new list will be returned.
     *
     * @param originalReport
     * @param etp
     * @param reportConfig
     * @param parentProgress
     * @return
     * @throws ExternalToolFailException fail to execute the external tool.
     */
    public List<BodyRecord> run(List<BodyRecord> originalReport, ExternalToolParam etp, ReportConfig reportConfig, Progress parentProgress) throws ExternalToolFailException {
        try {
            initWithOriginalReport(originalReport);
            resolveDependencies();
            return analysis(etp, reportConfig, parentProgress);
        } catch (Throwable t) {
            ExternalToolFailException e;
            if (t instanceof ExternalToolFailException) {
                e = (ExternalToolFailException) t;
            } else {
                e = new ExternalToolFailException(t);
            }
            throw e;
        }
    }

    /**
     * By default, put everything in tool's lib directory to cp.
     */
    protected void resolveDependencies() {
        URL location = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            Path jarPath = Paths.get(location.toURI());
            Path lib = jarPath.getParent().resolve("lib");
            if(Files.exists(lib)){
                toolCP = Files.list(lib).filter(p->p.getFileName().toString().endsWith(".jar")).map(p->p.normalize().toAbsolutePath().toString()).collect(Collectors.joining(File.pathSeparator));
            }
        } catch (URISyntaxException|IOException e) {
            throw new ExternalToolFailException(e);
        }
    }

    protected abstract void initWithOriginalReport(List<BodyRecord> originalReport);

    /**
     * The external tool may depend on a particular JDK version set by {@link ModifyReportTool#leastJDKVersion}.
     * Look for the proper JDK for external tool from 4 places:
     * <ol>
     *     <li>Currently executing java home. Usually 8.</li>
     *     <li>-j option. 11 or 17</li>
     *     <li>System variable $JAVA_HOME</li>
     *     <li>java in system path</li>
     * </ol>
     *
     * @param reportConfig
     * @return
     */
    protected Path lookForProperJavaHome(ReportConfig reportConfig) throws IOException {
        // 1.Check current running java version first. It should be JDK 8.
        String javaVersion = System.getProperty("java.version");
        int currentJavaVersion = extractJavaVersionFromString(javaVersion, 0);
        if (currentJavaVersion >= leastJDKVersion) {
            return appendJava(Paths.get(System.getProperty("java.home")));
        }

        // 2.Check JDK home set by -j option. It's JDK 11 or 17.
        if (reportConfig.getTargetJdkHome() != null) {
            return appendJava(Paths.get(reportConfig.getTargetJdkHome()));
        }

        // 3. -j not set, check Java on current machine by looking up system variables
        Path systemJavaHome = Paths.get(System.getenv("JAVA_HOME"));
        Path systemJava = appendJava(systemJavaHome);
        int systemJavaVersion = checkJDKVersion(systemJava);
        if (systemJavaVersion >= leastJDKVersion) {
            return systemJava;
        }

        // 4. Check java on system $PATH
        String[] paths = System.getenv("PATH").split(File.pathSeparator);
        for (String path : paths) {
            Path javaPath = Paths.get(path).resolve("java");
            if (Files.exists(javaPath)) {
                int systemPathJavaVersion = checkJDKVersion(javaPath);
                if (systemPathJavaVersion >= leastJDKVersion) {
                    return javaPath;
                }
                break;
            }
        }

        // 5. Throw exception if non of above JDK is found as proper
        throw new RuntimeException(this.getClass().getSimpleName() + " requires at least JDK " + leastJDKVersion + " to run." +
                "This problem can be fixed in 4 ways: 1) Start EMT4J with the required JDK; " +
                "2) Set EMT4J -j option to the required JDK; 3) Set your system variable JAVA_HOME to the required JDK;" +
                "4) Set required JDK bin to the system variable PATH.");

    }

    private static Path appendJava(Path jdkHome) {
        return jdkHome.resolve("bin").resolve("java");
    }

    private int checkJDKVersion(Path java) throws IOException {
        String output = ProcessUtil.run(java.toString(), "-version");
        // run `java -version` will output the version enclosed in a pair of quotes("").
        return extractJavaVersionFromString(output, output.indexOf("\"") + 1);
    }

    private int extractJavaVersionFromString(String str, int startIndex) {
        if (str.contains("1.8")) {
            return 8;
        } else {
            String versionStr = str.substring(startIndex, startIndex + 2);
            try {
                return Integer.parseInt(versionStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
}
