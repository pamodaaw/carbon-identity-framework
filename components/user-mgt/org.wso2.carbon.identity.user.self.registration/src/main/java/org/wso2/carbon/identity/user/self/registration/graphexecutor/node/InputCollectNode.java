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

package org.wso2.carbon.identity.user.self.registration.graphexecutor.node;

import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_PROMPT_ONLY;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

/**
 * Node to combine input requirements of multiple nodes and prompt.
 */
public class InputCollectNode extends AbstractNode {

    public InputCollectNode(String id) {

         super(id);
    }

    @Override
    public NodeResponse execute(RegistrationContext context) throws RegistrationFrameworkException {

        // Only declare the data required. So this node is complete.
        return new NodeResponse(STATUS_PROMPT_ONLY);
    }
}
