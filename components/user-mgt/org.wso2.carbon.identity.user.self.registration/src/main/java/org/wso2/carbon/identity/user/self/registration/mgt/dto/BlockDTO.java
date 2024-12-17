package org.wso2.carbon.identity.user.self.registration.mgt.dto;

import java.util.ArrayList;
import java.util.List;

public class BlockDTO {

    private String id;
    private final List<String> nodes = new ArrayList<>();

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public List<String> getElementIds() {

        return nodes;
    }

    public void addElementId(String elementId) {

        this.nodes.add(elementId);
    }
}
