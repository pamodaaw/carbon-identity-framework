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

package org.wso2.carbon.identity.user.registration.engine.node;

import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.engine.model.NodeResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;

/**
 * Abstract node implementation for the nodes in the registration flow graph.
 */
public abstract class AbstractNode implements Node {

    private String nextNodeId;
    private String previousNodeId;

    public String getNextNodeId() {

        return this.nextNodeId;
    }

    public void setNextNodeId(String nextNodeId) {

        this.nextNodeId = nextNodeId;

    }

    public String getPreviousNodeID() {

        return this.previousNodeId;
    }

    public NodeResponse rollback(RegistrationContext context) throws RegistrationFrameworkException {


        return null;
    }
}
