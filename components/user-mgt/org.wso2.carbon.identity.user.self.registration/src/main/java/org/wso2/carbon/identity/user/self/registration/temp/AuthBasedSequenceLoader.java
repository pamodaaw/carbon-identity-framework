/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.self.registration.temp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.self.registration.action.Authentication;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.self.registration.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.self.registration.executor.Executor;
import org.wso2.carbon.identity.user.self.registration.executor.impl.AttributeCollectorImpl;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.CombinedInputCollectionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.InputCollectNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.TaskExecutionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.UserOnboardNode;
import org.wso2.carbon.identity.user.self.registration.mgt.FlowConvertor;
import org.wso2.carbon.identity.user.self.registration.mgt.FlowToPageConvertor;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.NodeDTO;
import org.wso2.carbon.identity.user.self.registration.mgt.dto.RegistrationDTO;
import org.wso2.carbon.identity.user.self.registration.util.Constants;
import org.wso2.carbon.identity.user.self.registration.util.RegistrationFrameworkUtils;
import org.wso2.carbon.identity.user.self.registration.model.InputMetaData;
import org.wso2.carbon.identity.user.self.registration.model.RegSequence;
import org.wso2.carbon.identity.user.self.registration.graphexecutor.node.Node;
import org.wso2.carbon.identity.user.self.registration.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * This class is responsible for loading the authentication sequence based on the login sequence of the application.
 */
public class AuthBasedSequenceLoader {

    private static final Log LOG = LogFactory.getLog(AuthBasedSequenceLoader.class);

    public RegSequence loadSequence(String appId) {

        RegSequence sequence;
        if ("case1".equals(appId)) {
            sequence = loadSequence1();
        } else if ("case2".equals(appId)) {
            sequence = loadSequence2();
        } else if ("case3".equals(appId)) {
            sequence = loadSequence3();
        } else if("derive".equals(appId)) {
            LOG.info("Sequence deriving is not supported yet.");
            return null;
//            try {
//                sequence = new AuthBasedSequenceLoader().deriveRegistrationSequence(appId);
//            } catch (RegistrationFrameworkException e) {
//                LOG.error("Error while loading the sequence for the app: " + appId, e);
//                return null;
//            }
        } else {
            try {
                sequence = loadFlowFromFile(appId);
            } catch (RegistrationServerException e) {
                LOG.error("Error while loading the sequence from the database for the id: " + appId, e);
                return null;
            }
        }
        return sequence;
    }


//    private RegSequence deriveRegistrationSequence(String appId) throws RegistrationFrameworkException {
//
//        ServiceProvider serviceProvider = RegistrationFrameworkUtils.retrieveSpFromAppId(appId, "carbon.super");
//        AuthenticationStep[] authenticationSteps =
//                serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
//
//        RegSequence sequenceConfig = new RegSequence();
//
//        if (authenticationSteps == null || authenticationSteps.length == 0) {
//            return sequenceConfig;
//        }
//
//        Node currentNode = deriveAttributeCollectionStep(serviceProvider);
//        if (currentNode != null) {
//            sequenceConfig.setFirstNodeId(currentNode.getNodeId());
//        }
//        // For each authentication step, consider the registration supported steps.
//        for (AuthenticationStep authenticationStep : authenticationSteps) {
//
//            Node nextNode = defineNode(authenticationStep, serviceProvider.getTenantDomain());
//            if (nextNode == null) {
//                LOG.info("No supported registration executors in the " + authenticationStep.getStepOrder() + " step " +
//                                 "of the login flow.");
//                continue;
//            }
//            if (currentNode == null) {
//                sequenceConfig.setFirstNodeId(nextNode);
//            } else if (currentNode instanceof UserChoiceDecisionNode) {
//                for (Node option : ((UserChoiceDecisionNode) currentNode).getNextNodes()) {
//                    option.setNextNode(nextNode);
//                }
//            } else {
//                currentNode.setNextNode(nextNode);
//            }
//            currentNode = nextNode;
//        }
//        return sequenceConfig;
//    }

//    private Node defineNode(AuthenticationStep authenticationStep, String tenantDomain)
//            throws RegistrationFrameworkException {
//
//        List<Node> options = new ArrayList<>();
//
//        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
//        if (localAuthenticators != null) {
//            IdentityProvider localIdp = new IdentityProvider();
//            localIdp.setIdentityProviderName(FrameworkConstants.LOCAL_IDP_NAME);
//
//            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
//                Executor regExecutor = getRegExecutor(localAuthenticator.getName());
//                if (regExecutor != null) {
//                    TaskExecutionNode node = new TaskExecutionNode(regExecutor);
//                    options.add(node);
//                }
//            }
//        }
//
//        IdentityProvider[] federatedIDPs = authenticationStep.getFederatedIdentityProviders();
//        if (federatedIDPs != null) {
//            // For each idp in the step.
//            for (IdentityProvider federatedIDP : federatedIDPs) {
//                FederatedAuthenticatorConfig federatedAuthenticator = federatedIDP.getDefaultAuthenticatorConfig();
//
//                if (federatedAuthenticator == null) {
//                    try {
//                        federatedAuthenticator = IdentityProviderManager.getInstance()
//                                .getIdPByName(federatedIDP.getIdentityProviderName(), tenantDomain)
//                                .getDefaultAuthenticatorConfig();
//                    } catch (IdentityProviderManagementException e) {
//                        throw new RegistrationFrameworkException("Failed to load the default authenticator for IDP : "
//                                + federatedIDP.getIdentityProviderName(), e);
//                    }
//                }
//                Executor regExecutor = getRegExecutor(federatedAuthenticator.getName());
//                if (regExecutor != null) {
//                    String idpName = federatedIDP.getIdentityProviderName();
//                    TaskExecutionNode node = new TaskExecutionNode(regExecutor);
//                    options.add(node);
//                }
//            }
//        }
//
//        if (options.isEmpty()) {
//            return null;
//        }
//        if (options.size() > 1) {
//            UserChoiceDecisionNode userChoiceDecisionNode = new UserChoiceDecisionNode();
//            userChoiceDecisionNode.setNextNodes(options);
//            return userChoiceDecisionNode;
//        }
//        return options.get(0);
//    }

