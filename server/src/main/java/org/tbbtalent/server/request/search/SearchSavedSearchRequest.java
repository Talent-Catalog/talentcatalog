package org.tbbtalent.server.request.search;

import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.SearchRequest;

public class SearchSavedSearchRequest extends SearchRequest {

    private String keyword;
    private Boolean fixed;
    private Boolean owned;
    private Boolean shared;
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

    public Boolean getFixed() {
        return fixed;
    }

    public void setFixed(Boolean fixed) {
        this.fixed = fixed;
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }
}

