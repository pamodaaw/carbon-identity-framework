/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.self.registration.servlet;

import org.wso2.carbon.identity.user.self.registration.temp.ConfigDataHolder;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Server api to handle the registration orchestration config data.
 */
public class RegistrationOrchestrationServlet extends HttpServlet {

    private static final long serialVersionUID = 5546734997561711495L;
    private static final String superTenantDomain = "carbon.super";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        // get json data from request
        String configData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ConfigDataHolder.getInstance().getOrchestrationConfig().put(superTenantDomain, configData);

        response.setContentType("application/json");
        response.getWriter().write("{\"org\":\"" + superTenantDomain + "\", \"config\":" + configData + "}");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
