package org.wso2.carbon.identity.user.self.registration.mgt.dto;

import java.util.ArrayList;
import java.util.List;

public class ActionDTO {

    private String type;
    private List<ExecutorDTO> executors = new ArrayList<>();

    public ActionDTO(String type) {
        this.type = type;
    }


    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public List<ExecutorDTO> getExecutors() {

        return executors;
    }

    public void addExecutor(ExecutorDTO executorDTO) {

        this.executors.add(executorDTO);
    }
}

