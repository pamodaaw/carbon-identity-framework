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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.user.self.registration.model.dto.ActionDTO;
import org.wso2.carbon.identity.user.self.registration.model.dto.BlockDTO;
import org.wso2.carbon.identity.user.self.registration.model.dto.ElementDTO;
import org.wso2.carbon.identity.user.self.registration.model.dto.ExecutorDTO;
import org.wso2.carbon.identity.user.self.registration.model.dto.MetaDTO;
import org.wso2.carbon.identity.user.self.registration.model.dto.PageDTO;
import org.wso2.carbon.identity.user.self.registration.temp.ConfigDataHolder;
import org.wso2.carbon.identity.user.self.registration.util.Constants;

public class FlowToPageConvertor {

    public static Map<String, PageDTO> getPageList(String orgId) throws IOException {

        JsonNode rootNode = loadFlowJson(orgId);
        Map<String, ElementDTO> elementDTOMap = processElements(rootNode);
        Map<String, BlockDTO> blockDTOMap = processBlocks(rootNode);

        // Derive the pageDTOs from the nodes array.
        Map<String, PageDTO> pageDTOMap = new HashMap<>();
        JsonNode nodesArray = rootNode.get("nodes");
        if (nodesArray == null || !nodesArray.isArray()) {
            System.out.println("'nodes' array not found or is not an array.");
            return null;
        }
        for (JsonNode node : nodesArray) {
            PageDTO pageDTO = new PageDTO();
            String nodeId = node.get("id").asText();
            updateElementWithAction(node, nodeId, elementDTOMap);
            pageDTO.setId(nodeId);

            // Process elements array in the node
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

            pageDTOMap.put(nodeId, pageDTO);
        }
        return pageDTOMap;
    }

    private static JsonNode loadFlowJson(String orgId) throws IOException {

        if (Constants.NEW_FLOW.equalsIgnoreCase(orgId)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = ConfigDataHolder.getInstance().getOrchestrationConfig().get("carbon.super");
            if (jsonString == null) {
                throw new IllegalArgumentException("Flow not found. Make sure " +
                                                           "the flow is added through the /reg-orchestration/config API");
            }
            return objectMapper.readTree(jsonString);
        } else {
            throw new IllegalArgumentException("Flow not found.");
        }
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

    private static void updateElementWithAction(JsonNode node, String nodeId, Map<String, ElementDTO> elementDTOMap) {

        JsonNode actions = node.get("actions");
        if (actions == null || !actions.isArray()) {
            System.out.println("No actions found for node: " + nodeId);
            return;
        }
        for (JsonNode action : actions) {
            String actionId = action.get("id").asText();
            String actionType = action.get("action").get("type").asText();
            if ("NEXT".equals(actionType)) {
                ActionDTO actionDTO = new ActionDTO(actionType);
                if (elementDTOMap != null && elementDTOMap.containsKey(actionId)) {
                    elementDTOMap.get(actionId).setAction(actionDTO);
                }
            } else if ("EXECUTOR".equals(actionType)){
                String exName = null;
                JsonNode executorsNode = action.path("action").path("executors");

                // Retrieve the name of the first executor
                ActionDTO actionDTO = new ActionDTO(actionType);
                if (executorsNode.isArray() && !executorsNode.isEmpty()) {
                    JsonNode firstExecutor = executorsNode.get(0);
                    MetaDTO metaDTO = null;
                    if (firstExecutor.get("meta") != null) {
                        JsonNode exMeta = firstExecutor.get("meta");
                        if (exMeta != null && exMeta.has("idp")) {
                            metaDTO = new MetaDTO(exMeta.get("idp").asText());
                        }
                    }
                    exName = firstExecutor.path("name").asText();
                    ExecutorDTO executorDTO = new ExecutorDTO(exName, metaDTO);
                    actionDTO.addExecutor(executorDTO);
                }
                if (elementDTOMap != null && elementDTOMap.containsKey(actionId)) {
                    elementDTOMap.get(actionId).setAction(actionDTO);
                }
            }
        }
    }
}
