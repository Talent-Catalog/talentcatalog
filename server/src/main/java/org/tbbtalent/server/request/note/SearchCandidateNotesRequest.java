package org.tbbtalent.server.request.note;

import org.tbbtalent.server.request.SearchRequest;

import javax.validation.constraints.NotNull;

public class SearchCandidateNotesRequest extends SearchRequest {

    @NotNull
    private Long candidateId;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}

