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

/**
 * The context of a problem.The user can find where the problem occurs.
 */
public class IssueContext {
    private List<String> contextDesc = new ArrayList<>();
    private List<String> stackTrace = new ArrayList<>();

    public List<String> getContextDesc() {
        return contextDesc;
    }

    public void setContextDesc(List<String> contextDesc) {
        this.contextDesc = contextDesc;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueContext that = (IssueContext) o;

        if (contextDesc != null ? !contextDesc.equals(that.contextDesc) : that.contextDesc != null) return false;
        return stackTrace != null ? stackTrace.equals(that.stackTrace) : that.stackTrace == null;
    }

    @Override
    public int hashCode() {
        int result = contextDesc != null ? contextDesc.hashCode() : 0;
        result = 31 * result + (stackTrace != null ? stackTrace.hashCode() : 0);
        return result;
    }
}
