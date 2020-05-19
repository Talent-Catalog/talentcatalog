package org.tbbtalent.server.request.occupation;

import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.PagedSearchRequest;

public class SearchOccupationRequest extends PagedSearchRequest {

    private String keyword;

    private Status status;

    private String language;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

