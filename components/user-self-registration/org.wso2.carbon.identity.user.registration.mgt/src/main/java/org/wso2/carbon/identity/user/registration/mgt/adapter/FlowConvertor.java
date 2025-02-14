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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.Constants.FlowElements;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.BlockDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ElementDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.PageDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.EXECUTOR;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.FlowElements.*;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NEXT;

public class FlowConvertor {

    // Define a constant to LOG the information.
    private static final Log LOG = LogFactory.getLog(FlowConvertor.class);

    public static RegistrationFlowConfig getSequence(RegistrationFlowDTO flowDTO) throws IOException {

        RegistrationFlowConfig registrationFlowConfig = new RegistrationFlowConfig();
        for (StepDTO step : flowDTO.getSteps()) {
            String stepId = step.getId();

            List<NodeConfig> nextActionNodeDTOS = new ArrayList<>();

            for (Map.Entry<String, ActionDTO> entry : step.getActions().entrySet()) {

                ActionDTO action = entry.getValue();
                if (TYPE.equals(action.getType())) {
                    continue;
                }
                processActionType(registrationFlowConfig, nextActionNodeDTOS, action, flowDTO.getElementDTOMap());
            }

            String nodeIdMappedForPage =
                    updateSequenceWithNextActionNodes(registrationFlowConfig, nextActionNodeDTOS, stepId);
            String pageContent = processElementsInStep(step.getElementIds(), flowDTO.getElementDTOMap(), flowDTO.getBlockDTOMap());
            registrationFlowConfig.addNodePageMapping(nodeIdMappedForPage, pageContent);
        }

        return registrationFlowConfig;
    }

    private static NodeConfig createInputCollectorNode(ActionDTO actionDTO) {

        NodeConfig node = new NodeConfig();
        node.setId(actionDTO.getRef());
        node.setType(Constants.NodeTypes.PROMPT);
        node.addNextNodeId(actionDTO.getNextId());
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

    private static NodeConfig createExecutorNode(String nodeId, String nextId, ExecutorDTO executorDTO) {

        NodeConfig node = new NodeConfig();
        node.setId(nodeId);
        node.setType(Constants.NodeTypes.TASK_EXECUTION);
        node.addNextNodeId(nextId);
        node.setExecutorConfig(executorDTO);
        return node;
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
        ArrayNode nextNode = (ArrayNode) actionButton.get(FlowElements.NEXT);
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
                                          ActionDTO action, Map<String, ElementDTO> elementDTOMap) {

        if (EXECUTOR.equals(action.getType())) {
            processExecutorAction(sequence, nextActionNodeDTOS, action, elementDTOMap);
        } else if (NEXT.equals(action.getType())) {
            processNextAction(sequence, nextActionNodeDTOS, action, elementDTOMap);
        }
    }

    private static void processExecutorAction(RegistrationFlowConfig sequence, List<NodeConfig> nextActionNodeDTOS, ActionDTO actionDTO,
                                              Map<String, ElementDTO> elementDTOMap) {

        boolean firstExecutorInArray = true;
        NodeConfig prevNode = null;
        for (ExecutorDTO executor : actionDTO.getExecutors()) {
            if (firstExecutorInArray) {
                NodeConfig nodeDTO = createExecutorNode(actionDTO.getRef(), actionDTO.getNextId(), executor);
                nextActionNodeDTOS.add(nodeDTO);
                firstExecutorInArray = false;
                prevNode = nodeDTO;
            } else {
                String nextExecutorId = UUID.randomUUID().toString();
                NodeConfig nodeDTO = createExecutorNode(nextExecutorId, actionDTO.getNextId(), executor);
                prevNode.getNextNodeIds().remove(actionDTO.getNextId());
                prevNode.addNextNodeId(nextExecutorId);
                sequence.addNodeConfig(nodeDTO);
            }
        }
        updateElementWithAction(actionDTO, elementDTOMap);
    }

    private static void processNextAction(RegistrationFlowConfig sequence, List<NodeConfig> nextActionNodeDTOS,
                                          ActionDTO action, Map<String, ElementDTO> elementDTOMap) {

        NodeConfig nodeDTO = createInputCollectorNode(action);
        nextActionNodeDTOS.add(nodeDTO);
        updateElementWithAction(action, elementDTOMap);
        sequence.addNodeConfig(nodeDTO);
    }

    // todo do we really need this?
    private static void updateElementWithAction(ActionDTO action, Map<String, ElementDTO> elementDTOMap) {

        if (EXECUTOR.equals(action.getType()) && action.getExecutors() != null) {
            if (elementDTOMap != null && elementDTOMap.containsKey(action.getRef())) {
                elementDTOMap.get(action.getRef()).setAction(action);
            }
        }
    }

    private static String processElementsInStep(List<String> elementsInStep,
                                                Map<String, ElementDTO> elementDTOMap,
                                                Map<String, BlockDTO> blockDTOMap) throws JsonProcessingException {

        PageDTO pageDTO = new PageDTO();
        for (String elementId : elementsInStep) {
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
