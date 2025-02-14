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

package org.wso2.carbon.identity.user.registration.mgt.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.BlockDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ElementDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.PageDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlowConvertorOld {

    // Define a constant to LOG the information.
    private static final Log LOG = LogFactory.getLog(FlowConvertorOld.class);

    public static RegistrationFlowConfig getSequence(String flowJson) throws IOException {

        JsonNode registrationSequenceJson = new ObjectMapper().readTree(flowJson);
        RegistrationFlowConfig registrationFlowConfig = new RegistrationFlowConfig();
        registrationFlowConfig.setId(UUID.randomUUID().toString());
        registrationFlowConfig.setFlowJson(flowJson);

        Map<String, ElementDTO> elementDTOMap = processElements(registrationSequenceJson);
        Map<String, BlockDTO> blockDTOMap = processBlocks(registrationSequenceJson);

        // Parse the elements array
        JsonNode jsonNodes = registrationSequenceJson.get(Constants.FlowElements.NODES);
        if (jsonNodes == null || !jsonNodes.isArray()) {
            throw new IllegalArgumentException("Invalid JSON: 'nodes' is missing or not an array.");
        }

        for (JsonNode jNode : jsonNodes) {
            JsonNode actionButtons = jNode.get(Constants.FlowElements.ACTIONS);
            String jnodeId = jNode.get(Constants.FlowElements.ID).asText();

            List<NodeConfig> nextActionNodeDTOS = new ArrayList<>();

            if (actionButtons == null) {
                LOG.info("No action buttons found for the node: " + jnodeId);
                continue;
            }

            for (JsonNode actionButton : actionButtons) {
                String actionId = actionButton.get(Constants.FlowElements.ID).asText();
                JsonNode action = actionButton.get(Constants.FlowElements.ACTION);

                if (action == null) {
                    continue;
                }

                String nextNodeId = getNextNodeId(actionButton);
                if (action.get(Constants.FlowElements.TYPE) == null) {
                    continue;
                }
                String actionType = action.get(Constants.FlowElements.TYPE).asText();
                processActionType(registrationFlowConfig, nextActionNodeDTOS, actionId, nextNodeId, actionType, action,
                                  elementDTOMap);
            }

            String nodeIdMappedForPage =
                    updateSequenceWithNextActionNodes(registrationFlowConfig, nextActionNodeDTOS, jnodeId);
            String pageContent = processElementsInNode(jNode, elementDTOMap, blockDTOMap);
            registrationFlowConfig.addNodePageMapping(nodeIdMappedForPage, pageContent);
        }

        return registrationFlowConfig;
    }

    private static NodeConfig createInputCollectorNode(String nodeId, String nextNodeId) {

        NodeConfig node = new NodeConfig();
        node.setId(nodeId);
        node.setType(Constants.NodeTypes.PROMPT);
        node.addNextNodeId(nextNodeId);
        return node;
    }

    private static NodeConfig createDecisionNode() {

        String id = UUID.randomUUID().toString();
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setId(id);
        nodeConfig.setType(Constants.NodeTypes.DECISION);
        return nodeConfig;
    }

    private static NodeConfig createUserOnboardingNode() {

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setId(UUID.randomUUID().toString());
        nodeConfig.setType(Constants.NodeTypes.TASK_EXECUTION);
        ExecutorDTO executorConfig = new ExecutorDTO(Constants.EXECUTOR_FOR_USER_ONBOARDING);
        nodeConfig.setExecutorConfig(executorConfig);
        return nodeConfig;
    }

    private static NodeConfig createExecutorNode(String id, String nextNodeId, String exName, String instanceID) {

        NodeConfig node = new NodeConfig();
        node.setId(id);
        node.setType(Constants.NodeTypes.TASK_EXECUTION);
        node.addNextNodeId(nextNodeId);
        ExecutorDTO executorConfig = new ExecutorDTO();
        executorConfig.setName(exName);
        if (instanceID != null) {
            executorConfig.setAuthenticatorId(instanceID);
        }
        node.setExecutorConfig(executorConfig);
        return node;
    }

    private static Map<String, ElementDTO> processElements(JsonNode rootNode) throws JsonProcessingException {

        // Retrieve all the elements in the flow.
        JsonNode elementsArray = rootNode.get("elements");
        if (elementsArray == null || !elementsArray.isArray()) {
            System.out.println("'elements' array not found or is not an array.");
            return null;
        }
        Map<String, ElementDTO> elementDTOMap = new HashMap<>();
        for (JsonNode element : elementsArray) {

            String elementId = element.get("id").asText();
            String category = element.get("category").asText();
            String type = element.get("type").asText();
            String variant = element.get("variant").asText();

            ElementDTO elementDTO = new ElementDTO(elementId, category, type, variant);

            // Process config object in the element
            JsonNode config = element.get("config");
            if (config == null) {
                System.out.println("No config found for this element " + elementId);
            } else {
                JsonNode fieldObj = config.get("field");
                Iterator<Map.Entry<String, JsonNode>> fields = fieldObj.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    Object value;
                    if (field.getValue().isBoolean()) {
                        value = field.getValue().asBoolean();
                    } else if (field.getValue().isInt()) {
                        value = field.getValue().asInt();
                    } else if (field.getValue().isObject()) {
                        String json = field.getValue().toString();

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(json);
                        value = mapper.convertValue(jsonNode, Object.class);
                    } else if (field.getValue().isArray()) {
                        String jsonArray = field.getValue().toString();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(jsonArray);
                        // Convert JsonNode Array to List of Objects
                        value = mapper.convertValue(jsonNode, List.class);
                    } else {
                        value = field.getValue().asText();
                    }
                    elementDTO.addProperty(field.getKey(), value);
                }
            }
            elementDTOMap.put(elementId, elementDTO);
        }
        return elementDTOMap;
    }

    private static Map<String, String> processElementsAsJsonString(JsonNode rootNode) throws JsonProcessingException {

        // Retrieve all the elements in the flow.
        JsonNode elementsArray = rootNode.get("elements");
        if (elementsArray == null || !elementsArray.isArray()) {
            System.out.println("'elements' array not found or is not an array.");
            return null;
        }
        Map<String, String> elementsJsonMap = new HashMap<>();
        for (JsonNode element : elementsArray) {

            String elementId = element.get("id").asText();
            elementsJsonMap.put(elementId, new ObjectMapper().writeValueAsString(element));
        }
        return elementsJsonMap;
    }

    private static Map<String, String> processBlocksAsJsonString(JsonNode rootNode) throws JsonProcessingException {

        // Retrieve all the blocks in the flow.
        JsonNode blocksArray = rootNode.get("blocks");
        if (blocksArray == null || !blocksArray.isArray()) {
            System.out.println("'blocks' array not found or is not an array.");
            return null;
        }
        Map<String, String> blocksJsonMap = new HashMap<>();
        for (JsonNode block : blocksArray) {

            String blockId = block.get("id").asText();
            blocksJsonMap.put(blockId, new ObjectMapper().writeValueAsString(block) );
        }
        return blocksJsonMap;
    }


    private static Map<String, BlockDTO> processBlocks(JsonNode rootNode) {

        // Retrieve all the blocks in the flow.
        JsonNode blocksArray = rootNode.get("blocks");
        if (blocksArray == null || !blocksArray.isArray()) {
            System.out.println("'blocks' array not found or is not an array.");
            return null;
        }
        Map<String, BlockDTO> blockDTOMap = new HashMap<>();
        for (JsonNode block : blocksArray) {

            String blockId = block.get("id").asText();
            BlockDTO blockDTO = new BlockDTO();
            blockDTO.setId(blockId);

            JsonNode blockElements = block.get("elements");
            for (JsonNode blockElement : blockElements) {
                blockDTO.addElementId(blockElement.asText());
            }
            blockDTOMap.put(blockId, blockDTO);
        }
        return blockDTOMap;
    }

    private static String getNextNodeId(JsonNode actionButton) {

        String nextNodeId = "";
        ArrayNode nextNode = (ArrayNode) actionButton.get(Constants.FlowElements.NEXT);
        if (nextNode != null) {
            for (JsonNode next : nextNode) {
                if (next != null) {
                    String nextNodeIdValue = next.asText();
                    if (Constants.COMPLETE.equals(nextNodeIdValue)) {
                        NodeConfig finalNode = createUserOnboardingNode();
                        nextNodeId = finalNode.getId();
                    } else {
                        nextNodeId = nextNodeIdValue;
                    }
                    break;
                }
            }
        }
        return nextNodeId;
    }

    private static void processActionType(RegistrationFlowConfig sequence, List<NodeConfig> nextActionNodeDTOS,
                                          String actionId,
                                          String nextNodeId, String actionType, JsonNode action,
                                          Map<String, ElementDTO> elementDTOMap) {

        if (Constants.EXECUTOR.equals(actionType)) {
            processExecutorAction(sequence, nextActionNodeDTOS, actionId, nextNodeId, action, elementDTOMap);
        } else if (Constants.RULE.equals(actionType)) {
            LOG.info("Rule components are not yet supported.");
        } else if (Constants.NEXT.equals(actionType)) {
            processNextAction(sequence, nextActionNodeDTOS, actionId, nextNodeId, action, elementDTOMap);
        }
    }

    private static void processExecutorAction(RegistrationFlowConfig sequence, List<NodeConfig> nextActionNodeDTOS,
                                              String actionId, String nextNodeId, JsonNode action,
                                              Map<String, ElementDTO> elementDTOMap) {

        ArrayNode executorsArray = (ArrayNode) action.get(Constants.FlowElements.EXECUTORS);
        boolean firstExecutorInArray = true;
        NodeConfig prevNode = null;
        for (JsonNode executor : executorsArray) {
            String executorName = executor.get(Constants.FlowElements.NAME).asText();
            String instanceID = null;
            JsonNode exMeta = executor.get(Constants.FlowElements.META);
            if (exMeta != null && exMeta.has(Constants.FlowElements.IDP)) {
                instanceID = exMeta.get(Constants.FlowElements.IDP).asText();
            }
            if (firstExecutorInArray) {
                NodeConfig nodeDTO = createExecutorNode(actionId, nextNodeId, executorName, instanceID);
                nextActionNodeDTOS.add(nodeDTO);
                firstExecutorInArray = false;
                prevNode = nodeDTO;
            } else {
                String nextExecutorId = UUID.randomUUID().toString();
                NodeConfig nodeDTO = createExecutorNode(nextExecutorId, nextNodeId, executorName, instanceID);
                prevNode.getNextNodeIds().remove(nextNodeId);
                prevNode.addNextNodeId(nextExecutorId);
                sequence.addNodeConfig(nodeDTO);
            }
        }
        updateElementWithAction(actionId, Constants.EXECUTOR, elementDTOMap, executorsArray);
    }

    private static void processNextAction(RegistrationFlowConfig sequence, List<NodeConfig> nextActionNodeDTOS, String actionId,
                                          String nextNodeId, JsonNode action, Map<String, ElementDTO> elementDTOMap) {

        NodeConfig nodeDTO = createInputCollectorNode(actionId, nextNodeId);
        nextActionNodeDTOS.add(nodeDTO);
        updateElementWithAction(actionId, Constants.NEXT, elementDTOMap, null);
    }

    private static void updateElementWithAction(String actionId, String actionType,
                                                Map<String, ElementDTO> elementDTOMap, ArrayNode executorsArray) {

        ActionDTO actionDTO = new ActionDTO(actionType);
        if (Constants.EXECUTOR.equals(actionType) && executorsArray != null) {
            JsonNode firstExecutor = executorsArray.get(0);
            String idp = null;
            if (firstExecutor.get(Constants.FlowElements.META) != null) {
                JsonNode exMeta = firstExecutor.get(Constants.FlowElements.META);
                if (exMeta != null && exMeta.has(Constants.FlowElements.IDP)) {
                    idp = exMeta.get(Constants.FlowElements.IDP).asText();
                }
            }
            String exName = firstExecutor.path(Constants.FlowElements.NAME).asText();
            ExecutorDTO executorDTO = new ExecutorDTO(exName, idp);
            actionDTO.addExecutor(executorDTO);
        }
        if (elementDTOMap != null && elementDTOMap.containsKey(actionId)) {
            elementDTOMap.get(actionId).setAction(actionDTO);
        }
    }

    private static String processElementsInNode(JsonNode node, Map<String, ElementDTO> elementDTOMap,
                                              Map<String, BlockDTO> blockDTOMap) throws JsonProcessingException {

        PageDTO pageDTO = new PageDTO();
        JsonNode elements = node.get("elements");
        if (elements != null && elements.isArray()) {
            for (JsonNode element : elements) {
                String elementId = element.asText();
                if (elementId.startsWith("flow-block")) {
                    BlockDTO blockDTO = blockDTOMap.get(elementId);
                    if (blockDTO != null) {
                        pageDTO.addBlock(blockDTO);
                        for (String blockElementId : blockDTO.getElementIds()) {
                            if (elementDTOMap != null && elementDTOMap.containsKey(blockElementId)) {
                                pageDTO.addElement(elementDTOMap.get(blockElementId));
                            }
                        }
                    }
                } else {
                    if (elementDTOMap != null && elementDTOMap.containsKey(elementId)) {
                        pageDTO.addElement(elementDTOMap.get(elementId));
                    }
                }
            }
        }

        return new ObjectMapper().writeValueAsString(pageDTO);
    }

    private static String updateSequenceWithNextActionNodes(RegistrationFlowConfig sequence,
                                                          List<NodeConfig> nextActionNodeDTOS,
                                                          String jnodeId) {

        String nodeIdMappedToPrompt = null;
        if (nextActionNodeDTOS.size() > 1) {
            NodeConfig decisionNodeDTO = createDecisionNode();
            nodeIdMappedToPrompt = decisionNodeDTO.getId();
            if (sequence.getFirstNodeId() == null) {
                sequence.setFirstNodeId(decisionNodeDTO.getId());
                decisionNodeDTO.setFirstNode(true);
            }
            nextActionNodeDTOS.forEach(nodeDTO -> decisionNodeDTO.addNextNodeId(nodeDTO.getId()));
            nextActionNodeDTOS.forEach(sequence::addNodeConfig);
            for (Map.Entry<String, NodeConfig> entry : sequence.getNodeConfigs().entrySet()) {
                NodeConfig node = entry.getValue();
                if (node.getNextNodeIds().contains(jnodeId)) {
                    node.getNextNodeIds().remove(jnodeId);
                    node.addNextNodeId(decisionNodeDTO.getId());
                }
            }
            sequence.addNodeConfig(decisionNodeDTO);
        } else if (nextActionNodeDTOS.size() == 1) {
            NodeConfig nextNodeDTO = nextActionNodeDTOS.get(0);
            if (sequence.getFirstNodeId() == null) {
                sequence.setFirstNodeId(nextNodeDTO.getId());
                nextNodeDTO.setFirstNode(true);
            }
            nodeIdMappedToPrompt = nextNodeDTO.getId();
            for (Map.Entry<String, NodeConfig> entry : sequence.getNodeConfigs().entrySet()) {
                NodeConfig node = entry.getValue();
                if (node.getNextNodeIds().contains(jnodeId)) {
                    node.getNextNodeIds().remove(jnodeId);
                    node.addNextNodeId(nextNodeDTO.getId());
                }
            }
            sequence.addNodeConfig(nextNodeDTO);
        }
        return nodeIdMappedToPrompt;
    }
}
