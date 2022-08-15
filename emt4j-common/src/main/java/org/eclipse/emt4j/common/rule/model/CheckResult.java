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
package org.eclipse.emt4j.common.rule.model;

import org.eclipse.emt4j.common.Dependency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckResult implements Serializable {
    private boolean pass;
    private Map<String, Object> context;
    public static final CheckResult PASS = new CheckResult(true);
    public static final CheckResult FAIL = new CheckResult(false);
    public transient List<Dependency> propagated = new ArrayList<>();

    public CheckResult(boolean pass, Map<String, Object> context) {
        this.pass = pass;
        this.context = context;
    }

    public CheckResult(boolean pass) {
        this.pass = pass;
    }

    public CheckResult() {
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public static CheckResult pass() {
        return new CheckResult(true);
    }

    public static CheckResult pass(String key, Object value) {
        Map<String, Object> context = new HashMap<>();
        context.put(key, value);
        return new CheckResult(true, context);
    }

    public static CheckResult pass(Map<String, Object> context) {
        return new CheckResult(true, context);
    }

    public static CheckResult fail() {
        return new CheckResult(false);
    }

    public static CheckResult fail(String key, Object value) {
        Map<String, Object> context = new HashMap<>();
        context.put(key, value);
        return new CheckResult(false, context);
    }

    public static CheckResult fail(Map<String, Object> context) {
        return new CheckResult(false, context);
    }

    public List<Dependency> getPropagated() {
        return propagated;
    }

    public void setPropagated(List<Dependency> propagated) {
        this.propagated = propagated;
    }
}
