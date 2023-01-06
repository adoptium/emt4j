<!--
    Copyright (c) 2023 Contributors to the Eclipse Foundation

    See the NOTICE file(s) distributed with this work for additional
    information regarding copyright ownership.

    This program and the accompanying materials are made available under the
    terms of the Apache License, Version 2.0 which is available at
    https://www.apache.org/licenses/LICENSE-2.0.

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    SPDX-License-Identifier: Apache-2.0
 -->

# Development Guide

## Preparation

### Workflow

![workflow](workflow.png)

### How to Build

1. Install JDK 8 and 11 and configure them properly  in the Maven's toolchains.xml:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <toolchains>
       <!-- JDK toolchains -->
       <toolchain>
           <type>jdk</type>
           <provides>
               <version>8</version>
               <vendor>openjdk</vendor>
           </provides>
           <configuration>
               <jdkHome>path-to-jdk8-home</jdkHome>
           </configuration>
       </toolchain>
       <toolchain>
           <type>jdk</type>
           <provides>
               <version>11</version>
               <vendor>openjdk</vendor>
           </provides>
           <configuration>
               <jdkHome>path-to-jdk11-home</jdkHome>
           </configuration>
       </toolchain>
   </toolchains>
   ```

2. Package:
 
   ```
   $ mvn clean package -Prelease
   ```
   
3. Then emt4j-${version}.zip will be generated at emt4j-assembly/target.

### Integration Test

```
$ mvn clean verify -Ptest
```

## Modify an existing rule

### Incompatible jar

1. Open "incompatible_jar.cfg" in "emt4j-common" module.

2. The "incompatible_jar.cfg" is a CSV file, the first column is the artifact name, and the second column is the rule that describes which version can work.

3. If you need to customize the description for the jar in the final report file, you need to add a resource file:

    1. The resource file should add at  "emt4j-common/src/main/resources/default/i18n"

    2. Add a new resource bundle named "INCOMPATIBLE_JAR_${name}", the "${name}" is the artifact name in step 2.

    3. Each resource always contains these keys: "title", "description", and "solution".
 
4. Re-build emt4j.

### Other rules

Each rule contains these parts:

1. Rule description in "emt4j-common/src/main/resources/default/rule/11to17/rule.xml" or "emt4j-common/src/main/resources/default/rule/8to11/rule.xml".

2. (Optional) Rule data file which contains information needed by the rule implementation.

3. Rule implementation file.

4. Resource bundle for the error code of the rule.

For example, the "jvm-option" rule, the data file is "jvmoptions.cfg", the result code is "VM_OPTION".
If you want to add/modify/delete some options, you can modify the data file "jvmoptions.cfg".
If you want to customize the description in the report file, you can modify the resource bundle named with "VM_OPTION".
If you want to change the implementation of this rule, you can modify the implementation class "org.eclipse.emt4j.common.rule.impl.JvmOptionRule".

```xml
    <rule desc="JVM Option not compatible" type="jvm-option" jvm-option-file="jvmoptions.cfg"
          result-code="VM_OPTION" priority="p1">
        <support-modes>
            <mode>agent</mode>
            <mode>class</mode>
        </support-modes>
    </rule>
```

## Add a new rule

### Add rule description

Add a XML node "rule" in "rule.xml" which located at "emt4j-common" module.
The "rule" node contains:

1. Human-readable description that helps others understand the function of the rule.

2. Rule type used to make a connection with the implementation of the rule.

3. (Optional) Rule data file contains more information needed for the implementation.

4. The "result-code" makes a connection with the report file. Each result code contains a corresponding resource bundle with the same name.

5. The "priority" decides the sequence of check results in the report file.

6. The "support-modes" tell this rule is suitable for javaagent or static analysis.

### Add rule implementation

1. If the rule only applies with javaagent, the rule should add to the "emt4j-agent-jdk8" or "emt4j-agent-jdk11" or "emt4j-agent-common" module.

2. If the rule both applies with javaagent and class, the rule should add to "emt4j-common".

3. The rule need extend "org.eclipse.emt4j.common.rule.ExecutableRule".

4. The rule need add an annotation "org.eclipse.emt4j.common.RuleImpl".

### Rule registration

1. Agent Rule(JDK 8):  `org.eclipse.emt4j.agent.jdk8.MainAgent`

2. Agent Rule(JDK 11): `org.eclipse.emt4j.agent.jdk11.MainAgent`

3. Class Rule： `org.eclipse.emt4j.analysis.AnalysisExecutor`

### Add resource bundle

Suppose the result code is "BAR", add a new resource bundle named "BAR" at "emt4j-common/src/main/resources/default/i18n".

### Add integration test case

1. Add the test case to the "emt4j-test-jdk8" or "emt4j-test-jdk11" module.

2. The test case class name must end with "Test" and extend 'org.eclipse.emt4j.test.common.SITBaseCase', then implement the 'run' and 'verify' methods.

    1. The "run" method contains the code that has an incompatible problem.

    2. The "verify" method test if the check result matches the expected.

3. Add "org.eclipse.emt4j.test.common.TestConf" annotation for the new test case.