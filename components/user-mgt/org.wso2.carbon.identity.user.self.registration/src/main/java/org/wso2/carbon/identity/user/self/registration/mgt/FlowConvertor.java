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

package org.wso2.carbon.identity.user.self.registration.mgt;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.NodeDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.RegistrationDTO;

public class FlowConvertor {

    private static final Log LOG = LogFactory.getLog(FlowConvertor.class);


    public static void createRegistrationFlow() {

//        RegistrationDTO registrationDTO = adapt();
//        if (registrationDTO == null) {
//            LOG.error("Error while converting the registration flow.");
//            return; // Return if the registration flow is not created
//        }
//        RegistrationFlowDAO.insertRegistrationFlow()
    }

    private static JsonNode loadFlow(String flowId) throws IOException {

        String fileName = flowId + ".json";
        InputStream inputStream = FlowConvertor.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(inputStream);
    }

    public static RegistrationDTO adapt(String flowId) throws IOException {

        JsonNode registrationSequenceJson = loadFlow(flowId);
        RegistrationDTO sequence = new RegistrationDTO();
        String firstNode = null;


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
                System.out.println("Info: Actions null for " + jnodeId);
                continue;
            }

            for (JsonNode actionButton : actionButtons) {

                String actionId = actionButton.get("id").asText();

                JsonNode action = actionButton.get("action");

                if (action == null) {
                    System.out.println("Info2: Actions null for " + jnodeId);
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
                                nextNodeId = finalNode.getId();
                            } else {
                                nextNodeId = nextNodeIdValue;
                            }
                            break;
                        }
                    }
                }
                if (action.get("type") == null) {
                    System.out.println("Info: ActionDetails type null for " + actionId);
                    continue;
                }
                String actionType = action.get("type").asText();

                NodeDTO nodeDTO;
                if ("EXECUTOR".equals(actionType)) {
                    ArrayNode executorsArray = (ArrayNode) action.get("executors");
                    boolean firstExecutorInArray = true;
                    NodeDTO prevNode = null;
                    for (JsonNode executor : executorsArray) {
                        String executorID = executor.get("id").asText();
                        String executorName = executor.get("name").asText();
                        if (firstExecutorInArray) {
                            nodeDTO = createExecutorNode(actionId, nextNodeId, executorID, executorName);
                            nextActionNodeDTOS.add(nodeDTO);
                            firstExecutorInArray = false;
                            prevNode = nodeDTO;
                        } else {
                            String nextExecutorId = UUID.randomUUID().toString();
                            nodeDTO = createExecutorNode(nextExecutorId, nextNodeId, executorID, executorName);
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
                                System.out.println(
                                        "NEXT action found. Found node " + node.getId() + " with next node " + jnodeId);
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
                        System.out.println("Info: Found node " + node.getId() + " with next node " + jnodeId);
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
                        System.out.println("Info: Found node " + node.getId() + " with next node " + jnodeId);
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
        return new NodeDTO(id, "USER_ONBOARDING");
    }

    private static NodeDTO createExecutorNode(String id, String nextNodeId, String executorID, String exName) {

        System.out.println("Info: Create Executor Node for " + id);
        NodeDTO node = new NodeDTO(id, "EXECUTOR");
        node.addNextNode(nextNodeId);
        node.addProperty("EXECUTOR_ID", executorID);
        node.addProperty("EXECUTOR_NAME", exName);
        return node;
    }
}
