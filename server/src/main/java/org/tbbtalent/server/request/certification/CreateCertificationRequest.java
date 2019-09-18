package org.tbbtalent.server.request.certification;

public class CreateCertificationRequest {

    private String name;
    private String institution;
    private String dateCompleted;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getInstitution() { return institution; }

    public void setInstitution(String institution) { this.institution = institution; }

    public String getDateCompleted() { return dateCompleted; }

    public void setDateCompleted(String dateCompleted) { this.dateCompleted = dateCompleted; }
}
