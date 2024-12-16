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

package org.tctalent.server.request.partner;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;

@Getter
@Setter
public class UpdatePartnerRequest {

    private String abbreviation;

    private boolean autoAssignable;

    private Long defaultContactId;

    /**
     * Redundant field containing looked up user corresponding to {@link #defaultContactId}.
     * Should be populated from id before processing.
     * Gets around mutual dependency between user and partner services due to every user having
     * a partner and every partner having a default contact user.
     */
    private User defaultContact;

    /**
     * Redundant field containing looked up employer corresponding to {@link #employerSflink}.
     * Should be populated from the sfLink before processing.
     * Gets around mutual dependency between employer and partner services.
     */
    private Employer employer;

    private boolean defaultPartnerRef;

    private String employerSflink;

    private boolean jobCreator;

    private String logo;

    private String name;

    private String notificationEmail;

    private boolean sourcePartner;

    private Status status;

    private String websiteUrl;

    private String registrationLandingPage;

    private String sflink;

    private Set<Long> sourceCountryIds;

    private Long redirectPartnerId;

}
