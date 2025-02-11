/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.self.registration.mgt;

/**
 * Constants for the self registration flow.
 */
public class Constants {

    public static final String NEW_FLOW = "newflow"; // temp constant to engage new flow.

    // Constants for the registration flow json definition.
    public static final String COMPLETE = "COMPLETE";
    public static final String NEXT = "NEXT";
    public static final String EXECUTOR = "EXECUTOR";
    public static final String RULE = "RULE";
    public static final String EXECUTOR_TYPE = "EXECUTOR_TYPE";
    public static final String EXECUTOR_ID = "EXECUTOR_ID";

    /**
     * Constants for the flow elements.
     */
    public static class FlowElements {

        public static final String NODES = "nodes";
        public static final String ACTIONS = "actions";
        public static final String ACTION = "action";
        public static final String EXECUTORS = "executors";
        public static final String ID = "id";
        public static final String NEXT = "next";
        public static final String TYPE = "type";
        public static final String NAME = "name";
        public static final String META = "meta";
        public static final String IDP = "idp";
    }

    /**
     * Constants for the node types.
     */
    public static class NodeTypes {

        public static final String PROMPT = "PROMPT";
        public static final String DECISION = "DECISION";
        public static final String TASK_EXECUTION = "TASK_EXECUTION";
    }
}
