package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.PagedSearchRequest;

public class CandidatePhoneSearchRequest extends PagedSearchRequest {

    private String candidatePhone;

    public String getCandidatePhone() {
        return candidatePhone;
    }

    public void setCandidatePhone(String candidatePhone) {
        this.candidatePhone = candidatePhone;
    }
}
