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

import java.time.OffsetDateTime;
import java.util.Collections;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;

public class UserTestData {

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
        u.setApprover(getSystemAdminUser());
        u.setPurpose("Complete intakes");
        u.setSourceCountries(CountryTestData.getSourceCountrySetA());
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(getSystemAdminUser());
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(PartnerImplTestData.getSourcePartner());
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
        u.setCreatedBy(getAdminUser());
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(PartnerImplTestData.getDefaultSourcePartner());
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
        u.setCreatedBy(getSystemAdminUser());
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(PartnerImplTestData.getSourcePartner());
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
        u.setCreatedBy(getAdminUser());
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(PartnerImplTestData.getDefaultSourcePartner());
        u.setCandidate(CandidateTestData.getCandidate());
        return u;
    }

}
