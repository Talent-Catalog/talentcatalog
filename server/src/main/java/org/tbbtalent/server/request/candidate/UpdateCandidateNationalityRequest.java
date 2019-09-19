package org.tbbtalent.server.request.candidate;

public class UpdateCandidateNationalityRequest {

    private Long nationality;
    private Boolean registeredWithUN;
    private String registrationId;

    public Long getNationality() { return nationality; }

    public void setNationality(Long nationality) {  this.nationality = nationality; }

    public Boolean getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(Boolean registeredWithUN) { this.registeredWithUN = registeredWithUN; }

    public String getRegistrationId() { return registrationId; }

    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
}
