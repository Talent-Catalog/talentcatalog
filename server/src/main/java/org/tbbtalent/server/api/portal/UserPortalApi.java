package org.tbbtalent.server.api.portal;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.ExpiredTokenException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.InvalidPasswordTokenException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tbbtalent.server.request.user.ResetPasswordRequest;
import org.tbbtalent.server.request.user.SendResetPasswordEmailRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;


@RestController
@RequestMapping("/api/portal/user")
public class UserPortalApi {

    private UserService userService;

    @Autowired
    public UserPortalApi(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public Map<String, Object> getMyUser() {
        User user = userService.getMyUser();
        return userBriefDto().build(user);
    }

    @PostMapping("password")
    public void updatePassword(@Valid @RequestBody UpdateUserPasswordRequest request)  {
        userService.updatePassword(request);
    }

    @PostMapping(value="reset-password-email")
    public void sendResetPasswordEmail(@RequestBody SendResetPasswordEmailRequest request) throws NoSuchObjectException {
        userService.generateResetPasswordToken(request);
    }

    @PostMapping(value="check-token")
    public void checkResetTokenValidity(@RequestBody CheckPasswordResetTokenRequest request) throws ExpiredTokenException, InvalidPasswordTokenException {
        userService.checkResetToken(request);
    }

    @PostMapping(value="reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) throws InvalidPasswordFormatException {
        userService.resetPassword(request);
    }

    private DtoBuilder userBriefDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("email")
                .add("firstName")
                .add("lastName")
                ;
    }




}
