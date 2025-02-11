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

package org.wso2.carbon.identity.user.self.registration.node;

import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.util.Constants;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_INPUT_REQUIRED;

/**
 * Implementation of a node specific to prompting user to select a choice out of multiple registration executor options.
 */
public class UserChoiceDecisionNode extends AbstractNode {

    private static final String NODE_INPUT = "action-id";
    private List<Node> nextNodes = new ArrayList<>(); // For branching paths

    public UserChoiceDecisionNode() {

        super();
    }

    public UserChoiceDecisionNode(String id) {

        super(id);
    }

    /**
     * Set the nodes that are available for the user to choose from.
     *
     * @param nextNodes List of Task Executor Nodes.
     */
    public void setNextNodes(List<Node> nextNodes) {

        this.nextNodes = nextNodes;
    }

    /**
     * Get the nodes that are available for the user to choose from.
     *
     * @return List of Task Executor Nodes.
     */
    public List<Node> getNextNodes() {

        return this.nextNodes;
    }

    /**
     * Add a node to the list of nodes available for the user to choose from.
     *
     * @param node Task Executor Node.
     */
    public void addNextNodeId(Node node) {

        this.nextNodes.add(node);
    }

    @Override
    public NodeResponse execute(RegistrationContext context) throws RegistrationServerException {

        Map<String, String> inputData = context.getUserInputData();

        if (inputData != null && inputData.containsKey(NODE_INPUT)) {
            String selectedNode = inputData.get(NODE_INPUT);
            for (Node nextNode : nextNodes) {
                if (nextNode.getNodeId().equals(selectedNode)) {
                    setNextNodeId(selectedNode);
                    break;
                }
            }
            if (getNextNodeId() == null) {
                throw new RegistrationServerException("Cannot find a valid node to proceed.");
            }
        }
        if (getNextNodeId() != null) {
            return new NodeResponse(Constants.STATUS_NODE_COMPLETE);
        }
        return new NodeResponse(STATUS_USER_INPUT_REQUIRED);
    }
}
