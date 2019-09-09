package org.tbbtalent.server.response;

import org.tbbtalent.server.model.Candidate;

public class JwtAuthenticationResponse {

    private Candidate user;
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtAuthenticationResponse(String accessToken, Candidate user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Candidate getUser() {
        return user;
    }

    public void setUser(Candidate user) {
        this.user = user;
    }
}
