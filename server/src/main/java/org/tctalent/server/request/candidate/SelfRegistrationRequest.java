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

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.HasTcQueryParameters;

@Getter
@Setter
public class SelfRegistrationRequest extends BaseCandidateContactRequest
    implements HasTcQueryParameters {
    private String username;
    private String password;
    private String passwordConfirmation;
    private String reCaptchaV3Token;

    /**
     * If not null, can be used to look up partner to which the candidate belongs.
     */
    private String partnerAbbreviation;

    private String referrerParam;
    private String utmCampaign;
    private String utmContent;
    private String utmMedium;
    private String utmSource;
    private String utmTerm;

    /**
     * Email consent fields - both not null as should be true/false from the front end checkbox.
     */
    @NotNull
    private Boolean contactConsentRegistration;
    @NotNull
    private Boolean contactConsentPartners;

}
