package org.tbbtalent.server.service.impl;

import org.aspectj.lang.annotation.Before;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbbtalent.server.api.admin.SavedListAdminApi;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.UserRepository;
import org.tbbtalent.server.request.user.CreateUserRequest;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.SavedListService;
import org.tbbtalent.server.service.UserService;


@SpringBootTest
class UserServiceImplTest {

    private User user;

    @Mock
    private PasswordHelper passwordHelper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserAndCountries(){
        assertNotNull(userService);
        assertNotNull(userRepository);
        User user = new User(
                "username", "first", "last",
                "email@test.com", Role.admin);

        when(userRepository.save(user)).thenReturn(user);
        userRepository.save(user);
        assertNotNull(user);
    }

    @Test
    void testCreateUserNoSourceCountries(){
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("first");
        request.setLastName("last");
        request.setUsername("username2");
        request.setEmail("email2@test.com");
        request.setRole(Role.admin);
        request.setPassword("xxxxxxxxxx");


        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User testUser = userService.createUser(request);
        assertNotNull(testUser);
        assertThat(testUser.getSourceCountries()).isNull();
    }

}
