/*
 * Copyright (c) 2024 Talent Catalog.
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

import java.time.LocalDate;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;

@Getter
@Setter
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
    private Long[] otherNationalityIds;
    /* EXTERNAL ID */
    private String externalId;
    private String externalIdSource;
    /* UNHCR */
    @Enumerated(EnumType.STRING)
    private YesNoUnsure unhcrRegistered;
    private String unhcrNumber;
    @Enumerated(EnumType.STRING)
    private YesNo unhcrConsent;
}
