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

if (@ARGV != 2) {
    print STDERR "find_removed_classes.pl [base JDK source dir] [current JDK source dir]";
    exit(1);
}
my $base_src_dir = $ARGV[0];
if (!-d $base_src_dir) {
    print STDERR "$base_src_dir not exist!";
    exit(1);
}
my $curr_src_dir = $ARGV[1];
if (!-d $curr_src_dir) {
    print STDERR "$curr_src_dir not exist!";
    exit(1);
}

my %base_classes;
my %curr_classes;

## scan_type,when 1: scan base,when 2: scan current
my $scan_type = 1;
find(\&wanted, $base_src_dir);
$scan_type = 2;
find(\&wanted, $curr_src_dir);

my @sort_base_classes = sort keys %base_classes;
foreach my $base_class (@sort_base_classes) {
    if (!exists $curr_classes{$base_class}) {
        $base_class =~ s/\./\//g;
        print "$base_class\n";
    }
}

sub wanted {
    if ($File::Find::name =~ /\.java$/) {
        parse_java_file($File::Find::name);
    }
    return;
}

sub parse_java_file() {
    my ($file) = @_;
    return if ($file =~ m/Test/i);

    #print "$file\n";
    my $class_name = basename($file);
    $class_name =~ s/\.java//;
    #print "$class_name\n";
    open(FILE, "<", "$file") || die "[parse_java_file] cannot open the file: $!\n";
    my @line = <FILE>;
    my $package_name = "";
    foreach my $line (@line) {
        if ($line =~ m/^package\s+([\w.]+)\s*;\s*$/) {
            $package_name = $1;
        }
        # only public class we think should be used by outer
        # also it can be used by deep reflection.
        if ($line =~ m/public\s+.*class\s+$class_name/ || $line =~ m/interface\s+$class_name/) {
            if ($package_name eq "") {
                return;
            }

            if ($scan_type == 1) {
                $base_classes{"$package_name\.$class_name"} = "true";
            }
            elsif ($scan_type == 2) {
                $curr_classes{"$package_name\.$class_name"} = "true";
            }
            last;
        }
    }
    close FILE;
}

