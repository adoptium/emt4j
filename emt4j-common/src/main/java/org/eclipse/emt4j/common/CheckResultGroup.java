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
package org.eclipse.emt4j.common;

import java.util.ArrayList;
import java.util.List;

public class CheckResultGroup {

    private List<CheckResultContext> checkResultContextList = new ArrayList<>();

    private List<String> stackTrace;

    private List<CallFrame> diffStackTraceFrame = new ArrayList<>();

    public List<CheckResultContext> getCheckResultContextList() {
        return checkResultContextList;
    }

    public void setCheckResultContextList(List<CheckResultContext> checkResultContextList) {
        this.checkResultContextList = checkResultContextList;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<CallFrame> getDiffStackTraceFrame() {
        return diffStackTraceFrame;
    }

    public void setDiffStackTraceFrame(List<CallFrame> diffStackTraceFrame) {
        this.diffStackTraceFrame = diffStackTraceFrame;
    }

    public static CheckResultGroup createBase(CheckResultContext checkResultContext, List<String> stackTrace) {
        CheckResultGroup group = new CheckResultGroup();
        group.getCheckResultContextList().add(checkResultContext);
        group.setStackTrace(stackTrace);
        return group;
    }

    public void appendStackTraceDifference(CheckResultContext toCompare, int diffPos, String diffFrame) {
        checkResultContextList.add(toCompare);
        diffStackTraceFrame.add(new CallFrame(diffPos, diffFrame));
    }
}
