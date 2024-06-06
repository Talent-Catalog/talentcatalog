/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.request.candidate;

import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

public class UpdateCandidatePersonalRequest {

    /* PERSONAL */
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dob;
    /* LOCATION */
    private Long countryId;
    private String city;
    private String state;
    private Integer yearOfArrival;
    /* NATIONALITY */
    private Long nationalityId;
    /* EXTERNAL ID */
    private String externalId;
    private String externalIdSource;
    /* UNHCR */
    @Enumerated(EnumType.STRING)
    private YesNoUnsure unhcrRegistered;
    private String unhcrNumber;
    @Enumerated(EnumType.STRING)
    private YesNo unhcrConsent;

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

    public String getState() {return state;}

    public void setState(String state) {this.state = state;}

    public Integer getYearOfArrival() {
        return yearOfArrival;
    }

    public void setYearOfArrival(Integer yearOfArrival) {
        this.yearOfArrival = yearOfArrival;
    }

    public Long getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(Long nationalityId) {
        this.nationalityId = nationalityId;
    }

    public String getExternalId() {return externalId;}

    public void setExternalId(String externalId) {this.externalId = externalId;}

    public String getExternalIdSource() {return externalIdSource;}

    public void setExternalIdSource(String externalIdSource) {this.externalIdSource = externalIdSource;}

    public YesNoUnsure getUnhcrRegistered() {return unhcrRegistered;}

    public void setUnhcrRegistered(YesNoUnsure unhcrRegistered) {this.unhcrRegistered = unhcrRegistered;}

    public String getUnhcrNumber() {return unhcrNumber;}

    public void setUnhcrNumber(String unhcrNumber) {this.unhcrNumber = unhcrNumber;}

    public YesNo getUnhcrConsent() {return unhcrConsent;}

    public void setUnhcrConsent(YesNo unhcrConsent) {this.unhcrConsent = unhcrConsent;}
}
