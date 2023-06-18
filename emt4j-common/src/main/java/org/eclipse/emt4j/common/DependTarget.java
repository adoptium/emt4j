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
package org.eclipse.emt4j.common;

import org.eclipse.emt4j.common.util.ClassUtil;

import java.io.Serializable;
import java.net.URL;

public abstract class DependTarget implements Serializable {

    final DependType type;

    DependTarget(DependType type) {
        this.type = type;
    }

    public Method asMethod() {
        return (Method) this;
    }

    public Class asClass() {
        return (Class) this;
    }

    public Location asLocation() {
        return (Location) this;
    }

    public VMOption asVMOption() {
        return (VMOption) this;
    }

    public DependType type() {
        return type;
    }

    public abstract String desc();

    public static class Method extends DependTarget {
        private final String className;
        private final String methodName;
        private final String desc;
        public static final String ANY_DESC = "*";

        public Method(String className, String methodName, String desc, DependType type) {
            super(type);
            this.className = className;
            this.methodName = methodName;
            this.desc = desc;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Method method = (Method) o;

            if (className != null ? !className.equals(method.className) : method.className != null) return false;
            if (methodName != null ? !methodName.equals(method.methodName) : method.methodName != null) return false;
            return desc != null ? desc.equals(method.desc) : method.desc == null;
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            return result;
        }

        public String toMethodIdentifier() {
            return ClassUtil.buildMethodIdentifier(className, methodName, desc);
        }

        public String toMethodIdentifierNoDesc() {
            return ClassUtil.buildMethodIdentifierNoDesc(className, methodName);
        }

        @Override
        public String desc() {
            return toMethodIdentifier();
        }
    }

    public static class Location extends DependTarget {
        private final String locationExternalForm;

        public Location(URL location) {
            super(DependType.CODE_SOURCE);
            this.locationExternalForm = location.toExternalForm();
        }

        public Location(URL location, DependType dependType) {
            super(dependType);
            this.locationExternalForm = location.toExternalForm();
        }

        public String getLocationExternalForm() {
            return locationExternalForm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            return locationExternalForm != null ? locationExternalForm.equals(location.locationExternalForm) : location.locationExternalForm == null;
        }

        @Override
        public int hashCode() {
            return locationExternalForm != null ? locationExternalForm.hashCode() : 0;
        }

        @Override
        public String desc() {
            return locationExternalForm;
        }
    }

    public static class Class extends DependTarget {
        private final String className;

        public Class(String className, DependType type) {
            super(type);
            this.className = className;
        }

        public String getClassName() {
            return this.className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Class aClass = (Class) o;

            return className != null ? className.equals(aClass.className) : aClass.className == null;
        }

        @Override
        public int hashCode() {
            return className != null ? className.hashCode() : 0;
        }

        @Override
        public String desc() {
            return className;
        }
    }

    public static class VMOption extends DependTarget {
        private final String vmOption;

        public VMOption(String vmOption) {
            super(DependType.VM_OPTION);
            this.vmOption = vmOption;
        }

        public String getVmOption() {
            return vmOption;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VMOption vmOption1 = (VMOption) o;

            return vmOption != null ? vmOption.equals(vmOption1.vmOption) : vmOption1.vmOption == null;
        }

        @Override
        public int hashCode() {
            return vmOption != null ? vmOption.hashCode() : 0;
        }

        @Override
        public String desc() {
            return vmOption;
        }
    }
}
