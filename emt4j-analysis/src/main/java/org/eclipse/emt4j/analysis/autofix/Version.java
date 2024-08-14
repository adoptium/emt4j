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


import java.util.Objects;

public class Version {
    private String version;

    private int[] versionNumbers;
    private String tail;

    public Version(String version) {
        this.version = version;
        parseVersionString();
    }

    public String getTail() {
        return tail;
    }

    private void parseVersionString() {
        if (version.equals("")) {
            versionNumbers = new int[0];
            tail = "";
            return;
        }
        String[] parts = version.split("\\.");
        versionNumbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int numberEnd;
            for (numberEnd = 0; numberEnd < part.length() && Character.isDigit(part.charAt(numberEnd)); numberEnd++) ;
            try {
                versionNumbers[i] = numberEnd == 0 ? 0 : Integer.parseInt(part.substring(0, numberEnd));
            } catch (NumberFormatException e) {
                // likely to meet integer overflow, regard it as string
                tail = part;
                break;
            }

            if (i == parts.length - 1) {
                tail = part.substring(numberEnd);
            }
        }
        if (tail == null) {
            tail = "";
        }
    }

    public int compareTo(Version other) {
        if (this.equals(other)) {
            return 0;
        }
        int compareVersionNumbers = compareVersionNumbers(versionNumbers, other.versionNumbers);
        if (compareVersionNumbers != 0) {
            return compareVersionNumbers;
        }
        int compareTail = compareTail(tail, other.tail);
        return compareTail;
    }

    public boolean shouldUpdateTo(Version other) {
        int compare = compareTo(other);
        return compare < 0;
    }

    public static boolean shouldUpdate(String from, String to) {
        return new Version(from).shouldUpdateTo(new Version(to));
    }

    private int compareVersionNumbers(int[] a, int[] b) {
        int commonLength = Math.min(a.length, b.length);
        for (int i = 0; i < commonLength; i++) {
            int diff = a[i] - b[i];
            if (diff != 0) {
                return diff;
            }
        }
        if (a.length == commonLength && b.length == commonLength) {
            return 0;
        } else if (a.length != commonLength) {
            return 1;
        } else {
            return -1;
        }
    }

    private int compareTail(String a, String b) {
        return a.compareTo(b);
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return Objects.equals(version, version1.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
