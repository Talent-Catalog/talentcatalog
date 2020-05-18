package org.tbbtalent.server.request.candidate.source;

import org.tbbtalent.server.request.SearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchCandidateSourceRequestPaged extends SearchRequest {
    private String keyword;
    private Boolean fixed;
    private Boolean owned;
    private Boolean shared;
}

