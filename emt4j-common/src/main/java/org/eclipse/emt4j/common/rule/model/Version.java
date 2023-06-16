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

import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * As a tool for MVEL2 interpreter.
 * Compare between two versions with a string representation.
 * Like compare "1.1.0" with "1.1.2".
 */
public class Version {
    private String version;

    public Version(String version) {
        this.version = version;
    }

    public boolean lt(String toCompare) {
        return doCompare(version, toCompare, (a, b) -> a.intValue() < b.intValue());
    }

    public boolean le(String toCompare) {
        return doCompare(version, toCompare, (a, b) -> a.intValue() <= b.intValue());
    }

    public boolean eq(String toCompare) {
        return version.equals(toCompare);
    }

    public boolean ne(String toCompare) {
        return !eq(toCompare);
    }

    public boolean ge(String toCompare) {
        return doCompare(version, toCompare, (a, b) -> a.intValue() >= b.intValue());
    }

    public boolean gt(String toCompare) {
        return doCompare(version, toCompare, (a, b) -> a.intValue() > b.intValue());
    }

    private boolean doCompare(String aVersion, String bVersion, BiPredicate<Integer, Integer> predicate) {
        int[] a = toInt(aVersion);
        int[] b = toInt(bVersion);

        //if length is difference,align with same length.
        //padding with 0 as version
        if (a.length != b.length) {
            //align with same length
            if (a.length > b.length) {
                b = align(a, b);
            } else {
                a = align(b, a);
            }
        }

        for (int i = 0; i < a.length; i++) {
            if (predicate.test(a[i], b[i])) {
                // we should continue compare the next if current value is equal.
                if (a[i] == b[i] && i != a.length - 1) {
                    continue;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private int[] align(int[] alignTo, int[] needAlign) {
        int[] newArray = new int[alignTo.length];
        Arrays.fill(newArray, 0);
        System.arraycopy(needAlign, 0, newArray, 0, needAlign.length);
        return newArray;
    }


    private int[] toInt(String version) {
        String[] array = version.split("\\.");
        int[] intArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            intArray[i] = Integer.parseInt(array[i]);
        }
        return intArray;
    }

}
