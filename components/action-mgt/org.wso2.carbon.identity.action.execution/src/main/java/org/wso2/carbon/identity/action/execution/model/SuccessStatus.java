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
 * This class models the SuccessStatus.
 */
public class SuccessStatus extends ActionExecutionStatus<Success> {

    private final Success success;

    private SuccessStatus(Builder builder) {

        this.status = Status.SUCCESS;
        this.success = builder.success;
        this.responseContext = builder.responseContext;
    }

    @Override
    public Success getResponse() {

        return this.success;
    }

    /**
     * This class is the builder for SuccessStatus.
     */
    public static class Builder {

        private Success success;
        private Map<String, Object> responseContext;

        public Builder setSuccess(Success success) {

            this.success = success;
            return this;
        }

        public Builder setResponseContext(Map<String, Object> responseContext) {

            this.responseContext = responseContext;
            return this;
        }

        public SuccessStatus build() {

            return new SuccessStatus(this);
        }
    }
}
