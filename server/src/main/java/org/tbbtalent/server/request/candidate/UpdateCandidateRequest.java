/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.UnhcrStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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
    private String state;
    private LocalDate dob;
    private UnhcrStatus unhcrStatus;
    private String unhcrNumber;

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

    public String getState() {return state;}

    public void setState(String state) {this.state = state;}

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public UnhcrStatus getUnhcrStatus() {
        return unhcrStatus;
    }

    public void setUnhcrStatus(UnhcrStatus unhcrStatus) {
        this.unhcrStatus = unhcrStatus;
    }

    public String getUnhcrNumber() {
        return unhcrNumber;
    }

    public void setUnhcrNumber(String unhcrNumber) {
        this.unhcrNumber = unhcrNumber;
    }
}
