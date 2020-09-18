package com.camunda.consulting.services;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resources;

public interface AuthorizationCheckerService {

    Boolean checkAuthorization(String userId, Permission permission, Resources resources, String resourceId);
}
