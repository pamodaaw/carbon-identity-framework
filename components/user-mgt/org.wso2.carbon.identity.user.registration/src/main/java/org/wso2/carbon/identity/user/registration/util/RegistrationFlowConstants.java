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

public class RegistrationFlowConstants {

    public enum Status {

        INCOMPLETE,
        COMPLETE,
        FAILED
    }

    public enum RegistrationExecutorBindingType {

        NONE, // Indicates there is no binding.
        AUTHENTICATOR // Indicates the registration executor is bound to an authenticator.
    }

    /**
     * Supported protocols for self user registration.
     */
    public enum SupportedProtocol {

        OIDC, API_BASED
    }

    /**
     * Supported message types for self user registration flow responses.
     */
    public enum MessageType {

        INFO, ERROR
    }

    /**
     * Supported data types for the required parameters for the user registration.
     */
    public enum DataType {

        STRING, BOOLEAN, INTEGER
    }

    /**
     * Option types for the registration steps.
     */
    public enum StepType {

        SINGLE_OPTION, MULTI_OPTION,
    }

    /**
     * Prompt types for the registration steps.
     */
    public enum PromptType {

        USER_PROMPT,
    }

    /**
     * Status of a registration steps.
     */
    public enum StepStatus {

        NOT_STARTED, COMPLETE, INCOMPLETE, SELECTION_PENDING, USER_INPUT_REQUIRED
    }
}
