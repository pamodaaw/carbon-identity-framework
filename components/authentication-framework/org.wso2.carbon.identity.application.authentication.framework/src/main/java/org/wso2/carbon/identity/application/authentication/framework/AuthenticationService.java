/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.exception.auth.service.AuthServiceException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceErrorInfo;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceRequestWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponse;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponseData;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The authentication service class.
 * The class uses request and response wrappers to communicate with the authentication framework.
 */
public class AuthenticationService {

    private static final Log LOG = LogFactory.getLog(AuthenticationService.class);
    private final CommonAuthenticationHandler commonAuthenticationHandler = new CommonAuthenticationHandler();

    /**
     * Handles the authentication request.
     *
     * @param authRequest The authentication request.
     * @return The authentication response.
     * @throws AuthServiceException If an error occurs while handling the authentication request.
     */
    public AuthServiceResponse handleAuthentication(AuthServiceRequest authRequest) throws AuthServiceException {

        AuthServiceRequestWrapper wrappedRequest = getWrappedRequest(authRequest.getRequest(),
                authRequest.getParameters());
        AuthServiceResponseWrapper wrappedResponse = getWrappedResponse(authRequest.getResponse());
        try {
            commonAuthenticationHandler.doPost(wrappedRequest, wrappedResponse);
        } catch (ServletException | IOException e) {
            throw new AuthServiceException("Error while handling authentication request.", e);
        }

        return processCommonAuthResponse(wrappedRequest, wrappedResponse);
    }

    private AuthServiceRequestWrapper getWrappedRequest(HttpServletRequest request, Map<String, String[]> parameters) {

        return new AuthServiceRequestWrapper(request, parameters);
    }

    private AuthServiceResponseWrapper getWrappedResponse(HttpServletResponse response) {

        return new AuthServiceResponseWrapper(response);
    }

    private AuthServiceResponse processCommonAuthResponse(AuthServiceRequestWrapper request,
                                                          AuthServiceResponseWrapper response)
            throws AuthServiceException {

        AuthServiceResponse authServiceResponse = new AuthServiceResponse();

        /* This order of flow checking should be maintained as some of the
         error flows could come with flow status INCOMPLETE.*/
        if (isAuthFlowSuccessful(request)) {
            handleSuccessAuthResponse(request, response, authServiceResponse);
        } else if (isAuthFlowFailed(request, response)) {
            handleFailedAuthResponse(request, response, authServiceResponse);
        } else if (isAuthFlowIncomplete(request)) {
            handleIntermediateAuthResponse(request, response, authServiceResponse);
        } else {
            throw new AuthServiceException("Unknown authentication flow status: " + request.getAuthFlowStatus());
        }

        return authServiceResponse;
    }

