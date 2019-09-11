package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class SearchCandidateRequest extends SearchRequest {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

