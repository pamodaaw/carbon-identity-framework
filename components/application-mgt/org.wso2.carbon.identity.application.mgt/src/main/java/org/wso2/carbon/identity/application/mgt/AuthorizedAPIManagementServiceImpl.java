/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.dao.AuthorizedAPIDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.AuthorizedAPIDAOImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.UNEXPECTED_SERVER_ERROR;

/**
 * Authorized API management service implementation.
 */
public class AuthorizedAPIManagementServiceImpl implements AuthorizedAPIManagementService {

    private final AuthorizedAPIDAO authorizedAPIDAO = new AuthorizedAPIDAOImpl();

    @Override
    public void addAuthorizedAPI(String applicationId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Check if the application is a main application. If not, throw a client error.
        ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        String mainAppId = applicationManagementService.getMainAppId(applicationId);
        if (StringUtils.isNotBlank(mainAppId)) {
            throw buildClientException(INVALID_REQUEST, "Cannot add authorized APIs to a shared application.");
        }
        authorizedAPIDAO.addAuthorizedAPI(applicationId, authorizedAPI.getAPIId(),
                authorizedAPI.getPolicyId(), authorizedAPI.getScopes(), IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        authorizedAPIDAO.deleteAuthorizedAPI(appId, apiId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            // Check if the application is a main application else get the main application id and main tenant id.
            ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
            String mainAppId = applicationManagementService.getMainAppId(applicationId);
            if (StringUtils.isNotBlank(mainAppId)) {
                applicationId = mainAppId;
                int tenantId = applicationManagementService.getTenantIdByApp(mainAppId);
                tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            }

            List<AuthorizedAPI> authorizedAPIs = authorizedAPIDAO.getAuthorizedAPIs(applicationId,
                    IdentityTenantUtil.getTenantId(tenantDomain));
            for (AuthorizedAPI authorizedAPI : authorizedAPIs) {
                // Get API resource data from OSGi service.
                APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIResourceById(authorizedAPI.getAPIId(), tenantDomain);
                authorizedAPI.setAPIIdentifier(apiResource.getIdentifier());
                authorizedAPI.setAPIName(apiResource.getName());
                // Get Scope data from OSGi service.
                List<Scope> scopeList = new ArrayList<>();
                if (authorizedAPI.getScopes() != null) {
                    for (Scope scope : authorizedAPI.getScopes()) {
                        Scope scopeWithMetadata = ApplicationManagementServiceComponentHolder.getInstance()
                                .getAPIResourceManager().getScopeByName(scope.getName(), tenantDomain);
                        scopeList.add(scopeWithMetadata);
                    }
                }
                authorizedAPI.setScopes(scopeList);
            }
            return authorizedAPIs;
        } catch (APIResourceMgtException e) {
            throw buildServerException("Error while retrieving authorized APIs.", e);
        }
    }

    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                   List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException {

        authorizedAPIDAO.patchAuthorizedAPI(appId, apiId, addedScopes, removedScopes,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<AuthorizedScopes> getAuthorizedScopes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Check if the application is a main application else get the main application id and main tenant id.
        ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        String mainAppId = applicationManagementService.getMainAppId(appId);
        if (mainAppId != null) {
            appId = mainAppId;
            int tenantId = applicationManagementService.getTenantIdByApp(mainAppId);
            tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        }
        return authorizedAPIDAO.getAuthorizedScopes(appId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public AuthorizedAPI getAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            // Check if the application is a main application else get the main application id and main tenant id.
            ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
            String mainAppId = applicationManagementService.getMainAppId(appId);
            if (mainAppId != null) {
                apiId = mainAppId;
                int tenantId = applicationManagementService.getTenantIdByApp(mainAppId);
                tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            }

            AuthorizedAPI authorizedAPI = authorizedAPIDAO.getAuthorizedAPI(appId, apiId,
                    IdentityTenantUtil.getTenantId(tenantDomain));
            if (authorizedAPI == null) {
                return null;
            }
            APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                    .getAPIResourceManager().getAPIResourceById(authorizedAPI.getAPIId(), tenantDomain);
            authorizedAPI.setAPIIdentifier(apiResource.getIdentifier());
            authorizedAPI.setAPIName(apiResource.getName());
            // Get Scope data from OSGi service.
            List<Scope> scopeList = new ArrayList<>();
            for (Scope scope : authorizedAPI.getScopes()) {
                Scope scopeWithMetadata = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getScopeByName(scope.getName(), tenantDomain);
                scopeList.add(scopeWithMetadata);
            }
            authorizedAPI.setScopes(scopeList);
            return authorizedAPI;
        } catch (APIResourceMgtException e) {
            throw buildServerException("Error while retrieving authorized API.", e);
        }
    }

    private IdentityApplicationManagementClientException buildClientException(
            IdentityApplicationConstants.Error errorMessage, String message) {

        return new IdentityApplicationManagementClientException(errorMessage.getCode(), message);
    }

    private IdentityApplicationManagementServerException buildServerException(String message, Throwable ex) {

        return new IdentityApplicationManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), message, ex);
    }
}
