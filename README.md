<!--
    Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation

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

# Eclipse Migration Toolkit for Java (EMT4J)

EMT4J is a project that aims to simplify the Java version migration. At the moment, this project focuses on three LTS
(i.e. Long-Term-Support) versions: 8, 11, and 17. Therefore, if you want to migrate your application running on JDK 8/11
to JDK 11/17, then this project is for you.

EMT4J supports statically checking application artifacts including the project's classes and dependencies. It also
supports running as a Java agent to perform runtime checking. During the checking process, EMT4J collects compatibility
problems and outputs a report finally. It currently supports HTML, TEXT, and JSON formats. Users transform the project
according to the report and finally complete the migration of the Java version.

## How to use

### Maven Plugin

#### Find compatibility problems existing in a Maven project

Add the following configuration to pom.xml (root pom.xml if a multi-module project): 

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.eclipse.emt4j</groupId>
            <artifactId>emt4j-maven-plugin</artifactId>
            <version>0.8.0</version>
            <executions>
                <execution>
                    <phase>process-test-classes</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <fromVersion>8</fromVersion>
                <toVersion>11</toVersion>
                <outputFile>report.html</outputFile>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Then run the following command:

```shell
$ mvn process-test-classes
```

EMT4J's report will be generated in the project directory.

Users can also run the following command directly without modifying pom.xml:

```shell
# run with default configurations
$ mvn process-test-classes org.eclipse.emt4j:emt4j-maven-plugin:0.8.0:check
```

``` shell
# specify outputFile and priority by -D
$ mvn process-test-classes org.eclipse.emt4j:emt4j-maven-plugin:0.8.0:check -DoutputFile=emt4j-report.html -Dpriority=p1
```

Configurations:

- `fromVersion`: the JDK version that the project currently uses. 8 and 11 are supported, and 8 is as default.

- `toVersion`: the target JDK version. 11 and 17 are supported, and 11 is as default.

- `outputFile`: the destination of EMT4J's report. The default is report.html.

- `priority`: the minimum rule priority. p1, p2, p3 and p4 are supported. The default is not set. 

- `verbose`: print more detailed messages if true.

As mentioned earlier, EMT4J supports running as a Java agent. To leverage it in the test process you need to add the
following configuration:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.eclipse.emt4j</groupId>
            <artifactId>emt4j-maven-plugin</artifactId>
            <version>0.8.0</version>
            <executions>
                <execution>
                    <phase>initialize</phase>
                    <goals>
                        <goal>inject-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <phase>test</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <fromVersion>8</fromVersion>
                <toVersion>11</toVersion>
                <outputFile>report.html</outputFile>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Then run the following command:

```shell
$ mvn test
```

#### Find compatibility problems existing in specified files (classes, JAR, or directory)

``` shell
$ mvn org.eclipse.emt4j:emt4j-maven-plugin:0.8.0:check-files -Dfiles=...
```

### Java agent and CLI

First, you need to download the build from the link below:

[Releases](https://github.com/adoptium/emt4j/releases)

The build includes two Java agents, command line tools, a maven plugin, and required dependencies.

#### Use Java agent standalone to perform runtime checking

- Migration Java version from 8 to 11:

   ```shell
   $ java -javaagent:<path-to-emt4j-build>/lib/agent/emt4j-agent-jdk8-0.8.0.jar=to=11,file=jdk8to11.dat
   ```

- Migration Java version from 11 to 17:

   ```shell
   $ java -javaagent:<path-to-emt4j-build>/lib/agent/emt4j-agent-jdk11-0.8.0.jar=to=17,file=11to17.dat
   ```

- Migration Java version from 8 to 17:

   ```shell
   $ java -javaagent:<path-to-emt4j-build>/lib/agent/emt4j-agent-jdk8-0.8.0.jar=to=17,file=jdk8to17.dat
   ```

- The Java agent will record the compatibility problems found during running into the file. This file is a binary file,
  you need to transform it to a readable format by the command:

   ```shell
   $ sh <path-to-emt4j-build>/bin/analysis.sh -o report.html <file generated by agent>
   ```

Java agent options:

- `file` : the output file path. The default is `emt4j-${yyyyMMddHHmmss}.dat` in the current working directory.

- `to` : the target JDK version.

- `priority` : the minimum rule priority. p1, p2, p3 and p4 are supported. The default is not set.

#### Use CLI

The build contains a script named `analysis` located in the directory bin (.sh is for Mac or Linux users and .bat is for
Windows users).

You can use this script to scan compatibility problems that exist in classes, JARS, and directories that contain classes
and JARS.

- Migration Java version from 8 to 11:

   ```shell
   $ sh bin/analysis.sh -f 8 -t 11 -o report.html <files...>
   ```

- Check JVM options
   ```shell
   $ sh bin/analysis.sh -f 8 -t 17 <file that contains JVM options>
   ```

Options:

- `-f` : the JDK version that the project currently uses. 8 and 11 are supported.

- `-t` : the target JDK version. 11 and 17 are supported.

- `-priority` : the minimum rule priority. p1, p2, p3 and p4 are supported. The default is not set.

- `-p` : the report format, HTML, TXT, and JSON are supported. Default is HTML

- `-o` : the output file name (the default name is 'report').

- `-v` : print more detailed messages.

## Other Documents

[Development Guide](DEVELOPMENT_GUIDE.md)
