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

package org.wso2.carbon.identity.user.self.registration.mgt.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.NodeDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.RegistrationDTO;

public class RegistrationFlowDAO {

    public static String getRegistrationFlow(String tenantDomain) throws SQLException {

        String query = "SELECT ID FROM REG_FLOW WHERE TENANT_DOMAIN = ?";
        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String flowId = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantID);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                flowId = rs.getString("ID");
            }
        } catch (SQLException e) {
            throw new SQLException("Error while retrieving registration flow for tenant: " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeResultSet(rs);
        }
        return flowId;
    }

    public static String insertRegistrationFlow(String tenantDomain, String firstNode) throws SQLException {

        String query = "INSERT INTO REG_FLOW (TENANT_ID, FIRST_NODE) VALUES (?, ?)";
        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);

        PreparedStatement prepStmt = null;
        ResultSet results = null;
        String flowId = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantID);
            prepStmt.setString(2, firstNode);
            results = prepStmt.executeQuery();
            if (results.next()) {
                flowId = results.getString(1);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while storing the registration flow for tenant: " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeResultSet(results);
        }
        return flowId;
    }

    public static void insertNode(NodeDTO node, String tenantDomain) throws SQLException {

        String query = "INSERT INTO REG_NODE (ID, TENANT_ID, TYPE, FLOW_ID) VALUES (?, ?, ?, ?)";
        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);

        PreparedStatement prepStmt = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, node.getId());
            prepStmt.setInt(2, tenantID);
            prepStmt.setString(3, node.getType());
            prepStmt.setString(4, node.getFlowId());
            prepStmt.executeUpdate();

            for(Map.Entry<String, String> entry : node.getProperties().entrySet()) {
                addNodeProperties(connection, tenantID, node.getId(), entry.getKey(), entry.getValue());
            }
        } catch (SQLException e) {
            throw new SQLException("Error while storing the node definition of node: " + node.getId(), e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    private static void addNodeProperties(Connection connection, int tenantId, String nodeId, String key, String value)
            throws SQLException {

        String query = "INSERT INTO REG_NODE_PROPERTY (TENANT_ID, NODE_ID, KEY, VALUE) VALUES (?, ?, ?, ?)";
        PreparedStatement prepStmt = null;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, nodeId);
            prepStmt.setString(3, key);
            prepStmt.setString(4, value);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error while storing the properties of node: " + nodeId, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    public static void insertRegistrationPage(String tenantDomain, String pageId, String flowId, String content) throws SQLException {

        String query = "INSERT INTO REG_PAGE (ID, TENANT_ID, FLOW_ID, CONTENT) VALUES (?,?, ? ?)";
        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);

        PreparedStatement prepStmt = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, pageId);
            prepStmt.setInt(2, tenantID);
            prepStmt.setString(3, flowId);
            prepStmt.setString(4, content);
            prepStmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLException("Error while storing the page details for page id: " + pageId, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    public static RegistrationDTO retrieveRegistrationFlow(String flowID) throws SQLException {

        String query = "SELECT \n" +
                "    rf.ID AS Flow_ID,\n" +
                "    rf.TENANT_ID AS Flow_Tenant_ID,\n" +
                "    rf.FIRST_NODE AS First_Node,\n" +
                "    rn.ID AS Node_ID,\n" +
                "    rn.TYPE AS Node_Type,\n" +
                "    rn.FLOW_ID AS Node_Flow_ID,\n" +
                "    rnp.PROPERTY_NAME AS Node_Property_Name,\n" +
                "    rnp.PROPERTY_VALUE AS Node_Property_Value\n" +
                "FROM \n" +
                "    REG_FLOW rf\n" +
                "LEFT JOIN \n" +
                "    REG_NODE rn ON rf.ID = rn.FLOW_ID\n" +
                "LEFT JOIN \n" +
                "    REG_NODE_PROPERTIES rnp ON rn.ID = rnp.NODE_ID\n" +
                "WHERE \n" +
                "    rf.ID = ?\n" +
                "ORDER BY \n" +
                "    rf.ID, rn.ID, rnp.PROPERTY_NAME;\n";

        RegistrationDTO registrationDTO = new RegistrationDTO();
        Map<String, NodeDTO> nodeMap = new HashMap<>();
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            prepStmt = connection.prepareStatement(query);

            prepStmt.setString(1, flowID); // Replace with actual Flow ID
            resultSet = prepStmt.executeQuery();
        while (resultSet.next()) {
            // Extract flow-level details (only set once)
            if (registrationDTO.getFlowID() == null) {
                registrationDTO.setFlowID(resultSet.getString("Flow_ID"));
                registrationDTO.setTenantID(resultSet.getString("Flow_Tenant_ID"));
                registrationDTO.setFirstNode(resultSet.getString("First_Node"));
            }

            // Extract node details
            String nodeID = resultSet.getString("Node_ID");
            if (nodeID != null) {
                NodeDTO nodeDTO = nodeMap.getOrDefault(nodeID, new NodeDTO());
                nodeDTO.setId(nodeID);
                nodeDTO.setType(resultSet.getString("Node_Type"));

                // Extract node property details
                String propertyName = resultSet.getString("Node_Property_Name");
                String propertyValue = resultSet.getString("Node_Property_Value");
                if (propertyName != null) {
                    if (propertyName.equals("NEXT_NODE")) {
                        nodeDTO.addNextNode(propertyValue);
                    } else {
                        nodeDTO.getProperties().put(propertyName, propertyValue);
                    }
                }
                nodeMap.put(nodeID, nodeDTO);
            }
        }

        // Iterate the NodeDTO map and add each node to the RegistrationDTO.
        for (NodeDTO node : nodeMap.values()) {
            registrationDTO.addNode(node);
        }
        } catch (SQLException e) {
            throw new SQLException("Error while retrieving registration flow with id: " + flowID, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeResultSet(resultSet);
        }
        return registrationDTO;
    }
}
