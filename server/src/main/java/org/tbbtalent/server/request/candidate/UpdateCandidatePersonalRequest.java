package org.tbbtalent.server.request.candidate;

import java.time.LocalDate;

public class UpdateCandidatePersonalRequest {

    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dob;

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

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
}
