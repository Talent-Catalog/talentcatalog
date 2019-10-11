package org.tbbtalent.server.request.candidate.certification;

import java.time.LocalDate;

public class UpdateCandidateCertificationRequest {

    private String name;
    private String institution;
    private LocalDate dateCompleted;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getInstitution() { return institution; }

    public void setInstitution(String institution) { this.institution = institution; }

    public LocalDate getDateCompleted() { return dateCompleted; }

    public void setDateCompleted(LocalDate dateCompleted) { this.dateCompleted = dateCompleted; }
}
