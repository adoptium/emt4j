/********************************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis.common.util;

import org.eclipse.emt4j.common.JdkMigrationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A java argument processor. Avoid hard parsing the argument.
 */
public class OptionProcessor {

    private final String[] args;
    private List<Option> optionList = new ArrayList<>();
    private Option defaultOption;
    private Consumer<String> showUsage;

    public OptionProcessor(String[] args) {
        this.args = args;
    }

    public void addOption(Option option) {
        if (option.getOptionType() == Option.OptionType.DEFAULT) {
            if (defaultOption != null) {
                throw new JdkMigrationException("Only one default option!");
            }
            this.defaultOption = option;
        } else {
            optionList.add(option);
        }
    }

    public void setShowUsage(Consumer<String> showUsage) {
        this.showUsage = showUsage;
    }

    public void process() {
        for (final MutableInt i = new MutableInt(0); i.getValue() < args.length; ) {
            if (StringUtils.isEmpty(args[i.getValue()])) {
                i.add(1);
                continue;
            }
            Optional<Option> option = optionList.stream().filter((o) -> o.getParamName().equals(args[i.getValue()])).findFirst();
            if (option.isPresent()) {
                switch (option.get().getOptionType()) {
                    case PARAM_WITH_VALUE: {
                        if (i.getValue() == args.length - 1) {
                            showUsageAndExit(args[i.getValue()]);
                        } else {
                            checkAcceptAdvance(i, option.get(), i.getValue() + 1, 2);
                        }
                        break;
                    }
                    case PARAM_NO_VALUE: {
                        option.get().getConsumeOption().accept(args[i.getValue()]);
                        i.add(1);
                        break;
                    }
                    default:
                        throw new JdkMigrationException("Should not reach here!");
                }
            } else {
                if (defaultOption != null) {
                    checkAcceptAdvance(i, defaultOption, i.getValue(), 1);
                } else {
                    showUsageAndExit(args[i.getValue()]);
                }
            }
        }
    }

    private void checkAcceptAdvance(MutableInt index, Option option, int value, int advance) {
        if (option.getValidCheck() != null) {
            if (option.getValidCheck().test(args[value])) {
                option.getConsumeOption().accept(args[value]);
                index.add(advance);
            } else {
                showUsageAndExit(args[index.getValue()]);
            }
        } else {
            option.getConsumeOption().accept(args[value]);
            index.add(advance);
        }
    }

    private void showUsageAndExit(String arg) {
        showUsage.accept(arg);
    }
}
