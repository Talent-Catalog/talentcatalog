/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.partner;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;

@Getter
@Setter
public class UpdatePartnerRequest {

    private String partnerType;

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

    private boolean defaultPartnerRef;

    private String logo;

    private String name;

    private String notificationEmail;

    private Status status;

    private String websiteUrl;

    private String registrationLandingPage;

    private String sflink;

    private Set<Long> sourceCountryIds;

}
