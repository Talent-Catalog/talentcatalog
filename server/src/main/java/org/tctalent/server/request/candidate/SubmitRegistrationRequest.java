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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Setter
@Getter
@ToString
public class SubmitRegistrationRequest {

    /**
     * If not null, indicates the term in the database that has been accepted.
     */
    @Nullable
    private Long acceptedPrivacyPolicyId;

    /**
     * Email consent fields - both not null as should be true/false from the front end checkbox.
     */
    private boolean contactConsentRegistration;
    private boolean contactConsentPartners;
}
