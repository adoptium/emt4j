#! /bin/bash

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

# resolve links - $0 may be a softlink (code as used by gradle)
PRG="$0"

while
  base_dir=${PRG%"${PRG##*/}"}
  [ -h "$PRG" ]
do
  ls=$( ls -ld "$PRG")
  link=${ls#*' -> '}
  case $link in
    /* )  PRG=$link;;
    *  )  PRG=$base_dir$link;;
  esac
done
base_dir=$(cd "${base_dir:-./}" && pwd -P) || exit
lib_dir=$(builtin cd "$base_dir/../lib/analysis";pwd)
class_path="${lib_dir}/*"
java -cp "$class_path" org.eclipse.emt4j.analysis.AnalysisMain "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9" "${10}" "${11}" "${12}" "${13}" "${14}" "${15}" "${16}" "${17}" "${18}"
