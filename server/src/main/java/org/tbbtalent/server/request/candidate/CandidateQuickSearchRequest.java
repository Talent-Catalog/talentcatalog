package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class CandidateQuickSearchRequest extends SearchRequest {

    private String candidateNumber;

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }
}
