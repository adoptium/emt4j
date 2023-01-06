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
package org.eclipse.emt4j.agent.common.file;

import org.eclipse.emt4j.agent.common.Constant;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.InstanceRuleManager;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <code>ReportRecorder</code> implement an asynchronous <code>Recorder</code>.
 * For each dependency,there one to man rules need execute,and some rules do some cost time work.
 * When call record method,<code>ReportRecorder</code> put in a queue,then a background daemon thread
 * take it,execute all rules,and write to file finally.
 */
public class ReportRecorder implements Recorder {

    /**
     * a temporary buffer that needs to process dependency.
     */
    private BlockingQueue<Dependency> writeBuffer;

    /**
     * main work thread
     */
    private Thread writeThread;

    /**
     * Avoid duplicate dependency write more than one time.
     */
    private Set<Integer> alreadyWritten = new HashSet<>();

    private CheckResultFileWriter checkResultFileWriter;

    public ReportRecorder(CheckResultFileWriter writer) {
        this.writeBuffer = new LinkedBlockingQueue<>();
        this.checkResultFileWriter = writer;
    }

    /**
     * put in a queue, then a background thread will take it.
     *
     * @param dependency            dependency
     * @throws InterruptedException if InterruptedException occurred
     */
    @Override
    public void record(Dependency dependency) throws InterruptedException {
        if (dependency != null) {
            writeBuffer.put(dependency);
        }
    }

    /**
     * Start a daemon thread, the thread will take each dependency from the queue,
     * then provide dependency as a parameter to all rules.
     * If the check failed, write the result to the file.
     *
     * @return true if success
     * @throws IOException if IO operation failed
     */
    @Override
    public boolean init() throws IOException {
        checkResultFileWriter.begin();
        writeThread = new Thread(() -> {
            try {
                while (true) {
                    Dependency dependency = writeBuffer.take();
                    int hashCode = dependency.hashCode();
                    if (alreadyWritten.contains(hashCode)) {
                        continue;
                    }
                    publish(dependency);
                    alreadyWritten.add(hashCode);
                }
            } catch (Throwable e) {
                System.err.println("Write report thread occur exception,so exit");
                e.printStackTrace();
            } finally {
                try {
                    checkResultFileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, Constant.AGENT_REPORT_WRITE_THREAD);
        writeThread.setDaemon(true);
        writeThread.start();
        return true;
    }

    private void publish(Dependency dependency) throws IOException {
        //Record dependency jars that later used by external tools,such jdeps
        if (dependency.getDependType() == DependType.CODE_SOURCE) {
            checkResultFileWriter.write(dependency, null, null);
        }
        List<ExecutableRule> ruleList = InstanceRuleManager.getRuleInstanceList();
        for (ExecutableRule rule : ruleList) {
            if (rule.accept(dependency)) {
                ReportCheckResult checkResult = rule.execute(dependency);
                if (!checkResult.isPass()) {
                    if (checkResult.getPropagated().isEmpty()) {
                        checkResultFileWriter.write(dependency, checkResult, rule);
                    } else {
                        for (Dependency newDependency : checkResult.getPropagated()) {
                            checkResultFileWriter.write(newDependency, checkResult, rule);
                        }
                    }
                }
                List<Dependency> more = rule.propagate(dependency);
                if (!more.isEmpty()) {
                    publishMore(more);
                }
            }
        }
    }

    private void publishMore(List<Dependency> more) throws IOException {
        List<ExecutableRule> ruleList = InstanceRuleManager.getRuleInstanceList();
        for (Dependency dependency : more) {
            for (ExecutableRule rule : ruleList) {
                if (rule.accept(dependency)) {
                    ReportCheckResult checkResult = rule.execute(dependency);
                    if (!checkResult.isPass()) {
                        if (checkResult.getPropagated().isEmpty()) {
                            checkResultFileWriter.write(dependency, checkResult, rule);
                        } else {
                            for (Dependency newDependency : checkResult.getPropagated()) {
                                checkResultFileWriter.write(newDependency, checkResult, rule);
                            }
                        }
                    }
                }
            }
        }
    }
}