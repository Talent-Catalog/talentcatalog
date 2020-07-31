package org.tbbtalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterCandidateRequest extends BaseCandidateContactRequest {
    private String username;
    private String password;
    private String passwordConfirmation;
    private String reCaptchaV3Token;
}
