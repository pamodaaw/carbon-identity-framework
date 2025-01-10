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

package org.wso2.carbon.identity.rule.metadata.config;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.model.Field;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.model.Link;
import org.wso2.carbon.identity.rule.metadata.model.Operator;
import org.wso2.carbon.identity.rule.metadata.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.model.Value;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FlowConfigTest {

    @Mock
    private FieldDefinitionConfig fieldDefinitionConfig;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;
    private Map<String, FieldDefinition> fieldDefinitions;

    @BeforeClass
    public void setUpClass() throws RuleMetadataConfigException {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "configs/valid-operators.json"))
                .getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(filePath));
        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);

        MockitoAnnotations.openMocks(this);
        fieldDefinitions = getMockedFieldDefinitions();
        when(fieldDefinitionConfig.getFieldDefinitionMap()).thenReturn(fieldDefinitions);
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testLoadFieldDefinitionsForFlowFromValidConfig() throws Exception {

        String flowsFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-flows.json")).getFile();
        FlowConfig flowConfig = FlowConfig.load(new File(flowsFilePath), fieldDefinitionConfig);

        assertNotNull(flowConfig);

        List<FieldDefinition> result = flowConfig.getFieldDefinitionsForFlow(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        assertEquals(result,
                Arrays.asList(fieldDefinitions.get("application"), fieldDefinitions.get("grantType")));
    }

    @DataProvider(name = "invalidConfigFiles")
    public Object[][] invalidConfigFiles() {

        return new Object[][]{
                {"configs/invalid-flows-unregistered-flow.json"},
                {"configs/invalid-flows-unregistered-field.json"},
                {"unavailable-file.json"}
        };
    }

    @Test(dataProvider = "invalidConfigFiles", expectedExceptions = RuleMetadataConfigException.class,
            expectedExceptionsMessageRegExp = "Error while loading flows from file: .*")
    public void testLoadFieldDefinitionsForFlowFromInvalidConfig(String filePath) throws RuleMetadataConfigException {

        FlowConfig.load(filePath.equals("unavailable-file.json") ? new File(filePath) :
                new File(getClass().getClassLoader().getResource(filePath).getFile()), fieldDefinitionConfig);
    }

    private Map<String, FieldDefinition> getMockedFieldDefinitions() {

        Map<String, FieldDefinition> fieldDefinitionMap = new HashMap<>();

        Field applicationField = new Field("application", "application");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));

        List<Link> links = Arrays.asList(new Link("/applications?offset=0&limit=10", "GET", "values"),
                new Link("/applications?filter=name+eq+*&limit=10", "GET", "filter"));
        Value applicationValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("id")
                .valueDisplayAttribute("name").valueType(Value.ValueType.STRING).links(links).build();
        fieldDefinitionMap.put("application", new FieldDefinition(applicationField, operators, applicationValue));

        Field grantTypeField = new Field("grantType", "grantType");
        List<OptionsValue> optionsValues = Arrays.asList(new OptionsValue("authorization_code", "authorization code"),
                new OptionsValue("password", "password"), new OptionsValue("refresh_token", "refresh token"),
                new OptionsValue("client_credentials", "client credentials"),
                new OptionsValue("urn:ietf:params:oauth:grant-type:token-exchange", "token exchange"));
        Value grantTypeValue = new OptionsInputValue(Value.ValueType.STRING, optionsValues);
        fieldDefinitionMap.put("grantType", new FieldDefinition(grantTypeField, operators, grantTypeValue));

        return fieldDefinitionMap;
    }
}
