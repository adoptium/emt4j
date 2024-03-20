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
package org.eclipse.emt4j.analysis.autofix;

import static org.eclipse.emt4j.analysis.common.Constant.ANY;

public interface DependencyUpdateRule {
    boolean matches(String fromGATV);

    String getToGATV();

    public static class GADontChangeDependencyUpdateRule implements DependencyUpdateRule {
        protected String[] toParts;
        protected Version toVersion;
        protected String toGATV;

        public GADontChangeDependencyUpdateRule(String gatv) {
            toGATV = gatv;
            toParts = gatv.split(":");
            toVersion = new Version(toParts[3]);
            if (toParts.length != 4) {
                throw new RuntimeException("invalid gatv: " + gatv);
            }
        }

        @Override
        public boolean matches(String fromGATV) {
            String[] fromParts = fromGATV.split(":");
            return fromParts[0].equals(toParts[0]) && fromParts[1].equals(toParts[1])
                    && (ANY.equals(fromParts[2]) || fromParts[2].equals(toParts[2]))
                    && (ANY.equals(fromParts[3]) || new Version(fromParts[3]).shouldUpdateTo(toVersion));
        }

        @Override
        public String getToGATV() {
            return toGATV;
        }
    }

    public static class GATChangeDependencyUpdateRule implements DependencyUpdateRule {
        protected GADontChangeDependencyUpdateRule dontChaneRule;
        protected String[] changeFromGAT;

        public GATChangeDependencyUpdateRule(String fromGAT, String toGATV) {
            dontChaneRule = new GADontChangeDependencyUpdateRule(toGATV);
            changeFromGAT = fromGAT.split(":");
            if (changeFromGAT.length != 3) {
                throw new RuntimeException("invalid gat: " + fromGAT);
            }
        }

        @Override
        public boolean matches(String fromGATV) {
            String[] fromParts = fromGATV.split(":");
            boolean matchChangeGA = fromParts[0].equals(changeFromGAT[0])
                    && fromParts[1].equals(changeFromGAT[1])
                    && (ANY.equals(fromParts[2]) || fromParts[2].equals(changeFromGAT[2]));

            return dontChaneRule.matches(fromGATV) || matchChangeGA;
        }

        @Override
        public String getToGATV() {
            return dontChaneRule.getToGATV();
        }
    }

    public static class GADontChangeWithSpecialMark extends GADontChangeDependencyUpdateRule {
        private String mark;
        public GADontChangeWithSpecialMark(String gatv, String mark) {
            super(gatv);
            this.mark = mark;
        }

        @Override
        public boolean matches(String fromGATV) {
            return super.matches(fromGATV)  && fromGATV.split(":")[3].contains(mark);
        }
    }
}

