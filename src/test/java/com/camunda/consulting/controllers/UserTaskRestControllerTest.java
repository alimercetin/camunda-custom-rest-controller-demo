package com.camunda.consulting.controllers;

import com.camunda.consulting.dto.CustomCompleteTaskDto;
import com.camunda.consulting.proxy.TaskServiceProxy;
import com.camunda.consulting.proxy.TaskServiceProxyImpl;
import com.camunda.consulting.services.AuthorizationCheckerService;
import com.camunda.consulting.services.AuthorizationCheckerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Deployment(resources = "process.bpmn")
public class UserTaskRestControllerTest {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    private TaskServiceProxy taskServiceProxy;
    private AuthorizationCheckerService authorizationChecker;

    private UserTaskRestController userTaskRestController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private UserIdDto demoUserIdDto;
    private UserIdDto sampleUserDto;
    private CustomCompleteTaskDto demoCustomCompleteTaskDto;
    private CustomCompleteTaskDto sampleUserCustomCompleteTaskDto;

    @Before
    public void setup(){
        init(rule.getProcessEngine());
        IdentityService identityService = rule.getProcessEngine().getIdentityService();

        User sampleUser = new UserEntity();
        sampleUser.setId("sampleuser");
        sampleUser.setFirstName("Sample");
        sampleUser.setFirstName("User");
        sampleUser.setPassword(UUID.randomUUID().toString());

        Group sampleGroup = new GroupEntity();
        sampleGroup.setId("sample");
        sampleGroup.setName("sample");

        User demo = new UserEntity();
        demo.setId("demo");
        demo.setFirstName("Demo");
        demo.setFirstName("Demo");
        demo.setPassword(UUID.randomUUID().toString());

        identityService.saveUser(sampleUser);
        identityService.saveGroup(sampleGroup);
        identityService.createMembership(sampleUser.getId(), sampleGroup.getId());

        identityService.saveUser(demo);

        authorizationChecker = new AuthorizationCheckerServiceImpl(rule.getProcessEngine());

        taskServiceProxy = new TaskServiceProxyImpl(rule.getProcessEngine(), authorizationChecker, objectMapper);

        userTaskRestController = new UserTaskRestController(taskServiceProxy);
        mockMvc = MockMvcBuilders.standaloneSetup(userTaskRestController).build();

        demoUserIdDto = new UserIdDto();
        demoUserIdDto.setUserId("demo");

        sampleUserDto = new UserIdDto();
        sampleUserDto.setUserId("sampleuser");

        CompleteTaskDto completeTaskDto = new CompleteTaskDto();
        completeTaskDto.setWithVariablesInReturn(true);

        demoCustomCompleteTaskDto = new CustomCompleteTaskDto();
        demoCustomCompleteTaskDto.setUserIdDto(demoUserIdDto);
        demoCustomCompleteTaskDto.setCompleteTaskDto(completeTaskDto);

        sampleUserCustomCompleteTaskDto = new CustomCompleteTaskDto();
        sampleUserCustomCompleteTaskDto.setUserIdDto(sampleUserDto);
        sampleUserCustomCompleteTaskDto.setCompleteTaskDto(completeTaskDto);
    }

    @After
    public void clear(){
        IdentityService identityService = rule.getProcessEngine().getIdentityService();

        identityService.deleteUser("sampleuser");
        identityService.deleteUser("demo");
        identityService.deleteGroup("sample");
    }

    @Test
    public void testClaim() throws Exception {
        ProcessInstance processInstance = newInstance();
        claimTest(processInstance);
    }

    @Test
    public void testComplete() throws Exception {
        ProcessInstance processInstance = newInstance();
        claimTest(processInstance);
        completeTest(processInstance);
    }

    @Test
    public void testHappyPath() throws Exception{
        ProcessInstance processInstance = newInstance();
        claimTest(processInstance);
        completeTest(processInstance);
        finishTest(processInstance);
    }

