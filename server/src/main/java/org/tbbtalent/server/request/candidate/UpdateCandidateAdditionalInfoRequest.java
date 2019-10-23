package org.tbbtalent.server.request.candidate;

public class UpdateCandidateAdditionalInfoRequest {

    private String additionalInfo;
    private Boolean submit;

    public String getAdditionalInfo() { return additionalInfo; }

    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public Boolean getSubmit() {
        return submit;
    }

    public void setSubmit(Boolean submit) {
        this.submit = submit;
    }
}
