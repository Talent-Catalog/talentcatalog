package org.tbbtalent.server.model;

public class Candidate {

    private Long id;
    private String candidateNumber;
    private String firstName;
    private String lastName;

    public Candidate() {
    }

    public Candidate(Long id, String candidateNumber, String firstName, String lastName) {
        this.id = id;
        this.candidateNumber = candidateNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
