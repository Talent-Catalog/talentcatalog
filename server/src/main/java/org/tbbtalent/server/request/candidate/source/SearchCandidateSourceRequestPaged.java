package org.tbbtalent.server.request.candidate.source;

import org.tbbtalent.server.request.PagedSearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchCandidateSourceRequestPaged extends PagedSearchRequest {
    private String keyword;
    private Boolean fixed;
    private Boolean global;
    private Boolean owned;
    private Boolean shared;
    private Boolean watched;
}

