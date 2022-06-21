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
package org.eclipse.emt4j.common.i18n;

import org.eclipse.emt4j.common.JdkMigrationException;

import java.util.*;


/**
 * Access i18n resource for generating check reports.
 * Use the following priority to find the target i18 resource.
 * resultCode + subResultCode > subResultCode
 * If for given subResultCode has no i18n file, try to use the resultCode corresponding 18n resources.
 */
public class ReportResourceAccessor {

    private final Locale locale;
    private Map<String, Optional<ResourceBundle>> resourceBundleMap = new HashMap<>();
    private ResourceBundle commonResource;

    public ReportResourceAccessor(Locale locale) {
        this.locale = locale;
    }

    public I18nResourceUnit getResourceUnit(String resultCode, String subResultCode, String i18nBase) {
        ResourceBundle resultCodeSpecific = getResourceBundleLazily(resultCode, subResultCode, i18nBase);
        I18nResourceUnit resourceUnit = new I18nResourceUnit(resultCode, subResultCode, this, i18nBase);
        resourceUnit.setDescription(resultCodeSpecific.getString("description"));
        resourceUnit.setTitle(resultCodeSpecific.getString("title"));
        resourceUnit.setSolution(resultCodeSpecific.getString("solution"));

        ResourceBundle common = getCommonResourceBundle(i18nBase);
        resourceUnit.setDescriptionTitle(common.getString("description.title"));
        resourceUnit.setSolutionTitle(common.getString("solution.title"));
        resourceUnit.setIssueContextTitle(common.getString("issue.context.title"));

        return resourceUnit;
    }

    public String getNoIssueResource(String i18nBase) {
        return getCommonResourceBundle(i18nBase).getString("no.issue");
    }

    public String getString(String i18nBase, String key) {
        return getCommonResourceBundle(i18nBase).getString(key);
    }

    public String getCheckResultForFeature(String i18nBase) {
        return getCommonResourceBundle(i18nBase).getString("result.for.feature");
    }

    public ResourceBundle getCommonResourceBundle(String i18nBase) {
        if (null == commonResource) {
            commonResource = selectResourceBundle("common", i18nBase);
        }
        return commonResource;
    }

    public boolean containResourceBundle(String resultCode, String subResultCode, String i18nBase) {
        String key = resultCode + "_" + subResultCode;
        Optional<ResourceBundle> value = resourceBundleMap.get(key);
        if (value != null) {
            return value.isPresent();
        } else {
            try {
                ResourceBundle resourceBundle = selectResourceBundle(key, i18nBase);
                resourceBundleMap.put(key, Optional.of(resourceBundle));
                return true;
            } catch (MissingResourceException mre) {
                //not every result code and sub result code has specific resource definition.
                //so it is a normal case.
                //when this happen.we put a Optional.empty() indicate not resource bundle so avoid
                //find resource every time on disk.
                resourceBundleMap.put(key, Optional.empty());
                return false;
            }
        }
    }

    private ResourceBundle selectResourceBundle(String baseName, String i18nBase) {
        return ResourceBundle.getBundle(i18nBase + baseName, locale);
    }

    private ResourceBundle getResourceBundleLazily(String resultCode, String subResultCode, String i18nBase) {
        String[] keys = getPriorityKeys(resultCode, subResultCode);
        for (String key : keys) {
            Optional<ResourceBundle> value = resourceBundleMap.get(key);
            if (value != null && value.isPresent()) {
                return value.get();
            } else {
                try {
                    ResourceBundle resourceBundle = selectResourceBundle(key, i18nBase);
                    resourceBundleMap.put(key, Optional.of(resourceBundle));
                    return resourceBundle;
                } catch (MissingResourceException mre) {
                    //not every result code and sub result code has specific resource definition.
                    //so it is a normal case.
                    //when this happen.we put a Optional.empty() indicate not resource bundle so avoid
                    //find resource every time on disk.
                    resourceBundleMap.put(key, Optional.empty());
                }
            }
        }
        throw new JdkMigrationException("Can not found resource bundle for result code=" + resultCode + ",sub result code=" + subResultCode + ",i18nBase=" + i18nBase);
    }

    private String[] getPriorityKeys(String resultCode, String subResultCode) {
        if (null == subResultCode || "".equals(subResultCode)) {
            return new String[]{resultCode};
        } else {
            return new String[]{resultCode + "_" + subResultCode, resultCode};
        }
    }
}
