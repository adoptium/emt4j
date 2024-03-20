/********************************************************************************
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emt4j.analysis.common.util.Option;
import org.eclipse.emt4j.analysis.common.util.OptionProcessor;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.out.BinaryFileOutputConsumer;
import org.eclipse.emt4j.analysis.report.ReportMain;
import org.eclipse.emt4j.analysis.source.*;
import org.eclipse.emt4j.common.CheckConfig;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.SourceInformation;
import org.eclipse.emt4j.common.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The main entry of class analysis and report
 */
public class AnalysisMain {
    private static final String DEFAULT_FILE = "analysis_output";
    private static Set<String> analysisTargetClassPaths = new HashSet<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
        Progress root = new Progress(0, 0, "ROOT");
        doReport(doAnalysis(args, new Progress(root, "Analysis")),
                new Progress(root, "Report"));
    }

    private static void doReport(ReportConfig reportConfig, Progress progress) throws InterruptedException, IOException, ClassNotFoundException, URISyntaxException {
        ReportMain.run(reportConfig, progress);
    }

    static ReportConfig doAnalysis(String[] args, Progress progress) throws IOException {
        if (null == args || args.length == 0) {
            printUsage(null);
        }

        progress.printTitle();
        CheckConfig checkConfig = new CheckConfig();
        ReportConfig reportConfig = new ReportConfig();
        final List<Feature> featureList = new ArrayList<>(Collections.singletonList(Feature.DEFAULT));
        AnalysisExecutor analysisExecutor = new AnalysisExecutor(checkConfig);
        OptionProcessor optionProcessor = new OptionProcessor(args);
        optionProcessor.addOption(Option.buildParamWithValueOption("-o", null, reportConfig::setOutputFile));
        optionProcessor.addOption(Option.buildParamWithValueOption("-f", StringUtils::isNumeric, (v) -> checkConfig.setFromVersion(Integer.parseInt(v))));
        optionProcessor.addOption(Option.buildParamWithValueOption("-t", StringUtils::isNumeric, (v) -> checkConfig.setToVersion(Integer.parseInt(v))));
        optionProcessor.addOption(Option.buildParamWithValueOption("-priority", null, checkConfig::setPriority));
        optionProcessor.addOption(Option.buildParamWithValueOption("-p",
                (v) -> "txt".equalsIgnoreCase(v) || "json".equalsIgnoreCase(v) || "html".equalsIgnoreCase(v), (v) -> reportConfig.setOutputFormat(v.toLowerCase())));
        optionProcessor.addOption(Option.buildParamWithValueOption("-j", (v) -> new File(v).exists()
                && new File(v).isDirectory(), reportConfig::setTargetJdkHome));
        optionProcessor.addOption(Option.buildParamNoValueOption("-v", null, (v) -> {
            checkConfig.setVerbose(true);
            reportConfig.setVerbose(true);
        }));
        optionProcessor.addOption(Option.buildParamWithValueOption("-e", (v) -> new File(v).exists()
                && new File(v).isDirectory(), reportConfig::setExternalToolRoot));
        optionProcessor.addOption(Option.buildDefaultOption(
                AnalysisMain::isSource,
                (v) -> {
                    processSource(reportConfig, analysisExecutor, v);
                }));
        optionProcessor.addOption(Option.buildParamWithValueOption(
                "-features",
                (fs) -> fs.isEmpty() || Arrays.stream(fs.split(",")).allMatch((s) -> Feature.getFeatureByCommandLineText(s) != null),
                (fs) -> {
                    if (fs.isEmpty()) return;
                    featureList.clear();
                    featureList.addAll(Arrays.stream(fs.split(",")).map(Feature::getFeatureByCommandLineText).collect(Collectors.toList()));
                }));
        optionProcessor.setShowUsage(AnalysisMain::printUsage);
        if (checkConfig.getFromVersion() >= checkConfig.getToVersion()) {
            printUsage("from version should less than to version");
        }

        optionProcessor.process();

        if (analysisExecutor.hasSource()) {
            File tempFile = File.createTempFile(DEFAULT_FILE, ".dat");
            tempFile.deleteOnExit();
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile.toPath()))) {
                analysisExecutor.setAnalysisOutputConsumer(new BinaryFileOutputConsumer(out));
                analysisExecutor.execute(featureList, progress);
            }
            reportConfig.getInputFiles().add(tempFile);
            System.out.println("Write internal file to " + tempFile + " done.");
        }
        if (reportConfig.getInputFiles().isEmpty()) {
            printUsage(null);
        }

        return reportConfig;
    }

    private static SourceInformation buildSourceInformation(String str, boolean isDependency) {
        SourceInformation info = new SourceInformation();
        String[] arr = str.split(":");
        info.setIdentifier(arr[1]);
        info.setExtras(new String[]{str});
        info.setDependency(isDependency);
        info.setFullIdentifier(str);
        return info;
    }

    private static void processSource(ReportConfig reportConfig, AnalysisExecutor analysisExecutor, String v) {
        File file = new File(v);
        if (file.isDirectory() && file.getName().equals(".emt4j")) {
            try {
                BufferedReader br = Files.newBufferedReader(new File(file, "modules").toPath());
                String str;
                while ((str = br.readLine()) != null) {
                    String[] pair = str.split("=");
                    String[] paths = pair[1].split(File.pathSeparator);
                    SourceInformation info = buildSourceInformation(pair[0], false);
                    for (String path : paths) {
                        DependencySource dependencySource = doProcessSource(reportConfig, analysisExecutor, path);
                        if (dependencySource != null) {
                            dependencySource.setInformation(info);
                        }
                    }
                }
                br.close();

                br = Files.newBufferedReader(new File(file, "dependencies").toPath());
                while ((str = br.readLine()) != null) {
                    String[] pair = str.split("=");
                    if (pair[1].endsWith(".pom")) {
                        continue;
                    }
                    DependencySource dependencySource = doProcessSource(reportConfig, analysisExecutor, pair[1]);
                    if (dependencySource != null) {
                        dependencySource.setInformation(buildSourceInformation(pair[0], true));
                    }
                }
                br.close();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            doProcessSource(reportConfig, analysisExecutor, v);
        }
    }

    private static DependencySource doProcessSource(ReportConfig reportConfig, AnalysisExecutor analysisExecutor, String v) {
        Optional<DependencySource> opt = getSource(v);
        if (!opt.isPresent()) {
            System.err.println("Skip " + v + " since it doesn't exist");
            return null;
        }
        DependencySource ds = opt.get();
        if (ds.needAnalysis()) {
            analysisExecutor.add(ds);
            analysisTargetClassPaths.add(ds.getFile().getAbsolutePath());
        } else {
            reportConfig.getInputFiles().add(ds.getFile());
        }
        return ds;
    }

    private static boolean isSource(String file) {
        File f = new File(file);
        if (!f.exists()) {
            return false;
        }
        if (f.isFile()) {
            return file.endsWith(".class") || file.endsWith(".cfg") || file.endsWith(".jar") || file.endsWith(".dat");
        }
        return f.isDirectory();
    }

    private static Optional<DependencySource> getSource(String file) {
        File f = new File(file);
        if (!f.exists()) {
            return Optional.empty();
        }

        if (f.isFile()) {
            switch (FileUtil.fileType(file)) {
                case Class:
                    return Optional.of(new SingleClassSource(f));
                case Cfg:
                    return Optional.of(new JavaOptionSource(f));
                case Jar:
                    return Optional.of(new SingleJarSource(f));
                case Dat:
                    return Optional.of(new AgentOutputAsSource(f));
            }
        } else if (f.isDirectory()) {
            return Optional.of(new DirectorySource(f));
        }

        return Optional.empty();
    }

    private static void printUsage(String option) {
        if (option != null) {
            System.err.println(option + " is invalid!");
        }

        String osName = System.getProperty("os.name");
        boolean windows = osName != null && osName.toLowerCase().contains("windows");
        String launcher = windows ? "analysis.bat" : "analysis.sh";
        System.err.println("Usage:" + launcher + " [-f version] [-t version] [-p txt] [-o outputfile] [-j target jdk home] [-e external tool home] [-v] [-features features] <files>");
        System.err.println("-f From which JDK version,default is 8");
        System.err.println("-t To which JDK version,default is 11");
        System.err.println("-p The report format.Can be TXT or JSON or HTML.Default is HTML");
        System.err.println("-o Write analysis to output file. Default is " + DEFAULT_FILE);
        System.err.println("-j Target JDK home. Provide target jdk home can help to find more compatible problems.");
        System.err.println("-e The root directory of external tools.");
        System.err.println("-v Show verbose information.");
        System.err.println("-features Override features with a comma-split string.");
        System.err.println("files can be combination of following types :");
        final String[] allSupportFiles = new String[]{
                "Agent output file(*.dat)",
                "Single class file(*.class)",
                "Directory contains jars,classes",
                "File end with .cfg that contain java option(*.cfg)"
        };
        for (int i = 1; i <= allSupportFiles.length; i++) {
            System.err.println(i + ". " + allSupportFiles[i - 1]);
        }

        System.err.println("files can be class, jar,directory or java source file.");
        System.exit(1);
    }

    public static Set<String> getAnalysisTargetClassPaths() {
        return analysisTargetClassPaths;
    }
}
