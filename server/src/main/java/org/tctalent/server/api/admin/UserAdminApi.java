/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.ExpiredTokenException;
import org.tctalent.server.exception.InvalidPasswordFormatException;
import org.tctalent.server.exception.InvalidPasswordTokenException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ReCaptchaInvalidException;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.PartnerDtoHelper;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tctalent.server.request.user.ResetPasswordRequest;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.request.user.SendResetPasswordEmailRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminApi {

    private final UserService userService;
    private final AuthService authService;
    private final CountryService countryService;

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

        return switch (loggedInUser.getRole()) {
            case systemadmin, admin, partneradmin -> userDto().build(user);
            default -> userDtoSemiLimited().build(user);
        };

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

    @PostMapping(value="reset-password-email")
    public void sendResetPasswordEmail(
        @RequestBody SendResetPasswordEmailRequest request)
        throws NoSuchObjectException, ReCaptchaInvalidException {

        userService.generateResetPasswordToken(request);
    }

    @PostMapping(value="check-token")
    public void checkResetTokenValidity(@RequestBody CheckPasswordResetTokenRequest request)
        throws ExpiredTokenException, InvalidPasswordTokenException {

        userService.checkResetToken(request);
    }

    @PostMapping(value="reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) throws
        InvalidPasswordFormatException {

        userService.resetPassword(request);
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
                .add("jobCreator")
                .add("approver", userDtoApprover())
                .add("purpose")
                .add("sourceCountries", countryService.selectBuilder())
                .add("readOnly")
                .add("status")
                .add("createdDate")
                .add("createdBy", userDtoSemiLimited())
                .add("lastLogin")
                .add("usingMfa")
                .add("mfaConfigured")
                .add("partner", PartnerDtoHelper.getPartnerDto())
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
