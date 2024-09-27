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

package org.wso2.carbon.identity.user.self.registration.graphexecutor;

/**
 * Constants for the graph executor.
 */
public class Constants {

    // Define a set of constants to track the status returned from the Executor level. The status can be COMPLETE,
    // INCOMPLETE, USER_INPUT_REQUIRED, ERROR, USER_CHOICE_REQUIRED
    public static final String STATUS_FLOW_COMPLETE = "COMPLETE";
    public static final String STATUS_USER_INPUT_REQUIRED = "USER_INPUT_REQUIRED";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String STATUS_NODE_COMPLETE = "NODE_COMPLETE";
    public static final String STATUS_COMPLETE = "COMPLETE";
}
