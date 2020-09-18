package com.camunda.consulting.loader;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserLoader implements CommandLineRunner {

    private final IdentityService identityService;

    public UserLoader(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(shouldLoadUsers()){
            loadUsers();
        }
    }

    public Boolean shouldLoadUsers(){
        return identityService.createUserQuery().count() == 1;
    }

    private void loadUsers(){
        User user = new UserEntity();
        user.setId("sampleuser");
        user.setFirstName("Sample");
        user.setLastName("User");
        user.setPassword("demo");

        Group group = new GroupEntity();
        group.setId("sample");
        group.setName("sample");

        identityService.saveUser(user);
        identityService.saveGroup(group);

        identityService.createMembership(user.getId(), group.getId());
    }
}
