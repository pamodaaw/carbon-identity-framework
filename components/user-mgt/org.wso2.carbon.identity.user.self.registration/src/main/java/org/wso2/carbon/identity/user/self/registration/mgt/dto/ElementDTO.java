package org.wso2.carbon.identity.user.self.registration.mgt.dto;

import java.util.HashMap;
import java.util.Map;

public class ElementDTO {

    private String id;
    private String category;
    private String type;
    private String variant;
    private Map<String, Object> properties;
    private ActionDTO actionDTO;

    public ElementDTO(String id, String category, String type, String variant) {

        this.id = id;
        this.category = category;
        this.type = type;
        this.variant = variant;
        properties = new HashMap<>();
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getVariant() {

        return variant;
    }

    public void setVariant(String variant) {

        this.variant = variant;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public void addProperty(String key, Object value) {

        this.properties.put(key, value);
    }

    public ActionDTO getActionDTO() {

        return actionDTO;
    }

    public void setActionDTO(ActionDTO actionDTO) {

        this.actionDTO = actionDTO;
    }
}
