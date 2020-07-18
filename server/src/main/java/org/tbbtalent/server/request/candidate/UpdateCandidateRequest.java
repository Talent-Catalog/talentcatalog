package org.tbbtalent.server.request.candidate;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.db.Gender;

public class UpdateCandidateRequest extends BaseCandidateContactRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotNull
    private Gender gender;
    @NotNull
    private Long nationalityId;
    @NotNull
    private Long countryId;
    private Integer yearOfArrival;
    private String address1;
    private String city;
    private LocalDate dob;
    private Boolean unRegistered;
    private String unRegistrationNumber;

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Long getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(Long nationalityId) {
        this.nationalityId = nationalityId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Integer getYearOfArrival() {
        return yearOfArrival;
    }

    public void setYearOfArrival(Integer yearOfArrival) {
        this.yearOfArrival = yearOfArrival;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Boolean getUnRegistered() {
        return unRegistered;
    }

    public void setUnRegistered(Boolean unRegistered) {
        this.unRegistered = unRegistered;
    }

    public String getUnRegistrationNumber() {
        return unRegistrationNumber;
    }

    public void setUnRegistrationNumber(String unRegistrationNumber) {
        this.unRegistrationNumber = unRegistrationNumber;
    }
}
