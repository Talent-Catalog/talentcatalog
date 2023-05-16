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

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
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
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.user.SearchUserRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.request.user.UpdateUserRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/user")
public class UserAdminApi {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public UserAdminApi(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("search")
    public List<Map<String, Object>> search(@RequestBody SearchUserRequest request) {
        List<User> users = userService.search(request);
        return userDto().buildList(users);
    }

    @PostMapping("search-paged")
    public Map<String, Object> searchPaged(@RequestBody SearchUserRequest request) {
        Page<User> users = this.userService.searchPaged(request);
        return userDto().buildPage(users);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        User user = this.userService.getUser(id);
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        switch (loggedInUser.getRole()) {
            case systemadmin:
            case admin:
            case sourcepartneradmin:
                return userDto().build(user);
            default:
                return userDtoSemiLimited().build(user);
        }

    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody UpdateUserRequest request) throws UsernameTakenException {
        User user = this.userService.createUser(request);
        return userDto().build(user);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                            @RequestBody UpdateUserRequest request) {
        User user = this.userService.updateUser(id, request);
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
                .add("approver", userDtoApprover())
                .add("purpose")
                .add("sourceCountries", countryDto())
                .add("readOnly")
                .add("status")
                .add("createdDate")
                .add("createdBy", userDtoSemiLimited())
                .add("lastLogin")
                .add("usingMfa")
                .add("mfaConfigured")
                .add("partner", partnerDto())
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("abbreviation")
                .add("websiteUrl")
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

    /**
     * For providing limited details of a user's approver (who is another user with admin access)
     */
    private DtoBuilder userDtoApprover() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }

}
