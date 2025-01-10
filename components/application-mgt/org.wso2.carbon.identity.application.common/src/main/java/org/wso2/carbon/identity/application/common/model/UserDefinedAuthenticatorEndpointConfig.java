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

package org.wso2.carbon.identity.application.common.model;

import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * The authenticator endpoint configuration model for the user defined authenticator configurations.
 */
public class UserDefinedAuthenticatorEndpointConfig {

    private final EndpointConfig endpointConfig;

    private UserDefinedAuthenticatorEndpointConfig(UserDefinedAuthenticatorEndpointConfigBuilder builder) {

        endpointConfig = builder.endpointConfig;
    }
    
    public EndpointConfig getEndpointConfig() {

        return endpointConfig;
    }

    /**
     * Get the URI of the authenticator endpoint of the user defined authenticator.
     *
     * @return URI of the authenticator endpoint.
     */
    public String getAuthenticatorEndpointUri() {

        return endpointConfig.getUri();
    }

    /**
     * Get the authentication type of the authenticator endpoint of the user defined authenticator.
     *
     * @return Authentication type of the authenticator endpoint.
     */
    public String getAuthenticatorEndpointAuthenticationType() {

        return endpointConfig.getAuthentication().getType().getName();
    }

    /**
     * Get the authentication properties of the authenticator endpoint of the user defined authenticator.
     *
     * @return Authentication properties of the authenticator endpoint.
     */
    public Map<String, String> getAuthenticatorEndpointAuthenticationProperties() {

        Map<String, String> propertyMap = new HashMap<>();
        for (AuthProperty prop: endpointConfig.getAuthentication().getProperties()) {
            propertyMap.put(prop.getName(), prop.getValue());
        }
        return propertyMap;
    }

    /**
     * UserDefinedAuthenticatorEndpointConfig builder.
     */
    public static class UserDefinedAuthenticatorEndpointConfigBuilder {

        private String uri;
        private String authenticationType;
        private Map<String, String> authenticationProperties;
        private EndpointConfig endpointConfig;

        public UserDefinedAuthenticatorEndpointConfigBuilder() {
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder uri(String uri) {

            this.uri = uri;
            return this;
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder authenticationProperties(
                Map<String, String> authentication) {

            this.authenticationProperties = authentication;
            return this;
        }

        public UserDefinedAuthenticatorEndpointConfigBuilder authenticationType(String authenticationType) {

            this.authenticationType = authenticationType;
            return this;
        }
        
        public UserDefinedAuthenticatorEndpointConfig build() {

            EndpointConfig.EndpointConfigBuilder endpointConfigBuilder = new EndpointConfig.EndpointConfigBuilder();
            endpointConfigBuilder.uri(uri);
            endpointConfigBuilder.authentication(new Authentication.AuthenticationBuilder()
                        .type(Authentication.Type.valueOf(authenticationType))
                        .properties(authenticationProperties)
                        .build());
            endpointConfig = endpointConfigBuilder.build();

            return new UserDefinedAuthenticatorEndpointConfig(this);
        }
    }
}
