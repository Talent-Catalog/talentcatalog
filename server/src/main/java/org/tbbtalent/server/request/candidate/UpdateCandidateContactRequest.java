package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.Gender;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateCandidateContactRequest {

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
    private String phone;
    private String whatsapp;
    private String email;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
