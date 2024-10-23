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

package org.wso2.carbon.identity.user.self.registration.action;

import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.model.ExecutorResponse;
import org.wso2.carbon.identity.user.self.registration.model.InitData;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

/**
 * Interface for attribute collection.
 */
public interface AttributeCollection extends Executor {

    /**
     * Perform attribute collection from the given input and update the context.
     *
     * @param input  Input data.
     * @param context Registration context.
     * @return  ExecutorResponse.
     */
    ExecutorResponse collect(Map<String, String> input, RegistrationContext context);

    /**
     * Get the initial data required for attribute collection.
     *
     * @return initial data required for attribute collection.
     */
    InitData getAttrCollectInitData();
}
