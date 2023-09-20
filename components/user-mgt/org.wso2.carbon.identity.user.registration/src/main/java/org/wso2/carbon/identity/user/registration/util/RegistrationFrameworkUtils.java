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

package org.wso2.carbon.identity.user.registration.util;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequest;
import org.wso2.carbon.identity.user.registration.model.RegistrationRequestedUser;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class RegistrationFrameworkUtils {

    public static RegistrationContext initiateRegistrationContext(HttpServletRequest request,
                                                                  RegistrationRequest registrationRequest) throws RegistrationFrameworkException {

        String contextId = UUID.randomUUID().toString();
        int step = 1;
        RegistrationFlowConstants.RegistrationFlowStatus status = RegistrationFlowConstants.RegistrationFlowStatus.INCOMPLETE;
        RegistrationRequestedUser user = new RegistrationRequestedUser();

        String requestType = registrationRequest.getType();
        String relyingParty = registrationRequest.getRelyingParty();
        String tenantDomain = registrationRequest.getTenantDomain();

        int appId = getServiceProviderId(requestType, relyingParty, tenantDomain);

        RegistrationContext regContext = new RegistrationContext();
        regContext.setContextIdentifier(contextId);
        regContext.setRequestType(requestType);
        regContext.setRelyingParty(relyingParty);
        regContext.setTenantDomain(tenantDomain);
        regContext.setCurrentStep(step);
        regContext.setFlowStatus(status);
        regContext.setRegisteringUser(user);

        return regContext;
    }

    private static int getServiceProviderId(String reqType, String clientId, String tenantDomain) throws RegistrationFrameworkException {

        if ("oidc".equals(reqType)) {
            reqType = "oauth2";
        }

        ApplicationManagementService appMgtService = UserRegistrationServiceDataHolder.getApplicationManagementService();
        try {
            ServiceProvider serviceProvider = appMgtService.getServiceProviderByClientId(clientId, reqType,
                    tenantDomain);
            return serviceProvider.getApplicationID();
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error while retrieving service provider for client id: " +
                    clientId + " and request type: " + reqType, e);
        }
    }
}
