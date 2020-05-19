package org.tbbtalent.server.request.skill;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.request.PagedSearchRequest;

public class SearchCandidateSkillRequest extends PagedSearchRequest {

    @NotNull
    private Long candidateId;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}

