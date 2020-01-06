package org.tbbtalent.server.request.attachment;

import org.tbbtalent.server.request.SearchRequest;

import javax.validation.constraints.NotNull;

public class SearchCandidateAttachmentsRequest extends SearchRequest {

    @NotNull
    private Long candidateId;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}

