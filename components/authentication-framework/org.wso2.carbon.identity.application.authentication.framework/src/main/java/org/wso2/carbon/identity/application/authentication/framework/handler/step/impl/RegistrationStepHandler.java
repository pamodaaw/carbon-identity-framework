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

package org.wso2.carbon.identity.application.authentication.framework.handler.step.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is used to handle the registration step of the authentication flow.
 */
public class RegistrationStepHandler implements StepHandler {

    private static final Log LOG = LogFactory.getLog(RegistrationStepHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());

        List<AuthenticatorConfig> authConfigList = stepConfig.getAuthenticatorList();
        AuthenticatorConfig authenticatorConfig = authConfigList.get(0);

        if (!authenticatorConfig.getIdpNames().isEmpty()) {

                LOG.info("Step contains only a single IdP. Going to call it directly");

            // set the IdP to be called in the context
            try {
                context.setExternalIdP(ConfigurationFacade.getInstance()
                        .getIdPConfigByName(authenticatorConfig.getIdpNames().get(0),
                                context.getTenantDomain()));
            } catch (IdentityProviderManagementException e) {
                LOG.error("Exception while getting IdP by name", e);
            }
        }
        AuthenticatorFlowStatus status =
                authenticatorConfig.getApplicationAuthenticator().processRegistration(request,
                response, context);
        request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, status);
        LOG.info("User registration flow status: " + status);

        if (AuthenticatorFlowStatus.INCOMPLETE.equals(status)) {
            return;
        }
        stepConfig.setCompleted(true);
    }
}
