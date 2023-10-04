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

package org.wso2.carbon.identity.user.registration.internal;

import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

public class UserRegistrationServiceDataHolder {

    private static final List<RegistrationStepExecutor> registrationStepExecutors = new ArrayList<>();
    private static ApplicationManagementService applicationManagementService;
    private static RealmService realmService = null;

    public static List<RegistrationStepExecutor> getRegistrationStepExecutors() {

        // TODO: 9/27/17 do the null check
        return registrationStepExecutors;
    }

    public static ApplicationManagementService getApplicationManagementService() {

        // TODO: 9/27/17 do the null check
        return UserRegistrationServiceDataHolder.applicationManagementService;
    }

    public static void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        UserRegistrationServiceDataHolder.applicationManagementService = applicationManagementService;
    }

    public static RealmService getRealmService() {

        return realmService;
    }

    public static void setRealmService(RealmService realmService) {

        UserRegistrationServiceDataHolder.realmService = realmService;
    }
}
