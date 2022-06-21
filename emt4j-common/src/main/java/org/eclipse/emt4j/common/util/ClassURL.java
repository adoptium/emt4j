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

import org.eclipse.emt4j.common.util.refclass.Handler;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Create a new URL represent a class and method in this class.
 * The format is : refclass://${file}/${className}/${methodName}
 */
public class ClassURL {
    private static final String CLASS_PROTOCOL = "refclass";

    public static URL create(String file, String className, String methodName) {
        try {
            // why already call registerUrlProtoclHandler, there is new Handler explicitly?
            // The reason is when run as maven plugin, maven plugin has different classloader hierarchy, it cannot load the 'org.eclipse.emt4j.common.util.refclass.Handler'
            return new URL(CLASS_PROTOCOL, null, -1, "//" + emptyIfNull(file) + "/" + className + "/" + emptyIfNull(methodName), new Handler());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL create(String className, String methodName) {
        return create(null, className, methodName);
    }

    public static void registerUrlProtocolHandler() {
        String handlers = System.getProperty("java.protocol.handler.pkgs", "");
        System.setProperty("java.protocol.handler.pkgs",
                "".equals(handlers) ? "org.eclipse.emt4j.common.util" : (handlers + "|" + "org.eclipse.emt4j.common.util"));
        resetCachedUrlHandlers();
    }

    private static void resetCachedUrlHandlers() {
        try {
            URL.setURLStreamHandlerFactory(null);
        } catch (Error error) {
        }
    }

    private static String emptyIfNull(String file) {
        return file == null ? "" : file;
    }
}
