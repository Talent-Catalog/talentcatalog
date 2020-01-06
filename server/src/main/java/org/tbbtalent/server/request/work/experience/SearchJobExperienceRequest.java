package org.tbbtalent.server.request.work.experience;

import org.tbbtalent.server.request.SearchRequest;

import javax.validation.constraints.NotNull;

public class SearchJobExperienceRequest extends SearchRequest {

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

