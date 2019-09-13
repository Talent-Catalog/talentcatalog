package org.tbbtalent.server.request.candidate;

public class UpdateCandidateNationalityRequest {

    private String nationality;
    private String registeredWithUN;
    private String registrationId;

    public String getNationality() { return nationality; }

    public void setNationality(String nationality) {  this.nationality = nationality; }

    public String getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(String registeredWithUN) { this.registeredWithUN = registeredWithUN; }

    public String getRegistrationId() { return registrationId; }

    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
}
