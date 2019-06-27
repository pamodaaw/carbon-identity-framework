/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;

import java.util.List;

/**
 * Default implementation of {@link UserSessionDAO}. This handles {@link UserSession} related DB operations.
 */
public class UserSessionDAOImpl implements UserSessionDAO {

    private static final Log log = LogFactory.getLog(UserSessionDAOImpl.class);

    public UserSessionDAOImpl() {
    }

    public UserSession getSession(String sessionId) throws SessionManagementServerException {
        List<Application> applicationList = null;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            applicationList = jdbcTemplate.executeQuery(SQLQueries.GET_APPLICATION, (resultSet, rowNumber) ->
                            new Application(resultSet.getString(1), resultSet.getString(2)),
                    preparedStatement -> preparedStatement.setString(1, sessionId));

            String userAgent = jdbcTemplate.fetchSingleRecord(SQLQueries.GET_USER_AGENT,
                    (resultSet, rowNumber) -> resultSet.getString(1), preparedStatement ->
                            preparedStatement.setString(1, sessionId));

            String ip = jdbcTemplate.fetchSingleRecord(SQLQueries.GET_IP, (resultSet, rowNumber) ->
                    resultSet.getString(1), preparedStatement -> preparedStatement.setString
                    (1, sessionId));

            String loginTime = jdbcTemplate.fetchSingleRecord(SQLQueries.GET_LOGIN_TIME,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    preparedStatement -> preparedStatement.setString(1, sessionId));

            String lastAccessTime = jdbcTemplate.fetchSingleRecord(SQLQueries.GET_LAST_ACCESS_TIME
                    , (resultSet, rowNumber) -> resultSet.getString(1),
                    preparedStatement -> preparedStatement.setString(1, sessionId));

            if (!applicationList.isEmpty()) {
                UserSession userSession = new UserSession();
                userSession.setApplications(applicationList.toArray(new Application[applicationList.size()]));
                userSession.setUserAgent(userAgent);
                userSession.setIp(ip);
                userSession.setLoginTime(loginTime);
                userSession.setLastAccessTime(lastAccessTime);
                userSession.setSessionId(sessionId);
                return userSession;
            }
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(SessionMgtConstants.ErrorMessages
                    .ERROR_CODE_UNABLE_TO_GET_SESSION, SessionMgtConstants.HttpStatusCode.ERROR_CODE_500,
                    "Server encountered an error while retrieving session information for " + sessionId, e);
        }
        return null;
    }
}
