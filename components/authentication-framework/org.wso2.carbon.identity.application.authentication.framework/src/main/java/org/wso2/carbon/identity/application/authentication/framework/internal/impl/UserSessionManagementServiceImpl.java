/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

/**
 * This a service class used to manage user sessions.
 */
public class UserSessionManagementServiceImpl implements UserSessionManagementService {

    private static final Log log = LogFactory.getLog(UserSessionManagementServiceImpl.class);
    private SessionManagementService sessionManagementService = new SessionManagementService();
    private static final String SESSION_DELETE_PERMISSION = "/permission/admin/manage/identity/authentication" +
            "/session/delete";

    @Override
    public void terminateSessionsOfUser(String username, String userStoreDomain, String tenantDomain) throws
            UserSessionException {

        validate(username, userStoreDomain, tenantDomain);
        List<String> sessionListOfUser = getSessionsOfUser(username, userStoreDomain, tenantDomain);

        if (!sessionListOfUser.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating all the active sessions of user: " + username + " of userstore domain: " +
                        userStoreDomain + " in tenant: " + tenantDomain);
            }
            terminateSessionsOfUser(sessionListOfUser);
        }
    }

    private void validate(String username, String userStoreDomain, String tenantDomain) throws UserSessionException {

        if (StringUtils.isBlank(username) || StringUtils.isBlank(userStoreDomain) || StringUtils.isBlank(tenantDomain)) {
            throw new UserSessionException("Username, userstore domain or tenant domain cannot be empty");
        }

        int tenantId = getTenantId(tenantDomain);
        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            throw new UserSessionException("Invalid tenant domain: " + tenantDomain + " provided.");
        }
    }

    private List<String> getSessionsOfUser(String username, String userStoreDomain, String tenantDomain) throws
            UserSessionException {

        String userId = UserSessionStore.getInstance().getUserId(username, getTenantId(tenantDomain),
                userStoreDomain);
        return UserSessionStore.getInstance().getSessionId(userId);
    }

    private void terminateSessionsOfUser(List<String> sessionList) {

        for (String session : sessionList) {
            sessionManagementService.removeSession(session);
        }
    }

    private int getTenantId(String tenantDomain) throws UserSessionException {

        try {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new UserSessionException("Failed to retrieve tenant id for tenant domain: " + tenantDomain);
        }
    }

    @Override
    public List<UserSession> getSessionsByUserId(String userId) throws SessionManagementException {

        return getActiveSessionList(getSessionIdListByUserId(userId));
    }

    @Override
    public boolean terminateSessionsByUserId(String userId) throws SessionManagementException {

        List<String> sessionIdList = getSessionIdListByUserId(userId);
        terminateSessionsOfUser(sessionIdList);
        if (!sessionIdList.isEmpty()) {
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
        }
        return true;
    }

    @Override
    public boolean terminateSessionBySessionId(String userId, String sessionId) throws SessionManagementException {

        if (isUserAuthorized(userId, sessionId)) {
            sessionManagementService.removeSession(sessionId);
            List<String> sessionIdList = new ArrayList<>();
            sessionIdList.add(sessionId);
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
            return true;
        } else {
            throw new SessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_FORBIDDEN_ACTION,
                    "Session terminate action is forbidden to the user.");
        }
    }

    @Override
    public List<UserSession> getSessionsByUser(User user, int idpId) throws SessionManagementException {

        return getActiveSessionList(getSessionIdListByUser(user, idpId));
    }

    @Override
    public boolean terminateSessionsByUser(User user, int idpId) throws SessionManagementException {

        List<String> sessionIdList = getSessionIdListByUser(user, idpId);
        terminateSessionsOfUser(sessionIdList);
        if (!sessionIdList.isEmpty()) {
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
        }
        return true;
    }

    @Override
    public boolean terminateSessionBySessionId(User user, int idpId, String sessionId) throws
            SessionManagementException {

        if (isUserAuthorized(user, idpId, sessionId)) {
            sessionManagementService.removeSession(sessionId);
            List<String> sessionIdList = new ArrayList<>();
            sessionIdList.add(sessionId);
            UserSessionStore.getInstance().removeTerminatedSessionRecords(sessionIdList);
            return true;
        } else {
            throw new SessionManagementClientException(SessionMgtConstants.ErrorMessages.ERROR_CODE_FORBIDDEN_ACTION,
                    "Session terminate action is forbidden to the user.");
        }
    }

    /**
     * Returns the session id list for a given user id.
     *
     * @param userId user id for which the sessions should be retrieved.
     * @return the list of session ids
     * @throws SessionManagementServerException if session Ids can not be retrieved from the database.
     */
    private List<String> getSessionIdListByUserId(String userId) throws SessionManagementServerException {

        try {
            return UserSessionStore.getInstance().getSessionId(userId);
        } catch (UserSessionException e) {
            throw new SessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_GET_SESSIONS, "Server encountered an error while retrieving session list of" +
                    " user ID " + userId, e);
        }
    }

    /**
     * Returns the session id list for a given user.
     *
     * @param user  user object
     * @param idpId id of the user's idp
     * @throws SessionManagementServerException if session Ids can not be retrieved from the database.
     */
    private List<String> getSessionIdListByUser(User user, int idpId) throws SessionManagementServerException {

        try {
            return UserSessionStore.getInstance().getSessionId(user, idpId);
        } catch (UserSessionException e) {
            throw new SessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_GET_SESSIONS, "Server encountered an error while retrieving session list of" +
                    " user, " + user.getUserName(), e);
        }
    }

    /**
     * Returns the active sessions from given list of session IDs
     *
     * @param sessionIdList List of sessionIds
     * @return UserSession[] Usersessions
     * @throws SessionManagementServerException if an error occurs when retrieving the UserSessions.
     */
    private List<UserSession> getActiveSessionList(List<String> sessionIdList) throws SessionManagementServerException {

        List<UserSession> sessionsList = new ArrayList<>();
        for (String sessionId : sessionIdList) {
            if (sessionId != null) {
                SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId);
                if (sessionContext != null) {
                    UserSessionDAO userSessionDTO = new UserSessionDAOImpl();
                    UserSession userSession = userSessionDTO.getSession(sessionId);
                    if (userSession != null) {
                        sessionsList.add(userSession);
                    }
                }
            }
        }
        return sessionsList;
    }

    private boolean isUserAuthorized(String userId, String sessionId) throws SessionManagementServerException {

        boolean isUserAuthorized;
        try {
            isUserAuthorized = UserSessionStore.getInstance().isExistingMapping(userId, sessionId);
            if (!isUserAuthorized) {
                isUserAuthorized = isActionForbidden();
            }
        } catch (UserSessionException | UserStoreException e) {
            throw new SessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_AUTHORIZE_USER, "Server encountered an error while authorizing the user.", e);
        }
        return isUserAuthorized;
    }

    private boolean isUserAuthorized(User user, int idpId, String sessionId) throws SessionManagementServerException {

        boolean isUserAuthorized;

        try {
            isUserAuthorized = UserSessionStore.getInstance().isExistingMapping(user, idpId, sessionId);
            if (!isUserAuthorized) {
                isUserAuthorized = isActionForbidden();
            }
        } catch (UserSessionException | UserStoreException e) {
            throw new SessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_AUTHORIZE_USER, "Server encountered an error while authorizing the user.", e);
        }
        return isUserAuthorized;
    }

    private boolean isActionForbidden() throws UserStoreException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String loggedInName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        AuthorizationManager authzManager = FrameworkServiceDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(tenantId).getAuthorizationManager();
        return authzManager.isUserAuthorized(loggedInName, SESSION_DELETE_PERMISSION, CarbonConstants
                .UI_PERMISSION_ACTION);
    }
}
