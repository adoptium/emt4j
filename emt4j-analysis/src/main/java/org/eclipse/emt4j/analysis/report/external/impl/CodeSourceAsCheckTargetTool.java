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
package org.eclipse.emt4j.analysis.report.external.impl;

import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.common.model.ExternalToolParam;
import org.eclipse.emt4j.analysis.common.util.ProcessUtil;
import org.eclipse.emt4j.analysis.report.external.Tool;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CodeSourceAsCheckTargetTool implements Tool {

    @Override
    public List<BodyRecord> analysis(ExternalToolParam etp, ReportConfig reportConfig, Progress parentProgress) throws InterruptedException {
        if (StringUtils.isEmpty(etp.getTargetJdkHome()) || etp.getClassesOrJars().isEmpty()) {
            return Collections.emptyList();
        }
        String toolPath = getToolPath(etp);
        if (!new File(toolPath).exists()) {
            System.out.print("WARNING: Cannot find the " + toolPath + ",so skip some checking.");
            return Collections.emptyList();
        }

        log(reportConfig, "Run external tool :" + name());

        if (reportConfig.isVerbose()) {
            etp.getClassesOrJars().forEach((c) -> System.out.println("\t Check Target: [" + c + "]"));
        }

        CountDownLatch latch = new CountDownLatch(etp.getClassesOrJars().size());
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<BodyRecord> list = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger();
        Progress progress = new Progress(parentProgress, 0, etp.getClassesOrJars().size(), "Execute tool " + name());
        parentProgress.printProgress(0);
        for (File jarOrClass : etp.getClassesOrJars()) {
            executor.submit(() -> {
                try {
                    count.incrementAndGet();
                    String[] command = getCommand(toolPath, jarOrClass.getCanonicalPath());
                    log(reportConfig, "\tRun command " + String.join(" ", command));
                    String result = ProcessUtil.run(command);
                    log(reportConfig, "\rResult: " + result);
                    list.addAll(parseOutput(jarOrClass, result));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    synchronized (progress) {
                        progress.printProgress(count.get());
                    }
                    latch.countDown();
                }
            });
        }

        executor.shutdown();
        latch.await();

        return list;
    }

    void log(ReportConfig reportConfig, String str) {
        if (reportConfig.isVerbose()) {
            System.out.println(str);
        }
    }

    protected abstract List<BodyRecord> parseOutput(File jarOrClass, String result) throws MalformedURLException;

    protected abstract String[] getCommand(String toolPath, String jarOrClass);

    protected abstract String getToolPath(ExternalToolParam etp);
}
