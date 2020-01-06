package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotBlank;

public class CreateCandidateRequest extends BaseCandidateContactRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String username;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
