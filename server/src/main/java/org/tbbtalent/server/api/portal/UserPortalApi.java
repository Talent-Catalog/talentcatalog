package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.ExpiredTokenException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.InvalidPasswordTokenException;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tbbtalent.server.request.user.ResetPasswordRequest;
import org.tbbtalent.server.request.user.SendResetPasswordEmailRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.service.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;


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
    public void sendResetPasswordEmail(@RequestBody SendResetPasswordEmailRequest request) {
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
