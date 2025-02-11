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

package org.wso2.carbon.identity.user.self.registration.mgt.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.self.registration.mgt.Constants;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.ActionDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.BlockDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.ElementDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.ExecutorDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.MetaDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.NodeDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.PageDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.RegistrationDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlowConvertor {

    // Define a constant to LOG the information.
    private static final Log LOG = LogFactory.getLog(FlowConvertor.class);

    public static RegistrationDTO getSequence(String flowJson) throws IOException {

        JsonNode registrationSequenceJson = new ObjectMapper().readTree(flowJson);
        RegistrationDTO sequence = new RegistrationDTO();
        sequence.setFlowJson(flowJson);

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

            PageDTO pageDTO = new PageDTO();
            pageDTO.setId(jnodeId);

            List<NodeDTO> nextActionNodeDTOS = new ArrayList<>();

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
                processActionType(sequence, nextActionNodeDTOS, actionId, nextNodeId, actionType, action,
                                  elementDTOMap);
            }

            updateSequenceWithNextActionNodes(sequence, nextActionNodeDTOS, jnodeId);
            processElementsInNode(jNode, pageDTO, elementDTOMap, blockDTOMap);
            sequence.addPageDTO(pageDTO);
        }

        return sequence;
    }

    private static NodeDTO createInputCollectorNode(String nodeId, String nextNodeId) {

        NodeDTO node = new NodeDTO(nodeId, Constants.NodeTypes.PROMPT);
        node.addNextNode(nextNodeId);
        return node;
    }

    private static NodeDTO createDecisionNode() {

        String id = UUID.randomUUID().toString();
        return new NodeDTO(id, Constants.NodeTypes.DECISION);
    }

    private static NodeDTO createUserOnboardingNode() {

        String id = UUID.randomUUID().toString();
        NodeDTO node = new NodeDTO(id, Constants.NodeTypes.TASK_EXECUTION);
        node.addProperty(Constants.EXECUTOR_TYPE, "user-onboarding-executor");
        return node;
    }

    private static NodeDTO createExecutorNode(String id, String nextNodeId, String exName, String instanceID) {

        NodeDTO node = new NodeDTO(id, Constants.NodeTypes.TASK_EXECUTION);
        node.addNextNode(nextNodeId);
        node.addProperty(Constants.EXECUTOR_TYPE, exName);
        if (instanceID != null) {
            node.addProperty(Constants.EXECUTOR_ID, instanceID);
        }
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
                        NodeDTO finalNode = createUserOnboardingNode();
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

    private static void processActionType(RegistrationDTO sequence, List<NodeDTO> nextActionNodeDTOS, String actionId,
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

    private static void processExecutorAction(RegistrationDTO sequence, List<NodeDTO> nextActionNodeDTOS,
                                              String actionId, String nextNodeId, JsonNode action,
                                              Map<String, ElementDTO> elementDTOMap) {

        ArrayNode executorsArray = (ArrayNode) action.get(Constants.FlowElements.EXECUTORS);
        boolean firstExecutorInArray = true;
        NodeDTO prevNode = null;
        for (JsonNode executor : executorsArray) {
            String executorName = executor.get(Constants.FlowElements.NAME).asText();
            String instanceID = null;
            JsonNode exMeta = executor.get(Constants.FlowElements.META);
            if (exMeta != null && exMeta.has(Constants.FlowElements.IDP)) {
                instanceID = exMeta.get(Constants.FlowElements.IDP).asText();
            }
            if (firstExecutorInArray) {
                NodeDTO nodeDTO = createExecutorNode(actionId, nextNodeId, executorName, instanceID);
                nextActionNodeDTOS.add(nodeDTO);
                firstExecutorInArray = false;
                prevNode = nodeDTO;
            } else {
                String nextExecutorId = UUID.randomUUID().toString();
                NodeDTO nodeDTO = createExecutorNode(nextExecutorId, nextNodeId, executorName, instanceID);
                prevNode.getNextNodes().remove(nextNodeId);
                prevNode.addNextNode(nextExecutorId);
                sequence.addNode(nodeDTO);
            }
        }
        updateElementWithAction(actionId, Constants.EXECUTOR, elementDTOMap, executorsArray);
    }

    private static void processNextAction(RegistrationDTO sequence, List<NodeDTO> nextActionNodeDTOS, String actionId,
                                          String nextNodeId, JsonNode action, Map<String, ElementDTO> elementDTOMap) {

        NodeDTO nodeDTO = createInputCollectorNode(actionId, nextNodeId);
        nextActionNodeDTOS.add(nodeDTO);
        updateElementWithAction(actionId, Constants.NEXT, elementDTOMap, null);
    }

    private static void updateElementWithAction(String actionId, String actionType,
                                                Map<String, ElementDTO> elementDTOMap, ArrayNode executorsArray) {

        ActionDTO actionDTO = new ActionDTO(actionType);
        if (Constants.EXECUTOR.equals(actionType) && executorsArray != null) {
            JsonNode firstExecutor = executorsArray.get(0);
            MetaDTO metaDTO = null;
            if (firstExecutor.get(Constants.FlowElements.META) != null) {
                JsonNode exMeta = firstExecutor.get(Constants.FlowElements.META);
                if (exMeta != null && exMeta.has(Constants.FlowElements.IDP)) {
                    metaDTO = new MetaDTO(exMeta.get(Constants.FlowElements.IDP).asText());
                }
            }
            String exName = firstExecutor.path(Constants.FlowElements.NAME).asText();
            ExecutorDTO executorDTO = new ExecutorDTO(exName, metaDTO);
            actionDTO.addExecutor(executorDTO);
        }
        if (elementDTOMap != null && elementDTOMap.containsKey(actionId)) {
            elementDTOMap.get(actionId).setAction(actionDTO);
        }
    }

    private static void processElementsInNode(JsonNode node, PageDTO pageDTO, Map<String, ElementDTO> elementDTOMap,
                                              Map<String, BlockDTO> blockDTOMap) {

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
    }

    private static void updateSequenceWithNextActionNodes(RegistrationDTO sequence, List<NodeDTO> nextActionNodeDTOS,
                                                          String jnodeId) {

        if (nextActionNodeDTOS.size() > 1) {
            NodeDTO decisionNodeDTO = createDecisionNode();
            if (sequence.getFirstNode() == null) {
                sequence.setFirstNode(decisionNodeDTO.getId());
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
            if (sequence.getFirstNode() == null) {
                sequence.setFirstNode(nextNodeDTO.getId());
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
}
