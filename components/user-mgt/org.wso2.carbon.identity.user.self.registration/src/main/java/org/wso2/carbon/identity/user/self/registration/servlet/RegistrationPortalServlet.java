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

import com.google.gson.Gson;
import org.json.JSONObject;
import org.wso2.carbon.identity.user.self.registration.UserRegistrationFlowService;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.model.ExecutionState;
import org.wso2.carbon.identity.user.self.registration.model.InputData;
import org.wso2.carbon.identity.user.self.registration.util.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Client servlet to handle the registration flow data.
 */
public class RegistrationPortalServlet extends HttpServlet {

    private static final long serialVersionUID = 5546734997561711495L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        // get request uri
        String uri = request.getRequestURI();
        if (uri.contains("/initiate")) {
            initiateFlow(request, response);
        } else {
            continueFlow(request, response);
        }
    }

    private void initiateFlow(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            ExecutionState state = UserRegistrationFlowService.getInstance().initiateFlow(Constants.NEW_FLOW);
            buildResponse(response, state);
        } catch (RegistrationFrameworkException e) {
            log("Error while initiating the registration flow", e);
            buildStandardErrorResponse(response);
        }
    }

    private void continueFlow(HttpServletRequest request, HttpServletResponse response) throws IOException {

        /*
            { "flowId": "d13ec8d2-2d1e-11ee-be56-0242ac120002", "action": "EmailOTPVerifier", "inputs": { "username":
             "johndoe", "firstname": "John", "lastname": "Doe", "dob": "30/06/1995" } }
        */
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject json = new JSONObject(requestBody);
        String flowId = json.getString("flowId");
        String action = json.getString("action");
        JSONObject inputs = json.getJSONObject("inputs");
        inputs.put("user-choice", action);

        // loop through the inputs and convert them to a map without using streams
        Map<String, String> inputMap = new HashMap<>();
        for (Object key : inputs.keySet()) {
            inputMap.put((String) key, inputs.getString((String) key));
        }
        InputData inputData = new InputData();
        inputData.setUserInput(inputMap);

        try {
            ExecutionState state = UserRegistrationFlowService.getInstance().continueFlow(flowId, inputData);
            buildResponse(response, state);
        } catch (RegistrationFrameworkException e) {
            log("Error while continuing the registration flow", e);
            buildStandardErrorResponse(response);
        }
    }

    private void buildResponse(HttpServletResponse response, ExecutionState state) throws IOException {

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("flowId", state.getFlowId());
        data.put("status", state.getResponse().getStatus());
        data.put("type", "registration");
        data.put("page", state.getResponse().getPage());

        Gson gson = new Gson();
        String jsonString = gson.toJson(data);

        response.setContentType("application/json");
        response.getWriter().write(jsonString);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void buildStandardErrorResponse(HttpServletResponse response) throws IOException {

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("error", "Error occurred while processing the request. Check terminal for logs.");

        Gson gson = new Gson();
        String jsonString = gson.toJson(data);

        response.setContentType("application/json");
        response.getWriter().write(jsonString);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}
