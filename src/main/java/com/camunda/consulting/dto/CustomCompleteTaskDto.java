package com.camunda.consulting.dto;

import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;

public class CustomCompleteTaskDto {

    private UserIdDto userIdDto;
    private CompleteTaskDto completeTaskDto;

    public UserIdDto getUserIdDto() {
        return userIdDto;
    }

    public void setUserIdDto(UserIdDto userIdDto) {
        this.userIdDto = userIdDto;
    }

    public CompleteTaskDto getCompleteTaskDto() {
        return completeTaskDto;
    }

    public void setCompleteTaskDto(CompleteTaskDto completeTaskDto) {
        this.completeTaskDto = completeTaskDto;
    }
}