    private void handleIntermediateAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response,
                                                AuthServiceResponse authServiceResponse) throws AuthServiceException {

        authServiceResponse.setSessionDataKey(request.getSessionDataKey());
        authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.INCOMPLETE);
        AuthServiceResponseData responseData = new AuthServiceResponseData();
        boolean isMultiOptionsResponse = request.isMultiOptionsResponse();

        List<AuthenticatorData> authenticatorDataList;
        if (isMultiOptionsResponse) {
            responseData.setAuthenticatorSelectionRequired(true);
            authenticatorDataList = getAuthenticatorBasicData(response.getAuthenticators(),
                    request.getAuthInitiationData());
        } else {
            authenticatorDataList = request.getAuthInitiationData();
        }
        responseData.setAuthenticatorOptions(authenticatorDataList);
        authServiceResponse.setData(responseData);
    }

    private void handleSuccessAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response,
                                           AuthServiceResponse authServiceResponse) throws AuthServiceException {

        authServiceResponse.setSessionDataKey(getFlowCompletionSessionDataKey(request, response));
        authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.SUCCESS_COMPLETED);
    }

    private void handleFailedAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response,
                                          AuthServiceResponse authServiceResponse) throws AuthServiceException {

        String errorCode = null;
        String errorMessage = null;
        if (request.isAuthFlowConcluded()) {
            authServiceResponse.setSessionDataKey(request.getSessionDataKey());
            authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.FAIL_COMPLETED);
            AuthenticationResult authenticationResult = getAuthenticationResult(request);
            if (authenticationResult != null) {
                errorCode = (String) authenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_CODE);
                errorMessage = (String) authenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_MSG);
            }
        } else {
            authServiceResponse.setSessionDataKey(request.getSessionDataKey());
            authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.FAIL_INCOMPLETE);
            List<AuthenticatorData> authenticatorDataList = request.getAuthInitiationData();
            AuthServiceResponseData responseData = new AuthServiceResponseData(authenticatorDataList);
            authServiceResponse.setData(responseData);
            errorCode = getErrorCode(response);
            errorMessage = getErrorMessage(response);
        }

        if (StringUtils.isBlank(errorCode)) {
            errorCode = AuthServiceConstants.ERROR_CODE_UNKNOWN_ERROR;
        }

        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = AuthServiceConstants.ERROR_MSG_UNKNOWN_ERROR;
        }

        AuthServiceErrorInfo errorInfo = new AuthServiceErrorInfo(errorCode, errorMessage);
        authServiceResponse.setErrorInfo(errorInfo);
    }

    private String getErrorCode(AuthServiceResponseWrapper response) throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(response.getRedirectURL());
        return queryParams.get(AuthServiceConstants.ERROR_CODE_PARAM);
    }

    private String getErrorMessage(AuthServiceResponseWrapper response) throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(response.getRedirectURL());
        return queryParams.get(AuthServiceConstants.AUTH_FAILURE_MSG_PARAM);
    }

    private List<AuthenticatorData> getAuthenticatorBasicData(String authenticatorList,
                                                              List<AuthenticatorData> authInitiationData)
            throws AuthServiceException {

        List<AuthenticatorData> authenticatorDataList = new ArrayList<>();
        String[] authenticatorAndIdpsArr = StringUtils.split(authenticatorList,
                AuthServiceConstants.AUTHENTICATOR_SEPARATOR);
        for (String authenticatorAndIdps : authenticatorAndIdpsArr) {
            String[] authenticatorIdpSeperatedArr = StringUtils.split(authenticatorAndIdps,
                    AuthServiceConstants.AUTHENTICATOR_IDP_SEPARATOR);
            String name = authenticatorIdpSeperatedArr[0];

            // Some authentication options would directly send the complete data. ex: basic authenticator.
            AuthenticatorData authenticatorData = getAuthenticatorData(name, authInitiationData);
            if (authenticatorData != null) {
                authenticatorDataList.add(authenticatorData);
                continue;
            }

            ApplicationAuthenticator authenticator = FrameworkUtils.getAppAuthenticatorByName(name);
            if (authenticator == null) {
                throw new AuthServiceException("Authenticator not found for name: " + name);
            }

            if (!authenticator.isAPIBasedAuthenticationSupported()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticator: " + name + " does not support API based authentication.");
                }
                continue;
            }

            // The first element is the authenticator name hence its skipped to get the idp.
            for (int i = 1; i < authenticatorIdpSeperatedArr.length; i++) {
                String idp = authenticatorIdpSeperatedArr[i];
                authenticatorData = new AuthenticatorData();
                authenticatorData.setName(name);
                authenticatorData.setIdp(idp);
                authenticatorData.setDisplayName(authenticator.getFriendlyName());
                authenticatorData.setI18nKey(authenticator.getI18nKey());
                authenticatorDataList.add(authenticatorData);
            }
        }
        return authenticatorDataList;
    }

    private AuthenticatorData getAuthenticatorData(String authenticator,
                                                   List<AuthenticatorData> authenticatorDataList) {

        for (AuthenticatorData authenticatorData : authenticatorDataList) {
            if (StringUtils.equals(authenticatorData.getName(), authenticator)) {
                return authenticatorData;
            }
        }
        return null;
    }

    private boolean isAuthFlowSuccessful(AuthServiceRequestWrapper request) {

        return AuthenticatorFlowStatus.SUCCESS_COMPLETED == request.getAuthFlowStatus();
    }

    private boolean isAuthFlowFailed(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response)
            throws AuthServiceException {

        return AuthenticatorFlowStatus.FAIL_COMPLETED == request.getAuthFlowStatus() || response.isErrorResponse() ||
                isSentToRetryPageOnMissingContext(request, response);
    }

    private boolean isAuthFlowIncomplete(AuthServiceRequestWrapper request) {

        return AuthenticatorFlowStatus.INCOMPLETE == request.getAuthFlowStatus();
    }

    private AuthenticationResult getAuthenticationResult(AuthServiceRequestWrapper request) {

        AuthenticationResult authenticationResult =
                (AuthenticationResult) request.getAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);
        if (authenticationResult == null) {
            AuthenticationResultCacheEntry authenticationResultCacheEntry =
                    FrameworkUtils.getAuthenticationResultFromCache(request.getSessionDataKey());
            if (authenticationResultCacheEntry != null) {
                authenticationResult = authenticationResultCacheEntry.getResult();
            }
        }
        return authenticationResult;
    }

    private boolean isSentToRetryPageOnMissingContext(AuthServiceRequestWrapper request,
                                                      AuthServiceResponseWrapper response) throws AuthServiceException {

        // If it's a retry due to context being null there is nothing to retry again the flow should be restarted.
        if (AuthenticatorFlowStatus.INCOMPLETE == request.getAuthFlowStatus() &&
                Boolean.TRUE.equals(request.getAttribute(FrameworkConstants.IS_SENT_TO_RETRY))) {
            Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(response.getRedirectURL());
            return StringUtils.equals(queryParams.get(FrameworkConstants.STATUS_PARAM),
                    FrameworkConstants.ERROR_STATUS_AUTH_CONTEXT_NULL);
        }
        return false;
    }

    private String getFlowCompletionSessionDataKey(AuthServiceRequestWrapper request,
                                                   AuthServiceResponseWrapper response) throws AuthServiceException {

        String completionSessionDataKey = (String) request.getAttribute(FrameworkConstants.SESSION_DATA_KEY);
        if (StringUtils.isBlank(completionSessionDataKey)) {
            completionSessionDataKey = response.getSessionDataKey();
        }

        return completionSessionDataKey;
    }
}
