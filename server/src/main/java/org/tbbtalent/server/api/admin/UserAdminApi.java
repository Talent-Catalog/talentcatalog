/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.user.*;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.UserService;
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
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        if (loggedInUser.getRole() == Role.admin) {
            return userDto().build(user);
        } else if (loggedInUser.getRole() == Role.sourcepartneradmin) {
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

    @PutMapping("/shared-add/{id}")
    public Map<String, Object> addToSharedWithMe(
            @PathVariable("id") long id,
        @RequestBody UpdateSharingRequest request) {
        User user = this.userService.addToSharedWithUser(id, request);
        return userDto().build(user);
    }

    @PutMapping("/shared-remove/{id}")
    public Map<String, Object> removeFromSharedWithMe(
            @PathVariable("id") long id,
        @RequestBody UpdateSharingRequest request) {
        User user = this.userService.removeFromSharedWithUser(id, request);
        return userDto().build(user);
    }

    @PutMapping("/password/{id}")
    public void updatePassword(@PathVariable("id") long id,
                                              @RequestBody UpdateUserPasswordRequest request) {
        this.userService.updateUserPassword(id, request);
    }

    @PutMapping("/mfa-reset/{id}")
    public void mfaReset(@PathVariable("id") long id) {
        this.userService.mfaReset(id);
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
                .add("sourceCountries", countryDto())
                .add("readOnly")
                .add("status")
                .add("createdDate")
                .add("createdBy")
                .add("lastLogin")
                .add("usingMfa")
                .add("mfaConfigured")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder userDtoSemiLimited() {
        return new DtoBuilder()
                .add("id")
                .add("role")
                .add("status")
                .add("createdDate")
                .add("lastLogin")
                .add("usingMfa")
                .add("mfaConfigured")
                ;
    }

}