    private void claimTest(ProcessInstance processInstance) throws Exception {
        assertThat(processInstance).isWaitingAt("TaskForDemoTask").task().hasCandidateUser("demo");

        mockMvc.perform(post("/custom/task/"+task().getId()+"/claim")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(demoUserIdDto)))
                .andExpect(status().isOk());

        assertThat(processInstance).task().isAssignedTo("demo");
    }

    private void completeTest(ProcessInstance processInstance) throws Exception {
        assertThat(processInstance).isWaitingAt("TaskForDemoTask").task().isAssignedTo("demo");

        ResultActions resultActions = mockMvc.perform(post("/custom/task/" + task().getId() + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(demoCustomCompleteTaskDto)));

        assertCompleteCall(resultActions);

        assertThat(processInstance).isWaitingAt("TaskForSampleGroupTask");
    }

    private void finishTest(ProcessInstance processInstance) throws Exception {
        assertThat(processInstance).task().hasCandidateGroup("sample");

        mockMvc.perform(post("/custom/task/"+task().getId()+"/claim")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(sampleUserDto)))
                .andExpect(status().isOk());

        assertThat(processInstance).task().isAssignedTo("sampleuser");

        ResultActions resultActions = mockMvc.perform(post("/custom/task/"+task().getId()+"/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(sampleUserCustomCompleteTaskDto)))
                .andExpect(status().isOk());

        assertCompleteCall(resultActions);

        assertThat(processInstance).isWaitingAt("TaskForSampleUser");

        assertThat(processInstance).task().hasCandidateUser("sampleuser");

        mockMvc.perform(post("/custom/task/"+task().getId()+"/claim")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(sampleUserDto)))
                .andExpect(status().isOk());

        assertThat(processInstance).task().isAssignedTo("sampleuser");

        resultActions = mockMvc.perform(post("/custom/task/" + task().getId() + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(sampleUserCustomCompleteTaskDto)));

        assertCompleteCall(resultActions);

        assertThat(processInstance).isEnded();
    }

    @Test
    public void testClaimNoPermission() throws Exception {
        ProcessInstance processInstance = newInstance();
        assertThat(processInstance).isWaitingAt("TaskForDemoTask").task().hasCandidateUser("demo");

        MockHttpServletResponse response = mockMvc.perform(post("/custom/task/" + task().getId() + "/claim")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(sampleUserDto)))
                .andExpect(status().isForbidden()).andReturn().getResponse();

        String expectedMessage = String.format("Cannot claim task %s: no authorizations found for the user %s", task().getId(),
                sampleUserDto.getUserId());

        Assertions.assertThat(response.getErrorMessage()).isNotNull();
        Assertions.assertThat(response.getErrorMessage()).isEqualTo(expectedMessage);

        assertThat(processInstance).isWaitingAt("TaskForDemoTask");
    }

    @Test
    public void testCompleteNoPermission() throws Exception {
        ProcessInstance processInstance = newInstance();
        assertThat(processInstance).isWaitingAt("TaskForDemoTask").task().hasCandidateUser("demo");

        MockHttpServletResponse response = mockMvc.perform(post("/custom/task/" + task().getId() + "/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(sampleUserCustomCompleteTaskDto)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();

        String expectedMessage = String.format("Cannot complete task %s: no authorizations found for the user %s", task().getId(),
                sampleUserCustomCompleteTaskDto.getUserIdDto().getUserId());

        Assertions.assertThat(response.getErrorMessage()).isNotNull();
        Assertions.assertThat(response.getErrorMessage()).isEqualTo(expectedMessage);

        assertThat(processInstance).isWaitingAt("TaskForDemoTask");
    }

    public ProcessInstance newInstance(){
        VariableMap variables = Variables.createVariables().putValue("someVar", "someVal");
        return runtimeService().startProcessInstanceByKey("UserTaskDemoProcess", variables);
    }

    public void assertCompleteCall(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.someVar", Matchers.notNullValue()))
                .andExpect(jsonPath("$.someVar.type", Matchers.equalTo("String")))
                .andExpect(jsonPath("$.someVar.value", Matchers.equalTo("someVal")));
    }
}