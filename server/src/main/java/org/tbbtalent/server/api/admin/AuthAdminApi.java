package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.security.auth.login.AccountLockedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.db.CaptchaService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/auth")
public class AuthAdminApi {

    private final UserService userService;
    private final CaptchaService captchaService;

    @Autowired
    public AuthAdminApi(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody LoginRequest request)
            throws AccountLockedException, PasswordExpiredException, InvalidCredentialsException,
            InvalidPasswordFormatException {

        //Do check for automated logins. Throws exception if it looks
        //automated.
        captchaService.processCaptchaV3Token(request.getReCaptchaV3Token(), "login");

        JwtAuthenticationResponse response = this.userService.login(request);
        return jwtDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity logout() {
        this.userService.logout();
        return ResponseEntity.ok().build();
    }


    DtoBuilder jwtDto() {
        return new DtoBuilder()
                .add("accessToken")
                .add("tokenType")
                .add("user", userBriefDto())
                ;
    }

    private DtoBuilder userBriefDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("email")
                .add("role")
                .add("readOnly")
                ;
    }
}
