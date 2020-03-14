package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class CandidatePhoneSearchRequest extends SearchRequest {

    private String candidatePhone;

    public String getCandidatePhone() {
        return candidatePhone;
    }

    public void setCandidatePhone(String candidatePhone) {
        this.candidatePhone = candidatePhone;
    }
}
