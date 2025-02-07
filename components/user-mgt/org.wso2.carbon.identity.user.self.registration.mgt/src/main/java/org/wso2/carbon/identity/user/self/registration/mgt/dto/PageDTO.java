/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.self.registration.mgt.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.BlockDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.ElementDTO;

public class PageDTO {

    private String id;
    List<ElementDTO> elements = new ArrayList<>();
    List<BlockDTO> blocks = new ArrayList<>();

    public PageDTO() {

    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public List<ElementDTO> getElements() {

        return elements;
    }

    public void setElements(List<ElementDTO> elements) {

        this.elements = elements;
    }

    public void addElement(ElementDTO element) {

        this.elements.add(element);
    }

    public void addBlock(BlockDTO block) {

        this.blocks.add(block);
    }

    public List<BlockDTO> getBlocks() {

        return blocks;
    }
}
