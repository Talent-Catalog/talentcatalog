package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.user.CreateUserRequest;
import org.tbbtalent.server.request.user.SearchUserRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.request.user.UpdateUserRequest;
import org.tbbtalent.server.request.user.UpdateUsernameRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/user")
public class UserAdminApi {

    private final UserService userService;
    private final UserContext userContext;

    @Autowired
    public UserAdminApi(UserService userService, UserContext userContext) {
        this.userService = userService;
        this.userContext = userContext;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchUserRequest request) {
        Page<User> users = this.userService.searchUsers(request);
        return userDto().buildPage(users);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        User user = this.userService.getUser(id);
        User loggedInUser = userContext.getLoggedInUser();
        if (loggedInUser.getRole() == Role.admin) {
            return userDto().build(user);
        } else {
            return userDtoSemiLimited().build(user);
        }

    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateUserRequest request) throws UsernameTakenException {
        User user = this.userService.createUser(request);
        return userDto().build(user);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                            @RequestBody UpdateUserRequest request) {
        User user = this.userService.updateUser(id, request);
        return userDto().build(user);
    }

    @PutMapping("/username/{id}")
    public Map<String, Object> updateUsername(@PathVariable("id") long id,
                                      @RequestBody UpdateUsernameRequest request) {
        User user = this.userService.updateUsername(id, request);
        return userDto().build(user);
    }

    @PutMapping("/password/{id}")
    public void updatePassword(@PathVariable("id") long id,
                                              @RequestBody UpdateUserPasswordRequest request) {
        this.userService.updateUserPassword(id, request);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") long id) {
        this.userService.deleteUser(id);
    }


    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("firstName")
                .add("lastName")
                .add("email")
                .add("role")
                .add("readOnly")
                .add("status")
                .add("createdDate")
                .add("lastLogin")
                ;
    }

    private DtoBuilder userDtoSemiLimited() {
        return new DtoBuilder()
                .add("id")
                .add("role")
                .add("status")
                .add("createdDate")
                .add("lastLogin")
                ;
    }

}
