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

package org.wso2.carbon.identity.user.registration.mgt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationFlowDTO {

    private List<StepDTO> steps = new ArrayList<>();
    private Map<String, ElementDTO> elementDTOMap = new HashMap<>();
    private Map<String, BlockDTO> blockDTOMap = new HashMap<>();
    private String flowJson;

    public Map<String, ElementDTO> getElementDTOMap() {

        return elementDTOMap;
    }

    public void setElementDTOMap(
            Map<String, ElementDTO> elementDTOMap) {

        this.elementDTOMap = elementDTOMap;
    }

    public Map<String, BlockDTO> getBlockDTOMap() {

        return blockDTOMap;
    }

    public void setBlockDTOMap(
            Map<String, BlockDTO> blockDTOMap) {

        this.blockDTOMap = blockDTOMap;
    }

    public String getFlowJson() {

        return flowJson;
    }

    public void setFlowJson(String flowJson) {

        this.flowJson = flowJson;
    }

    public List<StepDTO> getSteps() {

        return steps;
    }

    public void setSteps(List<StepDTO> steps) {

        this.steps = steps;
    }
}
