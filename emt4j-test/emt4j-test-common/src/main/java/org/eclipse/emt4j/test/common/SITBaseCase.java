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

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.MainResultDetail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SIT(System Integration Test)
 * All SIT test cases must extend with SITBaseCase.
 */
public abstract class SITBaseCase {

    public abstract void verify(JsonReport jsonReport);

    /**
     * Add codes that are incompatible with JDK.
     */
    public abstract void run() throws Exception;

    /**
     * when the target jar/class is dynamic, sub class should implement this.
     */
    public void prepareDynamicTestTarget(String workDir) throws IOException {
        throw new JdkMigrationException("Not implemented!");
    }

    protected void assertTrue(boolean condition) {
        if (!condition) {
            throw new JdkMigrationException("assertTrue failed!");
        }
    }

    protected void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new JdkMigrationException("assertTrue failed!Message: " + msg);
        }
    }

    protected void assertFalse(boolean condition, String msg) {
        if (condition) {
            throw new JdkMigrationException("assertFalse failed!Message: " + msg);
        }
    }

    protected void assertEmpty(List<?> list, String msg) {
        if (!isEmpty(list)) {
            throw new JdkMigrationException("assertEmpty failed!Message: " + msg);
        }
    }

    protected void assertNotEmpty(List<?> list, String msg) {
        if (isEmpty(list)) {
            throw new JdkMigrationException("assertNotEmpty failed!Message : " + msg);
        }
    }

    protected boolean matchAny(JsonReport jsonReport, String resultCode) {
        return jsonReport.getResultDetailList().stream().filter((d) -> d.getMainResultCode().equals(resultCode))
                .findAny().isPresent();
    }

    protected List<MainResultDetail> filter(JsonReport jsonReport, String resultCode) {
        return jsonReport.getResultDetailList().stream().filter((d) -> d.getMainResultCode().equals(resultCode))
                .collect(Collectors.toList());
    }


    private boolean isEmpty(List<?> list) {
        return null == list || list.isEmpty();
    }

}
