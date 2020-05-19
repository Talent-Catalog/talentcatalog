package org.tbbtalent.server.request.work.experience;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.request.PagedSearchRequest;

public class SearchJobExperienceRequest extends PagedSearchRequest {

    @NotNull
    private Long candidateOccupationId;

    private Long candidateId;

    public Long getCandidateOccupationId() {
        return candidateOccupationId;
    }

    public void setCandidateOccupationId(Long candidateOccupationId) {
        this.candidateOccupationId = candidateOccupationId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}

