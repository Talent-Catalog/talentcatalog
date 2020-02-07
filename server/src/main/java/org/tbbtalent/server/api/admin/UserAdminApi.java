package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.user.*;
import org.tbbtalent.server.service.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/user")
public class UserAdminApi {

    private final UserService userService;

    @Autowired
    public UserAdminApi(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchUserRequest request) {
        Page<User> users = this.userService.searchUsers(request);
        return userDto().buildPage(users);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        User user = this.userService.getUser(id);
        return userDto().build(user);
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
                .add("status")
                .add("createdDate")
                .add("lastLogin")
                ;
    }

}
