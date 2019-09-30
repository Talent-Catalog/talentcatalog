package org.tbbtalent.server.request.user;

public class CheckPasswordResetTokenRequest {

    public String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
