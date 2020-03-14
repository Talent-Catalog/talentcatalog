package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class CandidateNumberOrNameSearchRequest extends SearchRequest {

    private String candidateNumberOrName;

    public String getCandidateNumberOrName() {
        return candidateNumberOrName;
    }

    public void setCandidateNumberOrName(String candidateNumberOrName) {
        this.candidateNumberOrName = candidateNumberOrName;
    }
}
