package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.Gender;

import java.time.LocalDate;

public class UpdateCandidatePersonalRequest {

    private String firstName;
    private String lastName;
    private Gender gender;
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

    public Gender getGender() { return gender; }

    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
}
