package com.camunda.consulting.services;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorizationCheckerServiceImpl implements AuthorizationCheckerService {

    private final ProcessEngine engine;

    public AuthorizationCheckerServiceImpl(ProcessEngine engine) {
        this.engine = engine;
    }

    @Override
    public Boolean checkAuthorization(String userId, Permission permission, Resources resources, String resourceId){
        IdentityService identityService = engine.getIdentityService();
        AuthorizationService authorizationService = engine.getAuthorizationService();

        List<String> groupIds = identityService.createGroupQuery().groupMember(userId).list().stream()
                .map(Group::getId).collect(Collectors.toList());

        return authorizationService.isUserAuthorized(userId, groupIds, permission, resources, resourceId);
    }
}
