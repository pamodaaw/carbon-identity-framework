/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DefaultProvisioningHandlerTest {

    private DefaultProvisioningHandler provisioningHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        provisioningHandler = new DefaultProvisioningHandler();
        CommonTestUtils.initPrivilegedCarbonContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testGetInstance() throws Exception {
        CommonTestUtils.testSingleton(
                DefaultProvisioningHandler.getInstance(),
                DefaultProvisioningHandler.getInstance()
        );
    }

    @Test
    public void testHandle() throws Exception {
    }

    @DataProvider(name = "associateUserEmptyInputProvider")
    public Object[][] getAssociatedUserEmptyInputs() {
        return new Object[][]{
                {"", null},
                {"", ""},
                {null, ""},
                {null, null},
        };
    }

    @Test(dataProvider = "associateUserEmptyInputProvider", expectedExceptions = FrameworkException.class)
    public void testAssociateUserEmptyInputs(String subject,
                                             String idp) throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            frameworkUtils.when(() -> FrameworkUtils.startTenantFlow("tenantDomain"))
                    .thenAnswer(invocation -> null);
            provisioningHandler.associateUser("dummy_user_name", "DUMMY_DOMAIN",
                    "dummy.com", subject, idp);
        }
    }

    @Test
    public void testGeneratePassword() throws Exception {
        char[] randomPassword = provisioningHandler.generatePassword();
        assertNotNull(randomPassword);
        assertEquals(randomPassword.length, 12);
    }

    @Test
    public void testResolvePassword() throws Exception {

        Map<String, String> userClaims = new HashMap<>();
        userClaims.put(FrameworkConstants.PASSWORD, "dummy_password");
        char[] resolvedPassword = provisioningHandler.resolvePassword(userClaims);
        assertEquals(resolvedPassword, "dummy_password".toCharArray());
    }
}
