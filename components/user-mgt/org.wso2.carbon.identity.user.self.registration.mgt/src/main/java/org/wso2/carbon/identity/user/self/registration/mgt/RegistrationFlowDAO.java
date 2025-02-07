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

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.RegistrationDTO;

/**
 * The DAO class for the registration flow.
 */
public class RegistrationFlowDAO {

    public void addRegistrationObject(RegistrationDTO regDTO, String tenantId) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                // Insert into REG_FLOW
                template.executeInsert("INSERT INTO REG_FLOW (flow_id, tenant_id, flow_name, flow_json, first_node_id) VALUES (?, ?, ?, ?, ?)",
                                       preparedStatement -> {
                                           preparedStatement.setInt(1, regDTO.getFlowID());
                                           preparedStatement.setInt(2, registrationObject.getTenantId());
                                           preparedStatement.setString(3, registrationObject.getFlowName());
                                           preparedStatement.setBlob(4, new ByteArrayInputStream(registrationObject.getFlowJson()));
                                           preparedStatement.setInt(5, registrationObject.getFirstNodeId());
                                       }, registrationObject, false);

                // Insert into REG_PAGE
                for (Page page : registrationObject.getPages()) {
                    template.executeInsert("INSERT INTO REG_PAGE (page_id, flow_id, page_name, page_content) VALUES (?, ?, ?, ?)",
                                           preparedStatement -> {
                                               preparedStatement.setInt(1, page.getPageId());
                                               preparedStatement.setInt(2, registrationObject.getFlowId());
                                               preparedStatement.setString(3, page.getPageName());
                                               preparedStatement.setBlob(4, new ByteArrayInputStream(page.getPageContent()));
                                           }, page, false);
                }

                // Insert into REG_NODE
                for (Node node : registrationObject.getNodes()) {
                    template.executeInsert("INSERT INTO REG_NODE (node_id, flow_id, node_type) VALUES (?, ?, ?)",
                                           preparedStatement -> {
                                               preparedStatement.setInt(1, node.getNodeId());
                                               preparedStatement.setInt(2, registrationObject.getFlowId());
                                               preparedStatement.setString(3, node.getNodeType());
                                           }, node, false);

                    // Insert into REG_NODE_PROPERTIES
                    for (NodeProperty property : node.getProperties()) {
                        template.executeInsert("INSERT INTO REG_NODE_PROPERTIES (node_id, property_key, property_value) VALUES (?, ?, ?)",
                                               preparedStatement -> {
                                                   preparedStatement.setInt(1, node.getNodeId());
                                                   preparedStatement.setString(2, property.getPropertyKey());
                                                   preparedStatement.setString(3, property.getPropertyValue());
                                               }, property, false);
                    }
                }
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ADD_RESOURCE, registrationObject.getFlowName(), e);
        }
    }

    public RegistrationObject getRegistrationObject(int flowId) throws ConfigurationManagementException {
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            // Retrieve REG_FLOW
            RegistrationObject registrationObject = jdbcTemplate.fetchSingleRecord("SELECT * FROM REG_FLOW WHERE flow_id = ?",
                                                                                   (resultSet, rowNumber) -> {
                                                                                       RegistrationObject obj = new RegistrationObject();
                                                                                       obj.setFlowId(resultSet.getInt("flow_id"));
                                                                                       obj.setTenantId(resultSet.getInt("tenant_id"));
                                                                                       obj.setFlowName(resultSet.getString("flow_name"));
                                                                                       obj.setFlowJson(resultSet.getBytes("flow_json"));
                                                                                       obj.setFirstNodeId(resultSet.getInt("first_node_id"));
                                                                                       return obj;
                                                                                   }, preparedStatement -> preparedStatement.setInt(1, flowId));

            if (registrationObject == null) {
                return null;
            }

            // Retrieve REG_PAGE
            List<Page> pages = jdbcTemplate.executeQuery("SELECT * FROM REG_PAGE WHERE flow_id = ?",
                                                         (resultSet, rowNumber) -> {
                                                             Page page = new Page();
                                                             page.setPageId(resultSet.getInt("page_id"));
                                                             page.setFlowId(resultSet.getInt("flow_id"));
                                                             page.setPageName(resultSet.getString("page_name"));
                                                             page.setPageContent(resultSet.getBytes("page_content"));
                                                             return page;
                                                         }, preparedStatement -> preparedStatement.setInt(1, flowId));
            registrationObject.setPages(pages);

            // Retrieve REG_NODE
            List<Node> nodes = jdbcTemplate.executeQuery("SELECT * FROM REG_NODE WHERE flow_id = ?",
                                                         (resultSet, rowNumber) -> {
                                                             Node node = new Node();
                                                             node.setNodeId(resultSet.getInt("node_id"));
                                                             node.setFlowId(resultSet.getInt("flow_id"));
                                                             node.setNodeType(resultSet.getString("node_type"));
                                                             return node;
                                                         }, preparedStatement -> preparedStatement.setInt(1, flowId));

            for (Node node : nodes) {
                // Retrieve REG_NODE_PROPERTIES
                List<NodeProperty> properties = jdbcTemplate.executeQuery("SELECT * FROM REG_NODE_PROPERTIES WHERE node_id = ?",
                                                                          (resultSet, rowNumber) -> {
                                                                              NodeProperty property = new NodeProperty();
                                                                              property.setPropertyId(resultSet.getInt("property_id"));
                                                                              property.setNodeId(resultSet.getInt("node_id"));
                                                                              property.setPropertyKey(resultSet.getString("property_key"));
                                                                              property.setPropertyValue(resultSet.getString("property_value"));
                                                                              return property;
                                                                          }, preparedStatement -> preparedStatement.setInt(1, node.getNodeId()));
                node.setProperties(properties);
            }
            registrationObject.setNodes(nodes);

            return registrationObject;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_RESOURCE, "flowId = " + flowId, e);
        }
    }
}
