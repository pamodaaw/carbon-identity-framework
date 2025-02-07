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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeDTO {

    private String id;
    private String type;
    private String flowId;
    private List<String> nextNodes;
    private Map<String, String> pageIds = new HashMap<>();
    private Map<String, String> properties;

    public NodeDTO() {

        this.properties = new HashMap<>();
        this.nextNodes = new ArrayList<>();
    }

    public NodeDTO(String id, String type) {

        this.id = id;
        this.type = type;
        this.nextNodes = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public NodeDTO(String id, String type, String flowId) {

        this.id = id;
        this.type = type;
        this.flowId = flowId;
        this.properties = new HashMap<>();
    }

    public String getId() {

        return id;
    }

    public String getType() {

        return type;
    }

    public void setId(String id) {

        this.id = id;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getFlowId() {

        return flowId;
    }

    public void setFlowId(String flowId) {

        this.flowId = flowId;
    }

    public void addProperty(String key, String value) {

        this.properties.put(key, value);
    }

    public Map<String, String> getProperties() {

        return properties;
    }

    public List<String> getNextNodes() {

        return nextNodes;
    }

    public void addNextNode(String nextNodeId) {

        this.nextNodes.add(nextNodeId);
    }

    public void setNextNodes(List<String> nextNodes) {

        this.nextNodes = nextNodes;
    }

    public Map<String, String> getPageIds() {

        return pageIds;
    }

    public void setPageIds(Map<String, String> pageIds) {

        this.pageIds = pageIds;
    }

    public void addPageIds(String key, String value) {

        this.pageIds.put(key, value);
    }
}
