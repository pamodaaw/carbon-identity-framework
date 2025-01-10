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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Response to resend recovery code request.
 **/
public class ResendResponse {


    private String code;
    private String message;
    private String flowConfirmationCode;
    private String notificationChannel;
    private String resendCode;
    private List<APICall> links = null;


    /**
     * Success status code.
     **/
    public ResendResponse code(String code) {

        this.code = code;
        return this;
    }


    @JsonProperty("code")
    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    /**
     * Success status message.
     *
     * @param message message
     * @return ResendResponse
     */
    public ResendResponse message(String message) {

        this.message = message;
        return this;
    }

    @JsonProperty("message")
    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    /**
     * Confirmation code of the recovery flow.
     *
     * @param flowConfirmationCode flowConfirmationCode
     * @return ResendResponse
     */
    public ResendResponse flowConfirmationCode(String flowConfirmationCode) {

        this.flowConfirmationCode = flowConfirmationCode;
        return this;
    }

    @JsonProperty("flowConfirmationCode")
    public String getFlowConfirmationCode() {

        return flowConfirmationCode;
    }

    public void setFlowConfirmationCode(String flowConfirmationCode) {

        this.flowConfirmationCode = flowConfirmationCode;
    }

    /**
     * Set the notification channel that user prefers to get recovery notifications.
     *
     * @param notificationChannel notificationChannel
     * @return ResendResponse
     */
    public ResendResponse notificationChannel(String notificationChannel) {

        this.notificationChannel = notificationChannel;
        return this;
    }

    @JsonProperty("notificationChannel")
    public String getNotificationChannel() {

        return notificationChannel;
    }

    public void setNotificationChannel(String notificationChannel) {

        this.notificationChannel = notificationChannel;
    }

    /**
     * Code to resend the notification to the user via user selected channel.
     *
     * @param resendCode resendCode
     * @return ResendResponse
     */
    public ResendResponse resendCode(String resendCode) {

        this.resendCode = resendCode;
        return this;
    }

    @JsonProperty("resendCode")
    public String getResendCode() {

        return resendCode;
    }

    public void setResendCode(String resendCode) {

        this.resendCode = resendCode;
    }

    /**
     * Links for next requests.
     *
     * @param links links
     * @return ResendResponse
     */
    public ResendResponse links(List<APICall> links) {

        this.links = links;
        return this;
    }

    @JsonProperty("links")
    public List<APICall> getLinks() {

        return links;
    }

    public void setLinks(List<APICall> links) {

        this.links = links;
    }

    /**
     * Add linksItem.
     *
     * @param linksItem linksItem.
     * @return ResendResponse.
     */
    public ResendResponse addLinksItem(APICall linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResendResponse passwordRecoveryInternalNotifyResponse = (ResendResponse) o;
        return Objects.equals(this.code, passwordRecoveryInternalNotifyResponse.code) &&
                Objects.equals(this.message, passwordRecoveryInternalNotifyResponse.message) &&
                Objects.equals(this.notificationChannel, passwordRecoveryInternalNotifyResponse.notificationChannel) &&
                Objects.equals(this.resendCode, passwordRecoveryInternalNotifyResponse.resendCode) &&
                Objects.equals(this.links, passwordRecoveryInternalNotifyResponse.links);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, message, notificationChannel, resendCode, links);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResendResponse {\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    notificationChannel: ").append(toIndentedString(notificationChannel)).append("\n");
        sb.append("    resendCode: ").append(toIndentedString(resendCode)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
