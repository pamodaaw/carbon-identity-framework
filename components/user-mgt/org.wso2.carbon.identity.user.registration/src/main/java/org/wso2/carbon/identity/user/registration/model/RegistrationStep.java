/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RegistrationStep implements Serializable {

    private static final long serialVersionUID = -6352819516789400021L;
    private int order;
    private String authenticatedIdP;
    private boolean isMultiOption;
    private boolean isCompleted;
    private RegistrationExecutorConfig selectedExecutor;
    private List<RegistrationExecutorConfig> configuredExecutors = new ArrayList<>();

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public String getAuthenticatedIdP() {

        return authenticatedIdP;
    }

    public void setAuthenticatedIdP(String authenticatedIdP) {

        this.authenticatedIdP = authenticatedIdP;
    }

    public boolean isMultiOption() {

        return isMultiOption;
    }

    public void setMultiOption(boolean multiOption) {

        isMultiOption = multiOption;
    }

    public boolean isCompleted() {

        return isCompleted;
    }

    public void setCompleted(boolean completed) {

        isCompleted = completed;
    }

    public RegistrationExecutorConfig getSelectedExecutor() {

        return selectedExecutor;
    }

    public void setSelectedExecutor(RegistrationExecutorConfig selectedExecutor) {

        this.selectedExecutor = selectedExecutor;
    }

    public List<RegistrationExecutorConfig> getConfiguredExecutors() {

        return configuredExecutors;
    }

    public void setConfiguredExecutors(List<RegistrationExecutorConfig> configuredExecutors) {

        this.configuredExecutors = configuredExecutors;
    }
}
