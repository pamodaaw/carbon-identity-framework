package org.wso2.carbon.identity.user.self.registration.mgt.dto;

public class ActionDTO {

    private String type;
    private String name;

    public ActionDTO(String type) {
        this.type = type;
    }

    public ActionDTO(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}
