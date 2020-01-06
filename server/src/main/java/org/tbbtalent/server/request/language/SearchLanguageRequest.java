package org.tbbtalent.server.request.language;

import org.springframework.data.domain.Sort;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.SearchRequest;

public class SearchLanguageRequest extends SearchRequest {

    private String keyword;

    private Status status;

    public String language;

    public SearchLanguageRequest() {
        super(Sort.Direction.ASC, new String[]{"name"});
    }

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

