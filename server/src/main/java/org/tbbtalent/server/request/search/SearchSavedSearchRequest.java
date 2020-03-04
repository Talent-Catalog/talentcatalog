package org.tbbtalent.server.request.search;

import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.SearchRequest;

public class SearchSavedSearchRequest extends SearchRequest {

    private String keyword;
    private SavedSearchType savedSearchType;
    private SavedSearchSubtype savedSearchSubtype;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SavedSearchType getSavedSearchType() {
        return savedSearchType;
    }

    public void setSavedSearchType(SavedSearchType savedSearchType) {
        this.savedSearchType = savedSearchType;
    }

    public SavedSearchSubtype getSavedSearchSubtype() {
        return savedSearchSubtype;
    }

    public void setSavedSearchSubtype(SavedSearchSubtype savedSearchSubtype) {
        this.savedSearchSubtype = savedSearchSubtype;
    }
}

