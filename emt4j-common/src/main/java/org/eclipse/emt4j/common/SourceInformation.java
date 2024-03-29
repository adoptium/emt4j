/********************************************************************************
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class SourceInformation implements Serializable {

    private String identifier;

    private boolean isDependency;

    private String[] extras;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isDependency() {
        return isDependency;
    }

    public void setDependency(boolean dependency) {
        isDependency = dependency;
    }

    public String[] getExtras() {
        return extras;
    }

    public void setExtras(String[] extras) {
        this.extras = extras;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInformation that = (SourceInformation) o;
        return isDependency == that.isDependency && Objects.equals(identifier, that.identifier) && Arrays.equals(extras, that.extras);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(identifier, isDependency);
        result = 31 * result + Arrays.hashCode(extras);
        return result;
    }

    @Override
    public String toString() {
        return "SourceInformation{" +
                "name='" + identifier + '\'' +
                ", isDependency=" + isDependency +
                ", extras=" + Arrays.toString(extras) +
                '}';
    }
}
