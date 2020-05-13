package org.tbbtalent.server.request.search;

import org.tbbtalent.server.model.SavedSearchSubtype;
import org.tbbtalent.server.model.SavedSearchType;
import org.tbbtalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateSavedSearchRequest extends AbstractUpdateCandidateSourceRequest {

    private Boolean reviewable;
    private SavedSearchType savedSearchType;
    private SavedSearchSubtype savedSearchSubtype;
    private SearchCandidateRequest searchCandidateRequest;

}
