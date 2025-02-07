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

package org.wso2.carbon.identity.user.self.registration.temp.mgt;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.wso2.carbon.identity.user.self.registration.model.dto.NodeDTO;
import org.wso2.carbon.identity.user.self.registration.model.dto.RegistrationDTO;
import org.wso2.carbon.identity.user.self.registration.temp.ConfigDataHolder;
import org.wso2.carbon.identity.user.self.registration.util.Constants;

public class FlowConvertor {

    public static RegistrationDTO getSequence(String orgId) throws IOException {

        JsonNode registrationSequenceJson = loadFlowJson(orgId);
        RegistrationDTO sequence = new RegistrationDTO();

        // Parse the elements array
        JsonNode jsonNodes = registrationSequenceJson.get("nodes");
        if (jsonNodes == null || !jsonNodes.isArray()) {
            throw new IllegalArgumentException("Invalid JSON: 'nodes' is missing or not an array.");
        }

        for (JsonNode jNode : jsonNodes) {
            JsonNode actionButtons = jNode.get("actions");
            String jnodeId = jNode.get("id").asText();

            List<NodeDTO> nextActionNodeDTOS = new ArrayList<>();

            if (actionButtons == null) {
                continue;
            }

            for (JsonNode actionButton : actionButtons) {

                String actionId = actionButton.get("id").asText();

                JsonNode action = actionButton.get("action");

                if (action == null) {
                    continue;
                }

                String nextNodeId = "";
                ArrayNode nextNode = (ArrayNode) actionButton.get("next");
                if (nextNode != null) {
                    for (JsonNode next : nextNode) {
                        if (next != null) {
                            String nextNodeIdValue = next.asText();
                            if ("COMPLETE".equals(nextNodeIdValue)) {
                                NodeDTO finalNode = createUserOnboardingNode();
                                sequence.addNode(finalNode);
                                nextNodeId = finalNode.getId();
                            } else {
                                nextNodeId = nextNodeIdValue;
                            }
                            break;
                        }
                    }
                }
                if (action.get("type") == null) {
                    continue;
                }
                String actionType = action.get("type").asText();

                NodeDTO nodeDTO;
                if ("EXECUTOR".equals(actionType)) {
                    ArrayNode executorsArray = (ArrayNode) action.get("executors");
                    boolean firstExecutorInArray = true;
                    NodeDTO prevNode = null;
                    for (JsonNode executor : executorsArray) {
                        String executorName = executor.get("name").asText();
                        String instanceID = null;
                        JsonNode exMeta = executor.get("meta");
                        if (exMeta != null && exMeta.has("idp")) {
                            instanceID = exMeta.get("idp").asText();
                        }
                        if (firstExecutorInArray) {
                            nodeDTO = createExecutorNode(actionId, nextNodeId, executorName, instanceID);
                            nextActionNodeDTOS.add(nodeDTO);
                            firstExecutorInArray = false;
                            prevNode = nodeDTO;
                        } else {
                            String nextExecutorId = UUID.randomUUID().toString();
                            nodeDTO = createExecutorNode(nextExecutorId, nextNodeId, executorName, instanceID);
                            prevNode.getNextNodes().remove(nextNodeId);
                            prevNode.addNextNode(nextExecutorId);
                            sequence.addNode(nodeDTO);
                        }
                    }
                } else if ("RULE".equals(actionType)) {
                    System.out.println("Info: Create Rule Node for " + actionId);
                } else if ("NEXT".equals(actionType)) {
                    if (action.has("meta")) {
                        String pageActionType = action.has("meta") && action.get("meta").has("actionType")
                                ? action.get("meta").get("actionType").asText()
                                : "INIT";

                        for (NodeDTO node : sequence.getNodes().values()) {
                            if (node.getNextNodes().contains(jnodeId)) {
                                node.getNextNodes().remove(jnodeId);
                                node.addNextNode(nextNodeId);
                                node.addPageIds(pageActionType, jnodeId);
                            }
                        }
                    } else {
                        nodeDTO = createInputCollectorNode(actionId, nextNodeId);
                        nextActionNodeDTOS.add(nodeDTO);
                    }
                }
            }

            if (nextActionNodeDTOS.size() > 1) {
                NodeDTO decisionNodeDTO = createDecisionNode();
                if (firstNode == null) {
                    firstNode = decisionNodeDTO.getId();
                }
                nextActionNodeDTOS.forEach(nodeDTO -> decisionNodeDTO.addNextNode(nodeDTO.getId()));
                decisionNodeDTO.addPageIds("INIT", jnodeId);
                nextActionNodeDTOS.forEach(sequence::addNode);
                for (NodeDTO node : sequence.getNodes().values()) {
                    if (node.getNextNodes().contains(jnodeId)) {
                        node.getNextNodes().remove(jnodeId);
                        node.addNextNode(decisionNodeDTO.getId());
                    }
                }
                sequence.addNode(decisionNodeDTO);
            } else if (nextActionNodeDTOS.size() == 1) {
                NodeDTO nextNodeDTO = nextActionNodeDTOS.get(0);
                if (firstNode == null) {
                    firstNode = nextNodeDTO.getId();
                }
                nextNodeDTO.addPageIds("INIT", jnodeId);
                for (NodeDTO node : sequence.getNodes().values()) {
                    if (node.getNextNodes().contains(jnodeId)) {
                        node.getNextNodes().remove(jnodeId);
                        node.addNextNode(nextNodeDTO.getId());
                    }
                }
                sequence.addNode(nextNodeDTO);
            }
        }

        sequence.setFirstNode(firstNode);
        return sequence;
    }

    private static JsonNode loadFlowJson(String orgId) throws IOException {

        if (Constants.NEW_FLOW.equalsIgnoreCase(orgId)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = ConfigDataHolder.getInstance().getOrchestrationConfig().get("carbon.super");
            if (jsonString == null) {
                throw new IllegalArgumentException("Flow not found. Make sure the flow is added through the /reg-orchestration/config API");
            }
            return objectMapper.readTree(jsonString);
        } else {
            throw new IllegalArgumentException("Flow not found.");
        }
    }

    private static NodeDTO createInputCollectorNode(String nodeId, String nextNodeId) {

        NodeDTO node = new NodeDTO(nodeId, "INPUT");
        node.addNextNode(nextNodeId);
        return node;
    }

    private static NodeDTO createDecisionNode() {

        String id = UUID.randomUUID().toString();
        return new NodeDTO(id, "DECISION");
    }

    private static NodeDTO createUserOnboardingNode() {

        String id = UUID.randomUUID().toString();
        NodeDTO node = new NodeDTO(id, "EXECUTOR");
        node.addProperty("EXECUTOR_NAME", "user-onboarding-executor");
        return node;
    }

    private static NodeDTO createExecutorNode(String id, String nextNodeId, String exName, String instanceID) {

        NodeDTO node = new NodeDTO(id, "EXECUTOR");
        node.addNextNode(nextNodeId);
        node.addProperty("EXECUTOR_NAME", exName);
        if (instanceID != null) {
            node.addProperty("EXECUTOR_ID", instanceID);
        }
        return node;
    }
}
