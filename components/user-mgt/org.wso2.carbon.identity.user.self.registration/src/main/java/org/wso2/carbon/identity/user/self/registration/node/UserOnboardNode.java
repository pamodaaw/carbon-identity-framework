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

package org.wso2.carbon.identity.user.self.registration.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.self.registration.model.NodeResponse;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationContext;
import org.wso2.carbon.identity.user.self.registration.model.RegistrationRequestedUser;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.ErrorMessages.ERROR_LOADING_USERSTORE_MANAGER;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.ErrorMessages.ERROR_ONBOARDING_USER;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.PASSWORD;
import static org.wso2.carbon.identity.user.self.registration.util.Constants.STATUS_USER_CREATED;

/**
 * Implementation of a node specific to onboarding the user.
 */
@Deprecated
public class UserOnboardNode extends AbstractNode {

    private static final Log LOG = LogFactory.getLog(UserOnboardNode.class);

    public UserOnboardNode() {

        super();
    }

    public UserOnboardNode(String id) {

        super(id);
    }

    @Override
    public NodeResponse execute(RegistrationContext context) throws RegistrationFrameworkException {

        return null;
    }

}