    private Executor getRegExecutor(String authenticatorName) {

        Executor mappedRegExecutor = null;
        for (Executor executor : UserRegistrationServiceDataHolder.getExecutors()) {
            if (executor instanceof Authentication && authenticatorName.equals(executor.getName())) {
                mappedRegExecutor = executor;
                break;
            }
        }

        return  mappedRegExecutor;
    }

    private Node deriveAttributeCollectionStep(ServiceProvider serviceProvider) {

        ClaimMapping[] requestedClaims = serviceProvider.getClaimConfig().getClaimMappings();

        if (requestedClaims == null || requestedClaims.length == 0) {
            return null;
        }

        AttributeCollectorImpl collector = getAttributeCollectionExecutor(
                requestedClaims);
        return new TaskExecutionNode(collector);
    }

    private AttributeCollectorImpl getAttributeCollectionExecutor(ClaimMapping[] requestedClaims) {

        AttributeCollectorImpl collector = new AttributeCollectorImpl();
        collector.setName("AttributeCollectorBasedOnAppClaims");

        int order = 0;
        for (ClaimMapping claimMapping : requestedClaims) {
            String claimUri = claimMapping.getLocalClaim().getClaimUri();
            String id = Base64.getEncoder().encodeToString(claimUri.getBytes(StandardCharsets.UTF_8));
            InputMetaData inputMetaData = new InputMetaData(id, claimUri, "STRING", ++order);
            inputMetaData.setMandatory(claimMapping.isMandatory());
            collector.addRequiredData(inputMetaData);
        }
        return collector;
    }

