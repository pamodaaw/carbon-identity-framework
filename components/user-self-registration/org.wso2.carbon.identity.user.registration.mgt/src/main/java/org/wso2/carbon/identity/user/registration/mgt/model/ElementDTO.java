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

import java.util.HashMap;
import java.util.Map;

/**
 * DTO class for Element.
 */
public class ElementDTO {

    private String id;
    private String category;
    private String type;
    private String identifier;
    private final Map<String, Object> properties = new HashMap<>();
    private ActionDTO action;
    private ValidationDTO validation;

    public ElementDTO(String id, String type) {

        this.id = id;
        this.type = type;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public void addProperty(String key, Object value) {

        this.properties.put(key, value);
    }

    public ActionDTO getAction() {

        return action;
    }

    public void setAction(ActionDTO action) {

        this.action = action;
    }

    public String getIdentifier() {

        return identifier;
    }

    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

    public ValidationDTO getValidation() {

        return validation;
    }

    public void setValidation(ValidationDTO validation) {

        this.validation = validation;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }
}
