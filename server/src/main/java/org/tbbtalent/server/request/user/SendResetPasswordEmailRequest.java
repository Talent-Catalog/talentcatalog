package org.tbbtalent.server.request.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SendResetPasswordEmailRequest {
    private String email;
    private String reCaptchaV3Token;
}
