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

package org.wso2.carbon.identity.user.self.registration.mgt.dto;

import java.util.ArrayList;
import java.util.List;

public class BlockDTO {

    private String id;
    private final List<String> elements = new ArrayList<>();

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public List<String> getElementIds() {

        return elements;
    }

    public void addElementId(String elementId) {

        this.elements.add(elementId);
    }
}
