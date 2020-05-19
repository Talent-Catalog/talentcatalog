package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.PagedSearchRequest;

public class CandidateNumberOrNameSearchRequest extends PagedSearchRequest {

    private String candidateNumberOrName;

    public String getCandidateNumberOrName() {
        return candidateNumberOrName;
    }

    public void setCandidateNumberOrName(String candidateNumberOrName) {
        this.candidateNumberOrName = candidateNumberOrName;
    }
}
