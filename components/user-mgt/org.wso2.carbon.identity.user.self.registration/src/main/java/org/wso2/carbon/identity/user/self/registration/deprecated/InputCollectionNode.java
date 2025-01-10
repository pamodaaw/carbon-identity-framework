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

package org.wso2.carbon.identity.user.self.registration.deprecated;

import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.node.Node;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;

import java.util.List;

/**
 * Interface for a node in the registration flow graph that collects input data.
 */
@Deprecated
public interface InputCollectionNode extends Node {

    /**
     * Get the list of data required for the node along with the metadata.
     *
     * @return  The list of data required for the node along with the metadata.
     */
    List<InputMetaData> getRequiredData() throws RegistrationFrameworkException;

}
