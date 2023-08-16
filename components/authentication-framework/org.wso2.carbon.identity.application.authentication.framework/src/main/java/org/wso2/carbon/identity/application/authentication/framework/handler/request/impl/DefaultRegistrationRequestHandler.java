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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.RegistrationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.RegistrationStepBasedSequenceHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default Registration Request Handler.
 */
public class DefaultRegistrationRequestHandler implements RegistrationRequestHandler {

    private static final Log log = LogFactory.getLog(DefaultRegistrationRequestHandler.class);
    private static volatile DefaultRegistrationRequestHandler instance;

    public static DefaultRegistrationRequestHandler getInstance() {

        if (instance == null) {
            synchronized (DefaultRegistrationRequestHandler.class) {
                if (instance == null) {
                    instance = new DefaultRegistrationRequestHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        RegistrationStepBasedSequenceHandler handler = new RegistrationStepBasedSequenceHandler();
        log.debug("Executing the Step Based Registration...");
        handler.handle(request, response, context);

        if (context.getSequenceConfig().isCompleted()) {
            log.info("The registration is completed. Can trigger post registration steps.");
            log.info("Final user" + context.getLastAuthenticatedUser().toString());
        }
    }
}
