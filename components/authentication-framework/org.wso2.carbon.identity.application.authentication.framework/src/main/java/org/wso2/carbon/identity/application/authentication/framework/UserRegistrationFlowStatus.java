package org.wso2.carbon.identity.application.authentication.framework;

import java.io.Serializable;

/**
 * UserRegistrationFlowStatus enum.
 */
public enum UserRegistrationFlowStatus implements Serializable {

        CREATED, CREDENTIAL_ENROLLED, EMAIL_VERIFIED, MOBILE_VERIFIED, INCOMPLETE
    }
