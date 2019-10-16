package org.tbbtalent.server.request.work.experience;

import org.tbbtalent.server.request.SearchRequest;

import javax.validation.constraints.NotNull;

public class SearchJobExperienceRequest extends SearchRequest {

    @NotNull
    private Long candidateOccupationId;

    public Long getCandidateOccupationId() {
        return candidateOccupationId;
    }

    public void setCandidateOccupationId(Long candidateOccupationId) {
        this.candidateOccupationId = candidateOccupationId;
    }
}

