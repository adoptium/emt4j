#! /usr/bin/perl

# Copyright (c) 2022 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0

## When test emt4j,there many options to test. Enumerate all these options is tedious
## so what this perl script to do .
## Usage: gen_all_testconfig.pl -p /path/to/emt4j -t /path/to/check1 -t /path/to/check2 ....
## -p : the home of emt4j tool
## -t : to check target,only valid for generate analysis test cases.

use strict;
use warnings FATAL => 'all';

my $emt4j_home = "";
my @check_targets;

my @agent_output;
my @analysis_output;
my @plugin_output;
my @jdk_from_to = (
    [ "8", "11" ],
    [ "11", "17" ],
    [ "8", "17" ]
    [ "8", "21" ]
    [ "11", "21" ]
    [ "17", "21" ]
);

sub print_output;

for my $i (0 .. $#ARGV) {
    if ($ARGV[$i] eq "-p") {
        $emt4j_home = $ARGV[$i + 1];
    }
    elsif ($ARGV[$i] eq "-t") {
        push(@check_targets, $ARGV[$i + 1]);
    }
}

for my $i (0 .. $#jdk_from_to) {
    gen_for_agent($jdk_from_to[$i][0], $jdk_from_to[$i][1]);
    gen_for_analysis($jdk_from_to[$i][0], $jdk_from_to[$i][1]);
    gen_for_mvn_plugin($jdk_from_to[$i][0], $jdk_from_to[$i][1]);
}

print_output("Agent config         ", \@agent_output);
print_output("Analysis shell script", \@analysis_output);
print_output("Maven config         ", \@plugin_output);


sub gen_for_agent {
    my ($from, $to) = @_;
    my $output_file = "agentoutput-${from}-${to}.dat";
    push(@agent_output, "-javaagent:${emt4j_home}/lib/agent/emt4j-agent-jdk${from}-0.1.jar=file=agentoutput-${from}-${to}.dat,to=${to}");
    push(@agent_output, "sh ${emt4j_home}/bin/analysis.sh $output_file");
    push(@agent_output, "\n");
}

sub gen_for_analysis {
    my ($from, $to) = @_;
    my @format = ("html", "txt", "json");
    foreach my $f (@format) {
        push(@analysis_output, "sh ${emt4j_home}/bin/analysis.sh -f $from -t $to -p $f -o analysisoutput-${from}-${to}.${f} " . join(' ', @check_targets));
    }
}

sub gen_for_mvn_plugin {
    my ($from, $to) = @_;
    my $config = qq {
    <plugin>
        <groupId>org.eclipse.emt4j</groupId>
        <artifactId>emt4j-maven-plugin</artifactId>
        <version>0.1</version>
        <executions>
            <execution>
                <phase>process-classes</phase>
                <goals>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <fromVersion>$from</fromVersion>
            <toVersion>$to</toVersion>
            <targetJdkHome>/Library/Java/JavaVirtualMachines/jdk-17.0.1.jdk/Contents/Home</targetJdkHome>
        </configuration>
    </plugin>
    };
    push(@plugin_output,$config);
}

sub print_output {
    my ($title, $ref_to_array) = @_;
    print "===================== $title =================\n";
    foreach my $content (@$ref_to_array) {
        print $content . "\n";
    }
    print "\n\n";
}



