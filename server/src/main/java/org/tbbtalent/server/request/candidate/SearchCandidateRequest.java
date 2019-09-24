package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class SearchCandidateRequest extends SearchRequest {

    private String keyword;
    private String status;
    private Boolean registeredWithUN;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Boolean getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(Boolean registeredWithUN) { this.registeredWithUN = registeredWithUN; }
}

