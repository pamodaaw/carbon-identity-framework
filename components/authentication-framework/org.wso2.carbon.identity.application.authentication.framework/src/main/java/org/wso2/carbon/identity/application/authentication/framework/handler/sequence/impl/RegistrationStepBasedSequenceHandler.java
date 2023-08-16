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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.RegistrationStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default Registration Step Based Sequence Handler.
 */
public class RegistrationStepBasedSequenceHandler implements StepBasedSequenceHandler {

    private static final Log log = LogFactory.getLog(RegistrationStepBasedSequenceHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        log.info("REG SEQ HANDLER:Executing the Step Based Registration...");

        while (!context.getSequenceConfig().isCompleted()) {

            int currentStep = context.getCurrentStep();

            // let's initialize the step count to 1 if this the beginning of the sequence
            if (currentStep == 0) {
                currentStep++;
                context.setCurrentStep(currentStep);
            }

            StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);

            // if the current step is completed
            if (stepConfig != null && stepConfig.isCompleted()) {
                stepConfig.setCompleted(false);
                stepConfig.setRetrying(false);

                // if the request didn't fail during the step execution
                if (context.isRequestAuthenticated()) {
                    log.info("REG SEQ HANDLER: Step " + stepConfig.getOrder() + " is completed. Going to get the next" +
                            " one.");
                    currentStep = context.getCurrentStep() + 1;
                    context.setCurrentStep(currentStep);
                    stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);
                }
                FrameworkUtils.resetAuthenticationContext(context);
            }

            // if no further steps exists
            if (stepConfig == null) {

                log.info("REG SEQ HANDLER: There are no more steps to execute.");

                // if no step failed at authentication we should do post authentication work (e.g.
                // claim handling, provision etc)
                if (context.isRequestAuthenticated()) {

                    log.debug("REG SEQ HANDLER: Request is successfully authenticated.");
                    context.getSequenceConfig().setCompleted(true);
                }

                // we should get out of steps now.
                log.debug("REG SEQ HANDLER: Step processing is completed.");
                continue;
            }

            // if the sequence is not completed, we have work to do.
            log.info("REG SEQ HANDLER: Starting Step: " + stepConfig.getOrder());

            RegistrationStepHandler registrationStepHandler = new RegistrationStepHandler();
            registrationStepHandler.handle(request, response, context);

            // if step is not completed, that means step wants to redirect to outside
            if (!stepConfig.isCompleted()) {
                log.info("REG SEQ HANDLER: Step is not complete yet. Redirecting to outside.");
                return;
            }
            context.setReturning(false);
        }
    }
}
