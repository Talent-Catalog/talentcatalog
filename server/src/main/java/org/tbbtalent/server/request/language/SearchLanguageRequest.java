package org.tbbtalent.server.request.language;

import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.SearchRequest;

public class SearchLanguageRequest extends SearchRequest {

    private String keyword;

    private Status status;

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
}

