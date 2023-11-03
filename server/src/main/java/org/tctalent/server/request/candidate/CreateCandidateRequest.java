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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateCandidateRequest extends BaseCandidateContactRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String username;
    @NotNull
    private Boolean contactConsentRegistration;
    @NotNull
    private Boolean contactConsentPartners;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getContactConsentRegistration() {
        return contactConsentRegistration;
    }

    public void setContactConsentRegistration(Boolean contactConsentRegistration) {
        this.contactConsentRegistration = contactConsentRegistration;
    }

    public Boolean getContactConsentPartners() {
        return contactConsentPartners;
    }

    public void setContactConsentPartners(Boolean contactConsentPartners) {
        this.contactConsentPartners = contactConsentPartners;
    }
}
