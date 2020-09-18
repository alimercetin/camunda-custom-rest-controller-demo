package com.camunda.consulting.proxy;

import com.camunda.consulting.dto.CustomCompleteTaskDto;
import com.camunda.consulting.services.AuthorizationCheckerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.rest.sub.task.impl.TaskResourceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.Response;

@Component
public class TaskServiceProxyImpl implements TaskServiceProxy{

    private final ProcessEngine engine;
    private final AuthorizationCheckerService authorizationChecker;
    private final ObjectMapper objectMapper;
    protected final String relativeRootResourcePath = "/";

    public TaskServiceProxyImpl(ProcessEngine engine, AuthorizationCheckerService authorizationChecker, ObjectMapper objectMapper) {
        this.engine = engine;
        this.authorizationChecker = authorizationChecker;
        this.objectMapper = objectMapper;
    }

    @Override
    public Response complete(String taskId, CustomCompleteTaskDto dto) {
        Boolean isAuthorized = authorizationChecker.checkAuthorization(dto.getUserIdDto().getUserId(), Permissions.UPDATE, Resources.TASK, taskId);

        if(isAuthorized) {
            TaskResourceImpl taskResource = new TaskResourceImpl(engine, taskId, relativeRootResourcePath, objectMapper);
            return taskResource.complete(dto.getCompleteTaskDto());
        }
        else{
            String errorMessage = String.format("Cannot complete task %s: no authorizations found for the user %s",
                    taskId, dto.getUserIdDto().getUserId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
        }
    }

    @Override
    public void claim(UserIdDto dto, String taskId) {
        Boolean isAuthorized = authorizationChecker.checkAuthorization(dto.getUserId(), Permissions.UPDATE, Resources.TASK, taskId);

        if(isAuthorized) {
            TaskResourceImpl taskResource = new TaskResourceImpl(engine, taskId, relativeRootResourcePath, objectMapper);
            taskResource.claim(dto);
        }
        else {
            String errorMessage = String.format("Cannot claim task %s: no authorizations found for the user %s",
                    taskId, dto.getUserId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
        }
    }
}
