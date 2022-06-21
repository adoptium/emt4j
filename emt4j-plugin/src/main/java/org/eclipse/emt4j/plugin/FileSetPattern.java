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
package org.eclipse.emt4j.plugin;

import java.io.File;
import java.util.regex.Pattern;

/**
 * A tool that converts maven file rule to java regular expressions.
 */
public class FileSetPattern {
    private final String regex;
    private final Pattern pattern;

    public FileSetPattern(String filesetPattern) {
        regex = toJavaRegexPattern(filesetPattern);
        pattern = Pattern.compile(regex);
    }

    private String toJavaRegexPattern(String filesetPattern) {
        if (filesetPattern.endsWith(File.separator)) {
            filesetPattern = filesetPattern + "**";
        }

        String[] fileSetPart = filesetPattern.split(Pattern.quote(File.separator));
        StringBuilder regex = new StringBuilder(filesetPattern.length() + 16);
        regex.append('^');
        for (int i = 0; i < fileSetPart.length; i++) {
            if ("**".equals(fileSetPart[i])) {
                regex.append(".*");
            } else if ("*".equals(fileSetPart[i])) {
                regex.append("[^" + File.separator + "]*");
                if (i != fileSetPart.length - 1) {
                    regex.append(Pattern.quote(File.separator));
                }
            } else {
                if (fileSetPart[i].indexOf('*') != -1) {
                    for (int j = 0; j < fileSetPart[i].length(); j++) {
                        if ('*' == fileSetPart[i].charAt(j)) {
                            regex.append(".*");
                        } else {
                            regex.append(Pattern.quote(String.valueOf(fileSetPart[i].charAt(j))));
                        }
                    }
                } else {
                    regex.append(Pattern.quote(fileSetPart[i]));
                }
                if (i != fileSetPart.length - 1) {
                    regex.append(Pattern.quote(File.separator));
                }
            }
        }
        regex.append('$');

        return regex.toString();
    }

    public boolean matches(String givenFile) {
        return pattern.matcher(givenFile).find();
    }
}
