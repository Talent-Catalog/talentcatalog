package org.tbbtalent.server.request.note;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.request.PagedSearchRequest;

public class SearchCandidateNotesRequest extends PagedSearchRequest {

    @NotNull
    private Long candidateId;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}

