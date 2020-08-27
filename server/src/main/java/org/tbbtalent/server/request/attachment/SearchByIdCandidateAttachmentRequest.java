package org.tbbtalent.server.request.attachment;

import javax.validation.constraints.NotNull;

public class SearchByIdCandidateAttachmentRequest {

    @NotNull
    private Long candidateId;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

}
