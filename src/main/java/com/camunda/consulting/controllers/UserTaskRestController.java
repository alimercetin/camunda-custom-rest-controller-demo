package com.camunda.consulting.controllers;

import com.camunda.consulting.dto.CustomCompleteTaskDto;
import com.camunda.consulting.proxy.TaskServiceProxy;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/custom/task")
public class UserTaskRestController {

    private final TaskServiceProxy taskServiceProxy;

    public UserTaskRestController(TaskServiceProxy taskServiceProxy) {
        this.taskServiceProxy = taskServiceProxy;
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity complete(@PathVariable String taskId, @RequestBody CustomCompleteTaskDto dto) {
        return convertFromResponse(taskServiceProxy.complete(taskId, dto));
    }

    @PostMapping("/{taskId}/claim")
    public void claim(@RequestBody UserIdDto dto, @PathVariable String taskId) {
        taskServiceProxy.claim(dto, taskId);
    }

    private ResponseEntity convertFromResponse(Response response){
        return new ResponseEntity(response.getEntity(), HttpStatus.resolve(response.getStatus()));
    }

}
