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

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthRequestWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.user.registration.SelfRegistrationConstants;
import org.wso2.carbon.identity.user.registration.model.RegistrationResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class acts as the entry point for the API based user registration flow.
 */
public class UserRegistrationService {

    private static final Log LOG = LogFactory.getLog(UserRegistrationService.class);
    private final CommonAuthenticationHandler  commonAuthenticationHandler = new CommonAuthenticationHandler();

    public RegistrationResponse handleRegistrationRequest(HttpServletRequest request, HttpServletResponse response) {

        request.setAttribute("isRegistrationRequest", true);
        CommonAuthRequestWrapper wrappedRequest = new CommonAuthRequestWrapper(request);
        CommonAuthResponseWrapper wrappedResponse = new CommonAuthResponseWrapper(response);
        try {
            commonAuthenticationHandler.doPost(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            LOG.error("Error while handling registration request", e);
        }
        return processCommonAuthResponse(request, response);
    }

    private RegistrationResponse processCommonAuthResponse(HttpServletRequest request, HttpServletResponse response) {

        return request.getAttribute(SelfRegistrationConstants.REGISTRATION_RESPONSE) != null ?
                (RegistrationResponse) request
                        .getAttribute(SelfRegistrationConstants.REGISTRATION_RESPONSE) : null;
    }
}
