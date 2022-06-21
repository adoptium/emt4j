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

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Describe a java option.If all option describe by <code>Option</code>,
 * there no need hard parse the java arguments.
 */
public class Option {

    enum OptionType {
        PARAM_WITH_VALUE,
        PARAM_NO_VALUE,
        DEFAULT
    }

    private OptionType optionType;

    private String paramName;

    private Predicate<String> validCheck;

    private Consumer<String> consumeOption;

    public static Option buildParamWithValueOption(String paramName, Predicate<String> validCheck, Consumer<String> consumeOption) {
        Option option = new Option();
        option.paramName = paramName;
        option.validCheck = validCheck;
        option.consumeOption = consumeOption;
        option.optionType = OptionType.PARAM_WITH_VALUE;
        return option;
    }

    public static Option buildParamNoValueOption(String paramName, Predicate<String> validCheck, Consumer<String> consumeOption) {
        Option option = new Option();
        option.paramName = paramName;
        option.validCheck = validCheck;
        option.consumeOption = consumeOption;
        option.optionType = OptionType.PARAM_NO_VALUE;
        return option;
    }

    public static Option buildDefaultOption(Predicate<String> validCheck, Consumer<String> consumeOption) {
        Option option = new Option();
        option.validCheck = validCheck;
        option.consumeOption = consumeOption;
        option.optionType = OptionType.DEFAULT;
        return option;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Predicate<String> getValidCheck() {
        return validCheck;
    }

    public void setValidCheck(Predicate<String> validCheck) {
        this.validCheck = validCheck;
    }

    public Consumer<String> getConsumeOption() {
        return consumeOption;
    }

    public void setConsumeOption(Consumer<String> consumeOption) {
        this.consumeOption = consumeOption;
    }
}

