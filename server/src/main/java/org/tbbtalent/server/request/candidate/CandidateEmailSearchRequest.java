package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.PagedSearchRequest;

public class CandidateEmailSearchRequest extends PagedSearchRequest {

    private String candidateEmail;

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }
}
