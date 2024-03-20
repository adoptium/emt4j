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
package org.eclipse.emt4j.analysis.autofix.recipe;

import org.eclipse.emt4j.analysis.autofix.XmlTagMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class XmlTagHelper {
    private XmlTagHelper() {
    }

    public static boolean matchTag(Xml.Tag parent, String name, String target) {
        if ("*".equals(target)) {
            return true;
        }
        Optional<String> childValue = parent.getChildValue(name);
        return childValue.map(s -> s.equals(target)).orElse(false);
    }

    private static final Pattern CONDITION_PATTERN =  Pattern.compile("([-\\w]+)\\[([-\\w]+)='([-\\w]+)'\\]");

    // this method helps find descendants in a complicated path. The path
    // supports XPath style format like /executions/execution[phase='package']/configuration/tasks/unzip
    public static List<Xml.Tag> findDescendants(Xml.Tag root, String path) {
        List<Xml.Tag> tags = Collections.singletonList(root);
        String[] parts = path.substring(1).split("/");
        for (String part : parts) {
            int indexLeftBracket = part.indexOf('[');
            boolean hasCondition=indexLeftBracket >= 0;
            String tagName = hasCondition ? part.substring(0, indexLeftBracket) : part;
            Predicate<Xml.Tag> condition = (tag) -> true;
            if (hasCondition) {
                Matcher matcher = CONDITION_PATTERN.matcher(part);
                if (matcher.matches()) {
                    String subTag = matcher.group(2);
                    String subTagValue = matcher.group(3);
                    condition = (tag) -> matchTag(tag, subTag, subTagValue);
                }
            }
            tags = tags.stream()
                    .flatMap(tag -> tag.getChildren(tagName).stream())
                    .filter(condition)
                    .collect(Collectors.toList());
        }
        return tags;
    }

    public static Set<Xml.Tag> findTags(Xml xml, XmlTagMatcher matcher) {
        Set<Xml.Tag> tags = new HashSet<>();
        new MyMavenVisitor<Set<Xml.Tag>>() {
            @Override
            public Xml visitTag(Xml.Tag tag, Set<Xml.Tag> ts) {
                if (matcher.matches(tag)) {
                    ts.add(tag);
                }
                return super.visitTag(tag, ts);
            }
        }.visit(xml, tags);
        return tags;
    }

    public static Xml.Tag findSingleTag(Xml xml, XmlTagMatcher matcher) {
        Set<Xml.Tag> tags = findTags(xml,matcher);
        return tags.stream().findAny().orElse(null);
    }
}
