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

import org.eclipse.emt4j.common.rule.model.JarFileInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Because not all jar file name with a uniform rule,so the util will try different pattern.
 */
public class JarFileInfoUtil {

    private static PatternWithNumber[] ALL_PATTERN = new PatternWithNumber[]{
            new PatternWithNumber(4, Pattern.compile(".*(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+).*")),
            new PatternWithNumber(3, Pattern.compile(".*(\\d+)\\.(\\d+)\\.(\\d+).*")),
            new PatternWithNumber(2, Pattern.compile(".*(\\d+)\\.(\\d+).*")),
            new PatternWithNumber(1, Pattern.compile(".*(\\d+).*")),
    };

    public static Optional<JarFileInfo> match(String jarFilePath) {
        Optional<String> jarFileName = stripPath(jarFilePath);
        if (!jarFileName.isPresent()) {
            return Optional.empty();
        }

        JarFileInfo jarFileInfo = new JarFileInfo();
        for (PatternWithNumber pn : ALL_PATTERN) {
            Matcher m = pn.pattern.matcher(jarFileName.get());
            if (m.find()) {
                List<String> version = new ArrayList<>();
                for (int i = 1; i <= pn.groupCount; i++) {
                    version.add(m.group(i));
                }

                String prefix = jarFileName.get().substring(0, m.start(1));
                jarFileInfo.setVersion(jarFileName.get().substring(m.start(1), m.end(pn.groupCount)));
                sortArtifactFragments(prefix, jarFileInfo);
                jarFileInfo.setJarFileName(jarFileName.get());
                return Optional.of(jarFileInfo);
            }
        }
        return Optional.empty();
    }

    public static void sortArtifactFragments(String artifactId, JarFileInfo jarFileInfo) {
        //split by special char
        String[] part = (artifactId).split("\\p{Punct}");

        List<String> artifact = new ArrayList<>();
        for (String p : part) {
            if (p == null || "".equals(p) || isNumeric(p)) {
                continue;
            }

            //eclipse jar like this: org.eclipse.osgi_3.13.200.v20181130-2106.jar
            //we should ignore "v20181130" part
            if ((p.charAt(0) == 'v' || p.charAt(0) == 'V')
                    && isNumeric(p.substring(1))) {
                continue;
            }

            artifact.add(p);
        }

        jarFileInfo.setArtifactFragments(artifact.toArray(new String[artifact.size()]));
        Collections.sort(artifact);
        jarFileInfo.setOrderedArtifactFragments(artifact.toArray(new String[artifact.size()]));
    }

    public static String[] sortArtifactFragments(String artifactId) {
        //split by special char
        String[] part = (artifactId).split("\\p{Punct}");

        List<String> artifact = new ArrayList<>();
        for (String p : part) {
            if (p == null || "".equals(p) || isNumeric(p)) {
                continue;
            }

            //eclipse jar like this: org.eclipse.osgi_3.13.200.v20181130-2106.jar
            //we should ignore "v20181130" part
            if ((p.charAt(0) == 'v' || p.charAt(0) == 'V')
                    && isNumeric(p.substring(1))) {
                continue;
            }

            artifact.add(p);
        }

        Collections.sort(artifact);
        return artifact.toArray(new String[artifact.size()]);
    }


    private static Optional<String> stripPath(String jarFilePath) {
        if (null == jarFilePath || "".equals(jarFilePath)) {
            return Optional.empty();
        }
        int end = jarFilePath.lastIndexOf(".jar");
        if (-1 == end) {
            return Optional.empty();
        }

        int start = end;
        while (start > 0) {
            char c = jarFilePath.charAt(start);
            if (c == '/' || c == '\\') {
                //skip / or \
                start++;
                break;
            }
            start--;
        }
        return Optional.of(jarFilePath.substring(start, end));
    }

    private static class PatternWithNumber {
        Pattern pattern;
        int groupCount;

        public PatternWithNumber(int groupCount, Pattern pattern) {
            this.pattern = pattern;
            this.groupCount = groupCount;
        }
    }

    public static boolean isNumeric(final CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
