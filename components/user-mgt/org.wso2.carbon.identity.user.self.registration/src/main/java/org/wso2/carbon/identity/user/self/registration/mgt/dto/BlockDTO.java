package org.wso2.carbon.identity.user.self.registration.mgt.dto;

import java.util.ArrayList;
import java.util.List;

public class BlockDTO {

    private String id;
    private final List<String> elements = new ArrayList<>();

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public List<String> getElementIds() {

        return elements;
    }

    public void addElementId(String elementId) {

        this.elements.add(elementId);
    }
}
