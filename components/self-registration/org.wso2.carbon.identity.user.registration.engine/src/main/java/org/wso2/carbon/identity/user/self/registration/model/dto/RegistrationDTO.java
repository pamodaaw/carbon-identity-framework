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

package org.wso2.carbon.identity.user.self.registration.model.dto;

import java.util.HashMap;
import java.util.Map;

public class RegistrationDTO {

    private String flowID;
    private String tenantID;
    private String firstNode;
    private Map<String, NodeDTO> nodeDTOList;
    private Map<String, PageDTO> pageDTOs;

    public RegistrationDTO() {

        this.nodeDTOList = new HashMap<>();
        this.pageDTOs = new HashMap<>();
    }

    public RegistrationDTO(String flowID, String tenantID, String firstNode) {

        this.flowID = flowID;
        this.tenantID = tenantID;
        this.firstNode = firstNode;
        this.nodeDTOList = new HashMap<>();
        this.pageDTOs = new HashMap<>(
        );
    }

    public String getFlowID() {

        return flowID;
    }

    public void setFlowID(String flowID) {

        this.flowID = flowID;
    }

    public String getTenantID() {

        return tenantID;
    }

    public void setTenantID(String tenantID) {

        this.tenantID = tenantID;
    }

    public String getFirstNode() {

        return firstNode;
    }

    public void setFirstNode(String firstNode) {

        this.firstNode = firstNode;
    }

    public Map<String, NodeDTO> getNodeDTOList() {

        return nodeDTOList;
    }

    public void addNode(NodeDTO node) {
        if (node != null && node.getId() != null) {
            this.nodeDTOList.put(node.getId(), node);
        } else {
            System.err.println("Warning: Attempted to add a null or invalid node.");
        }
    }

    public NodeDTO getNode(String id) {

        return this.nodeDTOList.get(id);
    }

    // Retrieve all nodes as a map
    public Map<String, NodeDTO> getNodes() {

        return  this.nodeDTOList;
    }

    public void setNodeDTOList(Map<String, NodeDTO> nodeDTOList) {

        this.nodeDTOList = nodeDTOList;
    }

    public Map<String, PageDTO> getPageDTOs() {

        return pageDTOs;
    }

    public void setPageDTOs( Map<String, PageDTO> pageDTOs) {

        this.pageDTOs = pageDTOs;
    }
}
