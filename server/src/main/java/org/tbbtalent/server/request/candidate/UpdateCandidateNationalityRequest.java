package org.tbbtalent.server.request.candidate;

public class UpdateCandidateNationalityRequest {

    private String nationality;
    private Boolean registeredWithUN;
    private String registrationId;

    public String getNationality() { return nationality; }

    public void setNationality(String nationality) {  this.nationality = nationality; }

    public Boolean getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(Boolean registeredWithUN) { this.registeredWithUN = registeredWithUN; }

    public String getRegistrationId() { return registrationId; }

    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
}
