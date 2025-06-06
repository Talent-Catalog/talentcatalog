/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import static org.tctalent.server.data.CandidateTestData.getCandidate;
import static org.tctalent.server.data.CountryTestData.getSourceCountrySet;
import static org.tctalent.server.data.PartnerImplTestData.getDefaultPartner;
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.UpdateUserRequest;

public class UserTestData {

    private static final User auditUser =
        new User("audit_user",
            "test",
            "user",
            "audit.user@ngo.org",
            Role.admin);

    public static User getAuditUser() {
        return auditUser;
    }

    public static User getAdminUser() {
        User u = new User(
            "admin_user",
            "admin",
            "user",
            "admin.user@ngo.org",
            Role.admin
        );
        u.setId(555L);
        u.setStatus(Status.active);
        u.setApprover(auditUser);
        u.setPurpose("Complete intakes");
        u.setSourceCountries(getSourceCountrySet());
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(auditUser);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getSourcePartner());
        return u;
    }

    public static User getSystemAdminUser() {
        User u = new User("system_admin",
            "system",
            "admin",
            "systemadmin@ngo2.org",
            Role.systemadmin);
        u.setId(57L);
        u.setSourceCountries(Collections.emptySet()); // Unrestricted
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(auditUser);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getDefaultPartner());
        return u;
    }

    public static User getLimitedUser() {
        User u = new User("limited_user",
            "limited",
            "user",
            "limited@ngo3.org",
            Role.limited);
        u.setId(58L);
        u.setSourceCountries(Collections.emptySet());
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(auditUser);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getSourcePartner());
        return u;
    }

    public static User getCandidateUser() {
        User u = new User("candidate_user",
            "candidate",
            "user",
            "candidate@email.com",
            Role.user);
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(auditUser);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getDefaultPartner());
        u.setCandidate(getCandidate());
        return u;
    }

    public static User getFullUser() {
        User u = new User("full_user",
                "full",
                "user",
                "full.user@tbb.org",
                Role.admin);
        u.setJobCreator(true);
        u.setApprover(auditUser);
        u.setPurpose("Complete intakes");
        u.setSourceCountries(new HashSet<>(List.of(CountryTestData.JORDAN)));
        u.setReadOnly(false);
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(auditUser);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getSourcePartner());
        return u;
    }

    /**
     * Holds an {@link UpdateUserRequest} along with the expected {@link User}
     * that should result from applying the request.
     */
    public record CreateUpdateUserTestData(UpdateUserRequest request, User expectedUser) { }

    /**
     * Constructs a {@link CreateUpdateUserTestData} record containing an {@link UpdateUserRequest}
     * and the expected {@link User} that should result from using it.
     */
    public static CreateUpdateUserTestData createUpdateUserRequestAndExpectedUser() {
        final String email = "alice@email.com";
        final String firstName = "Alice";
        final String lastName = "Alison";
        final String password = "password";
        final boolean readOnly = false;
        final Role role = Role.admin;
        final boolean jobCreator = false;
        final String purpose = "Testing";
        final Status status = Status.active;
        final String username = "aalison";
        final boolean usingMfa = true;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail(email);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPassword(password);
        request.setReadOnly(readOnly);
        request.setRole(role);
        request.setJobCreator(jobCreator);
        request.setPurpose(purpose);
        request.setStatus(status);
        request.setUsername(username);
        request.setUsingMfa(usingMfa);
        request.setPartnerId(1L);

        User expectedUser = new User();
        expectedUser.setEmail(email);
        expectedUser.setFirstName(firstName);
        expectedUser.setLastName(lastName);
        expectedUser.setReadOnly(readOnly);
        expectedUser.setRole(role);
        expectedUser.setJobCreator(jobCreator);
        expectedUser.setPurpose(purpose);
        expectedUser.setStatus(status);
        expectedUser.setUsername(username);
        expectedUser.setUsingMfa(usingMfa);
        expectedUser.setPasswordEnc(password);

        return new CreateUpdateUserTestData(request, expectedUser);
    }

}
