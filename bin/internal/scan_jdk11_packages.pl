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

use warnings FATAL => 'all';
use strict;
use File::Find;
use File::Basename;

if (@ARGV != 1) {
    print STDERR "scan_jdk11_packages.pl [jdk11 source dir]";
    exit(1);
}
my $jdk11_src_dir = $ARGV[0];
if (!-d $jdk11_src_dir) {
    print STDERR "$jdk11_src_dir not exist!";
    exit(1);
}

my %package_to_file;

find(\&wanted, $jdk11_src_dir);

my @packages = sort keys %package_to_file;
foreach my $package (@packages) {
    print "$package\n";
}

sub wanted {
    if ($File::Find::name =~ /\.java$/) {
        parse_java_file($File::Find::name);
    }
    return;
}

sub parse_java_file() {
    my ($file) = @_;
    open(FILE, "<", "$file") || die "[parse_java_file] cannot open the file: $!\n";
    my @line = <FILE>;
    foreach my $line (@line) {
        if ($line =~ m/^package\s+([\w.]+)\s*;\s*$/) {
            #print dirname($file),"==>$1\n";
            $package_to_file{$1} = dirname($file);
            last;
        }
    }
    close FILE;
}

