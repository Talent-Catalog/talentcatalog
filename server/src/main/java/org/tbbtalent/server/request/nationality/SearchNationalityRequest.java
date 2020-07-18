package org.tbbtalent.server.request.nationality;

import org.springframework.data.domain.Sort;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.request.PagedSearchRequest;

public class SearchNationalityRequest extends PagedSearchRequest {

    private String keyword;

    private Status status;

    private String language;

    public SearchNationalityRequest() {
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

