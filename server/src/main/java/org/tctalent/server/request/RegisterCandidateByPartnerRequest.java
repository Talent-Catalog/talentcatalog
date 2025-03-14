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

package org.tctalent.server.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.tctalent.anonymization.model.RegisterCandidateRequest;

/**
 * This request is a copy of the request that comes in on the public API, with the added
 * field of the partner that the request came from.
 * This will have been deduced from the authentication process on the public api service.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class RegisterCandidateByPartnerRequest extends RegisterCandidateRequest {

    /**
     * Partner (service provider) associated with offer.
     */
    @NonNull
    Long partnerId;

}
