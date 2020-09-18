package com.camunda.consulting.proxy;

import com.camunda.consulting.dto.CustomCompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;

import javax.ws.rs.core.Response;

public interface TaskServiceProxy {

    Response complete(String taskId, CustomCompleteTaskDto dto);
    void claim(UserIdDto dto, String taskId);
}
