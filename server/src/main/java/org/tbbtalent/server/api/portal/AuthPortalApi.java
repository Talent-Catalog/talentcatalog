package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.security.auth.login.AccountLockedException;
import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/auth")
public class AuthPortalApi {

    private final CandidateService candidateService;

    @Autowired
    public AuthPortalApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody LoginRequest request)
            throws AccountLockedException, PasswordExpiredException, InvalidCredentialsException,
            InvalidPasswordFormatException, CandidateDeactivatedException {
        JwtAuthenticationResponse response = this.candidateService.login(request);
        return jwtDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity logout() {
        this.candidateService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("register")
    public Map<String, Object> get(@Valid @RequestBody RegisterCandidateRequest request) throws NoSuchObjectException, AccountLockedException {
        JwtAuthenticationResponse jwt = candidateService.register(request);
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
                .add("email")
                .add("phone")
                .add("whatsapp")
                ;
    }
}
