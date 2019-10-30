package org.tbbtalent.server.request.user;

import javax.validation.constraints.NotBlank;

public class UpdateUsernameRequest {

    @NotBlank
    private String username;

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

}
