package org.tbbtalent.server.request.search;

import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.SearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchSavedSearchRequest extends SearchRequest {

    private String keyword;
    private Boolean fixed;
    private Boolean owned;
    private Boolean shared;
    private SavedSearchType savedSearchType;
    private SavedSearchSubtype savedSearchSubtype;
    
}

