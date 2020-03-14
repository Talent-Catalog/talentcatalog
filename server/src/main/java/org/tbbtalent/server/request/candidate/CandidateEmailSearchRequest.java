package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class CandidateEmailSearchRequest extends SearchRequest {

    private String candidateEmail;

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }
}
