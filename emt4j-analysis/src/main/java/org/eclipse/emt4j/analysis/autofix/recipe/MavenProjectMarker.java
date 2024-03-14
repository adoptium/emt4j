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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.apache.maven.project.MavenProject;
import org.openrewrite.marker.Marker;

import java.util.UUID;

@With
@AllArgsConstructor
@Data
public class MavenProjectMarker implements Marker {
    private UUID id = UUID.randomUUID();
    private MavenProject project;

    public MavenProjectMarker(MavenProject project) {
        this.project = project;
    }
}
