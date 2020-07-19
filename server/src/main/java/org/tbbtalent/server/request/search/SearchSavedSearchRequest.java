package org.tbbtalent.server.request.search;

import org.tbbtalent.server.model.db.SavedSearchSubtype;
import org.tbbtalent.server.model.db.SavedSearchType;
import org.tbbtalent.server.request.candidate.source.SearchCandidateSourceRequestPaged;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchSavedSearchRequest extends SearchCandidateSourceRequestPaged {
    private SavedSearchType savedSearchType;
    private SavedSearchSubtype savedSearchSubtype;
}

