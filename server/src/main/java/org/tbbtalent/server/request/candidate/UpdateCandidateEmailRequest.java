package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotBlank;

public class UpdateCandidateEmailRequest {

    @NotBlank(message = "Email must not be empty")
    private String email;

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}
