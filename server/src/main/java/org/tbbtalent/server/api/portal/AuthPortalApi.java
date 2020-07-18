package org.tbbtalent.server.api.portal;

import java.util.Map;

import javax.security.auth.login.AccountLockedException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.exception.UserDeactivatedException;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/auth")
public class AuthPortalApi {

    private final UserService userService;
    private final CandidateService candidateService;

    @Autowired
    public AuthPortalApi(UserService userService,
                         CandidateService candidateService) {
        this.userService = userService;
        this.candidateService = candidateService;
    }

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody LoginRequest request)
            throws AccountLockedException, PasswordExpiredException, InvalidCredentialsException,
            InvalidPasswordFormatException, UserDeactivatedException {
        JwtAuthenticationResponse response = userService.login(request);
        return jwtDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity logout() {
        this.userService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("register")
    public Map<String, Object> register(@Valid @RequestBody RegisterCandidateRequest request) throws NoSuchObjectException, AccountLockedException {
        LoginRequest loginRequest = candidateService.register(request);
        JwtAuthenticationResponse jwt = userService.login(loginRequest);
        return jwtDto().build(jwt);
    }

    DtoBuilder jwtDto() {
        return new DtoBuilder()
                .add("accessToken")
                .add("tokenType")
                .add("user", candidateBriefDto())
                ;
    }

    private DtoBuilder candidateBriefDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("email")
                ;
    }
}
