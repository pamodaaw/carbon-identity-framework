/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.action.execution.model;

import java.util.Map;

/**
 * This class models the Action Execution Status.
 * Action Execution Status is the status object that is returned by the Action Executor Service after executing an
 * action. It contains the status of the action execution and the response context.
 *
 * @param <T> Status type (i.e. SUCCESS {@link Success}, FAILED {@link Failure}, ERROR {@link Error})
 */
public abstract class ActionExecutionStatus<T> {

    protected Status status;
    protected Map<String, Object> responseContext;

    public Status getStatus() {

        return status;
    }

    public Map<String, Object> getResponseContext() {

        return responseContext;
    }

    public abstract T getResponse();

    /**
     * This enum defines the Action Execution Status.
     */
    public enum Status {
        SUCCESS,
        FAILED,
        INCOMPLETE,
        ERROR
    }
}
