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
    print STDERR "build_module_info.pl [jdk11 source dir]";
    exit(1);
}
my $jdk11_src_dir = $ARGV[0];
if (!-d $jdk11_src_dir) {
    print STDERR "$jdk11_src_dir not exist!";
    exit(1);
}

my %path_to_module_name;
my %path_to_package;

my %package_to_module;

find(\&wanted, $jdk11_src_dir);
&longest_match_package_to_module();

my @packages = sort keys %package_to_module;
print "#export package,module\n";
foreach my $package (@packages){
    print "$package",",",$package_to_module{$package},"\n";
}

sub wanted {
    if ($File::Find::name =~ /\/module-info.java$/) {
        parse_module_info($File::Find::name);
    }
    elsif ($File::Find::name =~ /\.java$/) {
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
            $path_to_package{dirname($file)} = $1;
            last;
        }
    }
    close FILE;
}


sub parse_module_info {
    my ($file) = @_;
    open(FILE, "<", "$file") || die "[parse_module_info] cannot open the file: $!\n";
    my @line = <FILE>;

    my $module_name;
    foreach my $line (@line) {
        if ($line =~ m/module\s+([\w.]+)\s*{/) {
            $module_name = $1;
            $path_to_module_name{dirname($file)} = $module_name;
        }
        elsif ($line =~ m/exports\s+([\w.]+)\s?;/) {
            #print "$1,$module_name\n";
            $package_to_module{$1} = $module_name;
        }
    }
    close FILE;
}

sub longest_match_package_to_module() {
    while (my ($path, $package) = each(%path_to_package)) {
        my $now_path = $path;
        my $prev_path = $path;

        my $found = 0;
        do {
            if (exists($path_to_module_name{$now_path})) {
                $package_to_module{$package} = $path_to_module_name{$now_path};
                $found = 1;
            }
            else {
                $prev_path = $now_path;
                $now_path = dirname($now_path);
            }
        } while ($found == 0 and ($now_path ne $prev_path));
    }
}
