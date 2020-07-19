package org.tbbtalent.server.request.candidate;

import java.time.LocalDate;

import org.tbbtalent.server.model.db.Gender;

public class UpdateCandidatePersonalRequest {

    /* PERSONAL */
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dob;
    /* LOCATION */
    private Long countryId;
    private String city;
    private Integer yearOfArrival;
    /* NATIONALITY */
    private Long nationality;
    private Boolean registeredWithUN;
    private String registrationId;

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

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getYearOfArrival() {
        return yearOfArrival;
    }

    public void setYearOfArrival(Integer yearOfArrival) {
        this.yearOfArrival = yearOfArrival;
    }

    public Long getNationality() {
        return nationality;
    }

    public void setNationality(Long nationality) {
        this.nationality = nationality;
    }

    public Boolean getRegisteredWithUN() {
        return registeredWithUN;
    }

    public void setRegisteredWithUN(Boolean registeredWithUN) {
        this.registeredWithUN = registeredWithUN;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }
}
