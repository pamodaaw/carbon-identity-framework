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

package org.wso2.carbon.identity.rule.metadata.service.impl;

import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.service.RuleMetadataService;

import java.util.List;

/**
 * Rule metadata service implementation.
 * This class is responsible for providing the rule metadata for the given flow type.
 */
public class RuleMetadataServiceImpl implements RuleMetadataService {

    private final RuleMetadataManager ruleMetadataManager;

    public RuleMetadataServiceImpl(RuleMetadataManager ruleMetadataManager) {

        this.ruleMetadataManager = ruleMetadataManager;
    }

    @Override
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        return ruleMetadataManager.getExpressionMetaForFlow(flowType, tenantDomain);
    }
}