    private RegSequence loadSequence1() {

        AttributeCollectorImpl attrCollector1 = new AttributeCollectorImpl("AttributeCollector1");
        String emailId = Base64.getEncoder().encodeToString("emailaddress".getBytes(StandardCharsets.UTF_8));
        InputMetaData e1 = new InputMetaData(emailId, "emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollectorImpl attrCollector2 = new AttributeCollectorImpl("AttributeCollector2");
        String firstNameId = Base64.getEncoder().encodeToString("firstname".getBytes(StandardCharsets.UTF_8));
        String dobId = Base64.getEncoder().encodeToString("dob".getBytes(StandardCharsets.UTF_8));
        InputMetaData e2 = new InputMetaData(firstNameId, "firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData(dobId, "dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarderTest pwdOnboard = new PasswordOnboarderTest();
        EmailOTPExecutorTest emailOTPExecutor = new EmailOTPExecutorTest();

        TaskExecutionNode node1 = new TaskExecutionNode(attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode();
        TaskExecutionNode node3 = new TaskExecutionNode(pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode(emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode(attrCollector2);
        UserOnboardNode node6 = new UserOnboardNode();

        node1.setNextNodeId(node2.getNodeId());
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNodeId(node5.getNodeId());
        node4.setNextNodeId(node5.getNodeId());
        node5.setNextNodeId(node6.getNodeId());

        // Define the flow of the graph

        RegSequence sequence = new RegSequence(node1.getNodeId());
        sequence.addNode(node1);
        sequence.addNode(node2);
        sequence.addNode(node3);
        sequence.addNode(node4);
        sequence.addNode(node5);
        sequence.addNode(node6);
        return sequence;
    }

    private RegSequence loadSequence2() {

        AttributeCollectorImpl attrCollector1 = new AttributeCollectorImpl("AttributeCollector1");
        String emailId = Base64.getEncoder().encodeToString("emailaddress".getBytes(StandardCharsets.UTF_8));
        InputMetaData e1 = new InputMetaData(emailId, "emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollectorImpl attrCollector2 = new AttributeCollectorImpl("AttributeCollector2");
        String firstNameId = Base64.getEncoder().encodeToString("firstname".getBytes(StandardCharsets.UTF_8));
        String dobId = Base64.getEncoder().encodeToString("dob".getBytes(StandardCharsets.UTF_8));
        InputMetaData e2 = new InputMetaData(firstNameId, "firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData(dobId, "dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarderTest pwdOnboard = new PasswordOnboarderTest();
        EmailOTPExecutorTest emailOTPExecutor = new EmailOTPExecutorTest();

        TaskExecutionNode node1 = new TaskExecutionNode(attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode();
        TaskExecutionNode node3 = new TaskExecutionNode(pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode(emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode(attrCollector2);

        CombinedInputCollectionNode node0 = new CombinedInputCollectionNode();
        node0.setReferencedNodes(new ArrayList<>(Arrays.asList(node1, node2, node5)));

        node0.setNextNodeId(node1.getNodeId());
        node1.setNextNodeId(node2.getNodeId());
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNodeId(node5.getNodeId());
        node4.setNextNodeId(node5.getNodeId());

        RegSequence sequence = new RegSequence(node0.getNodeId());
        sequence.addNode(node0);
        sequence.addNode(node1);
        sequence.addNode(node2);
        sequence.addNode(node3);
        sequence.addNode(node4);
        sequence.addNode(node5);
        return sequence;
    }

    private RegSequence loadSequence3() {

        AttributeCollectorImpl attrCollector1 = new AttributeCollectorImpl("AttributeCollector1");
        String emailId = Base64.getEncoder().encodeToString("emailaddress".getBytes(StandardCharsets.UTF_8));
        InputMetaData e1 = new InputMetaData(emailId, "emailaddress", "STRING", 1);
        attrCollector1.addRequiredData(e1);

        AttributeCollectorImpl attrCollector2 = new AttributeCollectorImpl("AttributeCollector2");
        String firstNameId = Base64.getEncoder().encodeToString("firstname".getBytes(StandardCharsets.UTF_8));
        String dobId = Base64.getEncoder().encodeToString("dob".getBytes(StandardCharsets.UTF_8));
        InputMetaData e2 = new InputMetaData(firstNameId, "firstname", "STRING", 1);
        InputMetaData e3 = new InputMetaData(dobId, "dob", "DATE", 2);
        attrCollector2.addRequiredData(e2);
        attrCollector2.addRequiredData(e3);

        PasswordOnboarderTest pwdOnboard = new PasswordOnboarderTest();
        EmailOTPExecutorTest emailOTPExecutor = new EmailOTPExecutorTest();

        TaskExecutionNode node1 = new TaskExecutionNode(attrCollector1);
        UserChoiceDecisionNode node2 = new UserChoiceDecisionNode();
        TaskExecutionNode node3 = new TaskExecutionNode(pwdOnboard);
        TaskExecutionNode node4 = new TaskExecutionNode(emailOTPExecutor);
        TaskExecutionNode node5 = new TaskExecutionNode(attrCollector2);

        node1.setNextNodeId(node2.getNodeId());
        node2.setNextNodes(new ArrayList<>(Arrays.asList(node3, node4)));
        node3.setNextNodeId(node5.getNodeId());
        node4.setNextNodeId(node5.getNodeId());

        // Define the flow of the graph

        RegSequence sequence = new RegSequence(node1.getNodeId());
        sequence.addNode(node1);
        sequence.addNode(node2);
        sequence.addNode(node3);
        sequence.addNode(node4);
        sequence.addNode(node5);
        return sequence;
    }

//    private RegSequence loadFlowFromDB(String flowId) throws RegistrationServerException {
//
//        RegistrationDTO regDto = null;
//        try {
//            regDto = FlowConvertor.adapt(flowId);
//        } catch (IOException e) {
//            throw new RegistrationServerException("Error while converting the registration flow.", e);
//        }
////        RegistrationDTO regDto;
////
////        try {
////            regDto = RegistrationFlowDAO.retrieveRegistrationFlow(appId);
////        } catch (SQLException e) {
////            LOG.error("Error while loading the sequence for the app: " + appId, e);
////            return null;
////        }
//        RegSequence sequence = new RegSequence();
//        Map<String, Node> nodeMap = new HashMap<>();
//        sequence.setId(regDto.getFlowID());
//        for (NodeDTO nodeDTO : regDto.getNodeDTOList().values()) {
//            Node node;
//
//            if ("EXECUTOR".equals(nodeDTO.getType())) {
//                String executorId = nodeDTO.getProperties().get("EXECUTOR_ID");
//                if (executorId == null) {
//                    throw new RegistrationServerException("Executor ID is not defined for the node: " + nodeDTO.getId());
//                }
//                if (nodeDTO.getNextNodes().size() > 1) {
//                    throw new RegistrationServerException("Multiple next nodes are defined for the executor node: " +
//                                                                  nodeDTO.getId());
//                }
//                Executor mappedRegExecutor = null;
//                if (executorId.equals("EmailOTPVerifier")) {
//                    mappedRegExecutor = new EmailOTPExecutorTest();
//                } else if (executorId.equals("PasswordOnboarder")) {
//                    mappedRegExecutor = new PasswordOnboarderTest();
//                } else if (executorId.equals("GoogleSignUp")) {
//                    mappedRegExecutor = new GoogleSignupTest();
//                } else {
//                    throw new RegistrationServerException("Unsupported executor ID: " + executorId);
//                }
//                node = new TaskExecutionNode(mappedRegExecutor);
//            } else if ("DECISION".equals(nodeDTO.getType())) {
//                if (nodeDTO.getNextNodes().size() < 2) {
//                    throw new RegistrationServerException(
//                            "Less than two next nodes are defined for the decision node: " +
//                                    nodeDTO.getId());
//                }
//                node = new UserChoiceDecisionNode(nodeDTO.getId());
//            } else if ("INPUT".equals(nodeDTO.getType())) {
//                if (nodeDTO.getNextNodes().size() > 1) {
//                    throw new RegistrationServerException("Multiple next nodes are defined for the executor node: " +
//                                                                  nodeDTO.getId());
//                }
//                node = new InputCollectNode(nodeDTO.getId());
//            } else {
//                throw new RegistrationServerException("Unsupported node type: " + nodeDTO.getType());
//            }
//            // Iterate properties and set them to the node.
////            for (Map.Entry<String, String> property : nodeDTO.getProperties().entrySet()) {
////                if (property.getKey().startsWith("PAGE_ID")) {
////                    node.addPageId(property.getKey(), property.getValue());
////                }
////            }
////            nodeDTO.getPageIds().forEach(node::addPageId);
//            nodeMap.put(nodeDTO.getId(), node);
//        }
//
//        for(NodeDTO nodeDTO : regDto.getNodeDTOList().values()) {
//            if (nodeDTO.getNextNodes().size() == 1) {
//                nodeMap.get(nodeDTO.getId()).setNextNode(nodeMap.get(nodeDTO.getNextNodes().get(0)));
//            } else if (nodeDTO.getNextNodes().size() > 1) {
//                List<Node> nextNodes = new ArrayList<>();
//                for (String nextNodeId : nodeDTO.getNextNodes()) {
//                    nextNodes.add(nodeMap.get(nextNodeId));
//                }
//                ((UserChoiceDecisionNode) nodeMap.get(nodeDTO.getId())).setNextNodes(nextNodes);
//            }
//        }
//
//        sequence.setFirstNodeId(nodeMap.get(regDto.getFirstNode()));
//
//        return sequence;
//        // Load the sequence from the database
//    }

    private RegSequence loadFlowFromFile(String flowId) throws RegistrationServerException {

        RegistrationDTO regDto;
        try {
            regDto = FlowConvertor.adapt(flowId);
            regDto.setPageDTOs(FlowToPageConvertor.convert(flowId));
        } catch (IOException e) {
            throw new RegistrationServerException("Error while converting the registration flow.", e);
        }

        RegSequence sequence = new RegSequence();
        sequence.setId(regDto.getFlowID());
        sequence.setPageDTOMap(regDto.getPageDTOs());
        for (NodeDTO nodeDTO : regDto.getNodeDTOList().values()) {
            Node node;

            if ("EXECUTOR".equals(nodeDTO.getType())) {
                String executorId = nodeDTO.getProperties().get("EXECUTOR_ID");
                if (executorId == null) {
                    throw new RegistrationServerException(
                            "Executor ID is not defined for the node: " + nodeDTO.getId());
                }
                if (nodeDTO.getNextNodes().size() > 1) {
                    throw new RegistrationServerException("Multiple next nodes are defined for the executor node: " +
                                                                  nodeDTO.getId());
                }
                Executor mappedRegExecutor = null;
                if (executorId.equals(Constants.EMAIL_OTP_EXECUTOR_NAME)) {
                    mappedRegExecutor = new EmailOTPExecutorTest();
                } else if (executorId.equals(Constants.PWD_EXECUTOR_NAME)) {
                    mappedRegExecutor = new PasswordOnboarderTest();
                } else if (executorId.equals(Constants.GOOGLE_EXECUTOR_NAME)) {
                    mappedRegExecutor = new GoogleSignupTest();
                } else {
                    throw new RegistrationServerException("Unsupported executor ID: " + executorId);
                }
                node = new TaskExecutionNode(nodeDTO.getId(), mappedRegExecutor);
            } else if ("DECISION".equals(nodeDTO.getType())) {
                if (nodeDTO.getNextNodes().size() < 2) {
                    throw new RegistrationServerException(
                            "Less than two next nodes are defined for the decision node: " +
                                    nodeDTO.getId());
                }
                node = new UserChoiceDecisionNode(nodeDTO.getId());
            } else if ("INPUT".equals(nodeDTO.getType())) {
                if (nodeDTO.getNextNodes().size() > 1) {
                    throw new RegistrationServerException("Multiple next nodes are defined for the executor node: " +
                                                                  nodeDTO.getId());
                }
                node = new InputCollectNode(nodeDTO.getId());
            } else if ("USER_ONBOARDING".equals(nodeDTO.getType())) {
                node = new UserOnboardNode(nodeDTO.getId());
            } else {
                throw new RegistrationServerException("Unsupported node type: " + nodeDTO.getType());
            }

            for (Map.Entry<String, String> pageDetail : nodeDTO.getPageIds().entrySet()) {

                String pageRefId = nodeDTO.getId() + "_" + pageDetail.getKey();
                sequence.addPageDTO(pageRefId, pageDetail.getValue());
            }
            sequence.addNode(node);
        }

        sequence.setFirstNodeId(regDto.getFirstNode());

        for(NodeDTO nodeDTO : regDto.getNodeDTOList().values()) {
            if (nodeDTO.getNextNodes().size() == 1) {
                sequence.getNodeList(nodeDTO.getId()).setNextNodeId(nodeDTO.getNextNodes().get(0));
            } else if (nodeDTO.getNextNodes().size() > 1) {
                List<Node> nextNodes = new ArrayList<>();
                for (String nextNodeId : nodeDTO.getNextNodes()) {
                    nextNodes.add(sequence.getNodeList(nextNodeId));
                }
                ((UserChoiceDecisionNode) sequence.getNodeList(nodeDTO.getId())).setNextNodes(nextNodes);
            }
        }
        return sequence;
    }
}
