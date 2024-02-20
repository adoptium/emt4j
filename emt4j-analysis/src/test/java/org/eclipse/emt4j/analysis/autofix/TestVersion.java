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

import org.junit.Assert;
import org.junit.Test;

public class TestVersion {

    @Test
    public void testVersionCompare() {
        Assert.assertTrue(new Version("1.2.1").shouldUpdateTo(new Version("1.2.3")));
        Assert.assertFalse(new Version("1.2.3").shouldUpdateTo(new Version("1.2.1")));
        Assert.assertTrue(new Version("1.2.1").shouldUpdateTo(new Version("2.2.3")));
        Assert.assertFalse(new Version("2.2.3").shouldUpdateTo(new Version("1.2.1")));
        Assert.assertTrue(new Version("1.2").shouldUpdateTo(new Version("1.2.3")));
        Assert.assertFalse(new Version("1.2.3").shouldUpdateTo(new Version("1.2")));
        Assert.assertTrue(new Version("1.2.1").shouldUpdateTo(new Version("1.2.1-update")));
        Assert.assertFalse(new Version("1.2.1-update").shouldUpdateTo(new Version("1.2.1")));
        Assert.assertTrue(new Version("1.2.1-aaaaaa").shouldUpdateTo(new Version("1.2.1-update")));
        Assert.assertFalse(new Version("1.2.1-update").shouldUpdateTo(new Version("1.2.1-aaaaaaa")));
        Assert.assertTrue(new Version("1.2.1").shouldUpdateTo(new Version("1.2.1.1234567890123456789"))); //overflow
        Assert.assertFalse(new Version("1.2.1.1234567890123456789").shouldUpdateTo(new Version("1.2.1"))); //overflow
        Assert.assertFalse(new Version("1.2.1").shouldUpdateTo(new Version("1.2.1")));
        Assert.assertTrue(new Version("1.0.25.2018030201").shouldUpdateTo(new Version("1.0.26-jdk11")));
        // pandora boot does not use "." as delimiter, but we can directly compare string
        Assert.assertTrue(new Version("2018-09-stable").shouldUpdateTo(new Version("2023-03-release")));
        Assert.assertFalse(new Version("2023-09-stable").shouldUpdateTo(new Version("2023-03-release")));
    }
}
