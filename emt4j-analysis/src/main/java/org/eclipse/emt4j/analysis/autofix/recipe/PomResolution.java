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

import lombok.Data;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;

// record the code structure of pom file
@Data
public class PomResolution {
    private Xml.Tag moduleSelfTag=null;
    private Xml.Tag parentTag = null;
    private Xml.Tag dependenciesTag = null;
    private List<Xml.Tag> properties = new ArrayList<>();
    private List<Xml.Tag> dependencies = new ArrayList<>();
    private List<Xml.Tag> plugins = new ArrayList<>();
    private List<Xml.Tag> managedDependencies = new ArrayList<>();
}
