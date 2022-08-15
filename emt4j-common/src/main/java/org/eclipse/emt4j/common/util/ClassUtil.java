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
package org.eclipse.emt4j.common.util;

import java.util.Optional;
import java.util.Set;

public class ClassUtil {

    public static String buildMethodIdentifier(String className, String methodName, String desc) {
        return className + "." + methodName + desc;
    }

    public static String buildMethodIdentifierNoDesc(String className, String methodName) {
        return className + "." + methodName;
    }


    public static Optional<String> maxLongMatch(String className, Set<String> allPackages) {
        if (null == className || "".equals(className)) {
            return Optional.empty();
        } else {
            int index = className.lastIndexOf('.');
            if (-1 == index) {
                return Optional.empty();
            }
            do {
                if (allPackages.contains(className.substring(0, index))) {
                    return Optional.of(className.substring(0, index));
                }
                index = className.substring(0, index).lastIndexOf('.');
            } while (index != -1);
            return Optional.empty();
        }
    }

    public static Optional<String> getPackage(String className) {
        if (className == null || "".equals(className)) {
            return Optional.empty();
        }
        int index = className.lastIndexOf('.');
        if (-1 == index) {
            return Optional.empty();
        }
        return Optional.of(className.substring(0, index));
    }
}
