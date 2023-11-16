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

package org.wso2.carbon.identity.user.registration.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cryptacular.codec.Base64Encoder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.user.registration.RegistrationStepExecutor;
import org.wso2.carbon.identity.user.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.model.response.RequiredParam;
import org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.registration.util.RegistrationFlowConstants.StepStatus.NOT_STARTED;

/**
 * This class is responsible for loading the authentication sequence based on the login sequence of the application.
 */
public class AuthSequenceBasedConfigLoader {

    private static final Log LOG = LogFactory.getLog(AuthSequenceBasedConfigLoader.class);
    private static AuthSequenceBasedConfigLoader instance = new AuthSequenceBasedConfigLoader();

    private AuthSequenceBasedConfigLoader() {

    }

    public static AuthSequenceBasedConfigLoader getInstance() {

        return instance;
    }

    public RegistrationSequence deriveRegistrationSequence(String appId, String tenantDomain) throws RegistrationFrameworkException {

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ServiceProvider sp;
        try {
            sp = appInfo.getApplicationByResourceId(appId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error occurred while retrieving service provider", e);
        }
        if (sp == null) {
            throw new RegistrationFrameworkException("Service provider not found for app id: " + appId);
        }
        AuthenticationStep[] authenticationSteps =
                sp.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
        return getSequenceFromServiceProvider(sp, authenticationSteps);
    }


    private RegistrationSequence getSequenceFromServiceProvider(ServiceProvider serviceProvider,
                                                                AuthenticationStep[] authenticationSteps) throws RegistrationFrameworkException {

        if (serviceProvider == null) {
            throw new RegistrationFrameworkException("ServiceProvider cannot be null.");
        }
        RegistrationSequence sequenceConfig = new RegistrationSequence();
        sequenceConfig.setApplicationId(serviceProvider.getApplicationName());
        sequenceConfig.setFlowDefinition(RegistrationFlowConstants.DEFAULT_FLOW_DEFINITION);

        if (authenticationSteps == null) {
            return sequenceConfig;
        }

        // for each configured step. We are considering only the first step at the moment.
//        for (AuthenticationStep authenticationStep : authenticationSteps) {
//
//            // loading local authenticators
//            RegistrationStep stepConfig = loadExecutors(authenticationStep);
//
//            if (stepConfig != null) {
//                stepConfig.setStatus(NOT_STARTED);
//                stepConfig.setOrder(++stepOrder);
//                sequenceConfig.getStepMap().put(stepConfig.getOrder(), stepConfig);
//            }
//        }
        AuthenticationStep firstStep = authenticationSteps[0];

        // Load registration executors based on the authenticators.
        RegistrationStep stepConfig = loadExecutors(firstStep, serviceProvider.getTenantDomain());

        if (stepConfig != null) {
            sequenceConfig.addStepDefinition(stepConfig);
        }
        RegistrationStep attributeCollectStep = deriveAttributeCollectionStep(serviceProvider);

        if (attributeCollectStep != null) {
            sequenceConfig.addStepDefinition(attributeCollectStep);
        }
        return sequenceConfig;
    }

    private RegistrationStepExecutor getRegStepExecutor(String name) {

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (name.equals(executor.getName())) {
                return executor;
            }
        }
        return null;
    }

    private RegistrationStep loadExecutors(AuthenticationStep authenticationStep, String tenantDomain)
            throws RegistrationFrameworkException {

        List<RegistrationStepExecutorConfig> executorConfigs = new ArrayList<>();

        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
        if (localAuthenticators != null) {
            // assign it to the step
            IdentityProvider localIdp = new IdentityProvider();
            localIdp.setIdentityProviderName(FrameworkConstants.LOCAL_IDP_NAME);

            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(localAuthenticator.getName(),
                        localAuthenticator.getName(), localIdp);
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        IdentityProvider[] federatedIDPs = authenticationStep.getFederatedIdentityProviders();
        if (federatedIDPs != null) {
            // for each idp in the step
            for (IdentityProvider federatedIDP : federatedIDPs) {
                FederatedAuthenticatorConfig federatedAuthenticator = federatedIDP.getDefaultAuthenticatorConfig();

                if (federatedAuthenticator == null) {
                    try {
                        federatedAuthenticator = IdentityProviderManager.getInstance()
                                .getIdPByName(federatedIDP.getIdentityProviderName(), tenantDomain)
                                .getDefaultAuthenticatorConfig();
                    } catch (IdentityProviderManagementException e) {
                        throw new RegistrationFrameworkException("Failed to load the default authenticator for IDP : "
                                + federatedIDP.getIdentityProviderName(), e);
                    }
                }
                RegistrationStepExecutorConfig regStepConfig = getMappedRegExecutorConfig(
                        federatedIDP.getIdentityProviderName(), federatedAuthenticator.getName(), federatedIDP
                );
                if (regStepConfig != null) {
                    executorConfigs.add(regStepConfig);
                }
            }
        }

        if (executorConfigs.size() == 0) {
            LOG.info("No supported executors in the step");
            return null;
        }
        RegistrationStep stepConfig = new RegistrationStep();
        stepConfig.setConfiguredExecutors(executorConfigs);
        if (executorConfigs.size() > 1) {
            stepConfig.setMultiOption(true);
        }
        return stepConfig;
    }

    private RegistrationStepExecutorConfig getMappedRegExecutorConfig(String idpName,
                                                                      String authenticatorName, IdentityProvider idp)
            throws RegistrationFrameworkException {

        RegistrationStepExecutorConfig regStepConfig = new RegistrationStepExecutorConfig();
        RegistrationStepExecutor mappedRegExecutor = null;

        for (RegistrationStepExecutor executor : UserRegistrationServiceDataHolder.getRegistrationStepExecutors()) {
            if (RegistrationFlowConstants.RegistrationExecutorBindingType.AUTHENTICATOR.equals(executor.getBindingType())
                    && executor.getBoundIdentifier().equals(authenticatorName)) {
                mappedRegExecutor = executor;
                break;
            }
        }

        if (mappedRegExecutor == null) {
            return null;
        }
        regStepConfig.setName(idpName);
        regStepConfig.setId(Base64.getEncoder().encodeToString(idpName.getBytes(StandardCharsets.UTF_8)));
        regStepConfig.setExecutor(mappedRegExecutor);
        regStepConfig.setIdentityProvider(idp);

        return regStepConfig;
    }

    private RegistrationStep deriveAttributeCollectionStep(ServiceProvider serviceProvider) {

        ClaimMapping[] requestedClaims = serviceProvider.getClaimConfig().getClaimMappings();

        if (requestedClaims == null || requestedClaims.length == 0) {
            return null;
        }
        RegistrationStepExecutorConfig config = new RegistrationStepExecutorConfig();
        config.setName("AttributeCollection");
        config.setId("AttributeCollectorBasedOnAppClaims");
        config.setRequestedClaims(requestedClaims);
        config.setExecutor(getRegStepExecutor("AttributeCollector"));

        List<RegistrationStepExecutorConfig> executors = new ArrayList<>();
        executors.add(config);

        RegistrationStep step = new RegistrationStep();
        step.setMultiOption(false);
        step.setSelectedExecutor(null);
        step.setConfiguredExecutors(executors);

        return step;
    }
}
