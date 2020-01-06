package org.tbbtalent.server.request.search;

import org.tbbtalent.server.request.SearchRequest;

public class SearchSavedSearchRequest extends SearchRequest {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

