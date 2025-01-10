/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.rule.metadata.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

/**
 * Represents an operator in a rule.
 */
public class Operator {

    private final String name;
    private final String displayName;

    @JsonCreator
    public Operator(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName) {

        validate(name, displayName);
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {

        return name;
    }

    public String getDisplayName() {

        return displayName;
    }

    private void validate(String name, String displayName) {

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Operator 'name' cannot be null or empty.");
        }

        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("Operator 'displayName' cannot be null or empty.");
        }
    }
}
