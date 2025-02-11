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

package org.wso2.carbon.identity.user.self.registration.mgt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.BlockDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.ElementDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.NodeDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.PageDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.RegistrationDTO;

/**
 * The DAO class for the registration flow.
 */
public class RegistrationFlowDAO {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowDAO.class);

    private static final RegistrationFlowDAO instance = new RegistrationFlowDAO();

    private RegistrationFlowDAO() {

    }

    public static RegistrationFlowDAO getInstance() {

        return instance;
    }

    /**
     * Add the registration object to the database.
     *
     * @param regDTO   The registration object.
     * @param tenantId The tenant ID.
     */
    public void addRegistrationObject(RegistrationDTO regDTO, String tenantId) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        byte[] jsonBytes = regDTO.getFlowJson().getBytes();
        ByteArrayInputStream flowAsBlob = new ByteArrayInputStream(jsonBytes);

        try {
            jdbcTemplate.withTransaction(template -> {
                // Insert into REG_FLOW
                template.executeInsert(
                        "INSERT INTO REG_FLOW (flow_id, tenant_id, flow_name, flow_json) VALUES (?, ?, ?, ?)",
                        preparedStatement -> {
                            preparedStatement.setString(1, regDTO.getFlowID());
                            preparedStatement.setString(2, tenantId);
                            preparedStatement.setString(3, "default");
                            preparedStatement.setBlob(4, flowAsBlob);
                        }, regDTO, false);

                // Insert into REG_PAGE

                for (Map.Entry<String, PageDTO> entry : regDTO.getPageDTOs().entrySet()) {
                    PageDTO page = entry.getValue();
                    ByteArrayInputStream elementsBlob =
                            new ByteArrayInputStream(
                                    new ObjectMapper().writeValueAsString(page.getElements()).getBytes());
                    ByteArrayInputStream blocksBlob =
                            new ByteArrayInputStream(
                                    new ObjectMapper().writeValueAsString(page.getBlocks()).getBytes());
                    template.executeInsert("INSERT INTO REG_PAGE (page_id, flow_id, elements, blocks) VALUES (?, ?, " +
                                                   "?, ?)",
                                           preparedStatement -> {
                                               preparedStatement.setString(1, page.getId());
                                               preparedStatement.setString(2, regDTO.getFlowID());
                                               preparedStatement.setBlob(3, elementsBlob);
                                               preparedStatement.setBlob(4, blocksBlob);
                                           }, page, false);
                }

                // Insert into REG_NODE
                for (Map.Entry<String, NodeDTO> entry : regDTO.getNodes().entrySet()) {
                    NodeDTO node = entry.getValue();
                    template.executeInsert("INSERT INTO REG_NODE (node_id, flow_id, node_type) VALUES (?, ?, ?)",
                                           preparedStatement -> {
                                               preparedStatement.setString(1, node.getId());
                                               preparedStatement.setString(2, regDTO.getFlowID());
                                               preparedStatement.setString(3, node.getType());
                                           }, node, false);

                    // Insert into REG_NODE_PROPERTIES
                    for (Map.Entry<String, String> property : node.getProperties().entrySet()) {
                        template.executeInsert(
                                "INSERT INTO REG_NODE_PROPERTIES (node_id, property_key, property_value) VALUES (?, " +
                                        "?, ?)",
                                preparedStatement -> {
                                    preparedStatement.setString(1, node.getId());
                                    preparedStatement.setString(2, property.getKey());
                                    preparedStatement.setString(3, property.getValue());
                                }, property, false);
                    }
                }
                return null;
            });
        } catch (TransactionException e) {
            LOG.error("Failed to store the flow object", e);
        }
    }

    /**
     * Retrieve the registration object by tenant ID.
     *
     * @param tenantId The tenant ID.
     * @return The registration object.
     */
    public RegistrationDTO getRegistrationObjectByTenantId(String tenantId) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            // Retrieve REG_FLOW
            RegistrationDTO registrationObject =
                    jdbcTemplate.fetchSingleRecord("SELECT * FROM REG_FLOW WHERE tenant_id = ?",
                                                   (resultSet, rowNumber) -> {
                                                       RegistrationDTO obj = new RegistrationDTO();
                                                       obj.setFlowID(resultSet.getString("flow_id"));
                                                       obj.setTenantID(resultSet.getString("tenant_id"));
                                                       obj.setFlowJson(new String(resultSet.getBytes("flow_json")));
                                                       return obj;
                                                   }, preparedStatement -> preparedStatement.setString(1, tenantId));

            if (registrationObject == null) {
                return null;
            }

            String flowId = registrationObject.getFlowID();

            // Retrieve REG_PAGE
            List<PageDTO> pages = jdbcTemplate.executeQuery("SELECT * FROM REG_PAGE WHERE flow_id = ?",
                                                            (resultSet, rowNumber) -> {
                                                                String elementsArray =
                                                                        new String(resultSet.getBytes("elements"));
                                                                String blocksArray =
                                                                        new String(resultSet.getBytes("blocks"));
                                                                List<ElementDTO> elementDTOS;
                                                                List<BlockDTO> blockDTOS;
                                                                try {
                                                                    elementDTOS =
                                                                            new ObjectMapper().readValue(elementsArray,
                                                                                                         new TypeReference<List<ElementDTO>>() {
                                                                                                         });
                                                                    blockDTOS =
                                                                            new ObjectMapper().readValue(blocksArray,
                                                                                                         new TypeReference<List<BlockDTO>>() {
                                                                                                         });
                                                                } catch (JsonProcessingException e) {
                                                                    throw new RuntimeException(e);
                                                                }

                                                                PageDTO page = new PageDTO();
                                                                page.setId(resultSet.getString("page_id"));
                                                                page.setElements(elementDTOS);
                                                                page.setBlocks(blockDTOS);
                                                                return page;
                                                            }, preparedStatement -> preparedStatement.setString(1,
                                                                                                                flowId));

            // Write a logic to loop pages list and setPageDTOs.
            for (PageDTO page : pages) {
                registrationObject.addPageDTO(page);
            }

            // Retrieve REG_NODE
            List<NodeDTO> nodes = jdbcTemplate.executeQuery("SELECT * FROM REG_NODE WHERE flow_id = ?",
                                                            (resultSet, rowNumber) -> {
                                                                NodeDTO node = new NodeDTO();
                                                                node.setId(resultSet.getString("node_id"));
                                                                node.setFlowId(resultSet.getString("flow_id"));
                                                                node.setType(resultSet.getString("node_type"));
                                                                node.setFirstNode(
                                                                        resultSet.getBoolean("is_first_node"));
                                                                return node;
                                                            }, preparedStatement -> preparedStatement.setString(1,
                                                                                                                flowId));

            for (NodeDTO node : nodes) {
                // Retrieve REG_NODE_PROPERTIES
                Map<String, String> properties =
                        jdbcTemplate.executeQuery("SELECT * FROM REG_NODE_PROPERTIES WHERE node_id = ?",
                                                  (resultSet, rowNumber) -> {
                                                      return new AbstractMap.SimpleEntry<>(
                                                              resultSet.getString("property_key"),
                                                              resultSet.getString("property_value"));
                                                  },
                                                  preparedStatement -> preparedStatement.setString(1, node.getId()))
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                node.setProperties(properties);
                registrationObject.addNode(node);
            }
            return registrationObject;
        } catch (DataAccessException e) {
            LOG.error("Failed to retrieve the flow object", e);
            return null;
        }
    }
}
