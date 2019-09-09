package org.tbbtalent.server.request.candidate;

public class UpdateCandidateAdditionalContactRequest {

    private String phone;
    private String whatsapp;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}
