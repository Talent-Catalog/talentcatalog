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

package org.tctalent.server.model.db.partner;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;

/**
 * All kinds of partners inherit from this.
 *
 * @author John Cameron
 */
public interface BasePartner {

    /**
     * Abbreviated name of partner, if any.
     * @return Abbreviated name of partner - eg "TBB", or null if no abbreviation.
     */
    @Nullable
    String getAbbreviation();
    void setAbbreviation(@Nullable String s);

    @Nullable
    User getDefaultContact();
    void setDefaultContact(@Nullable User defaultContact);

    /**
     * Unique id identifying this partner
     * @return partner id
     */
    Long getId();
    void setId(Long id);

    /**
     * Partner's logo.
     * <p/>
     * Optional - if none is specified this call returns null, and the calling code may default
     * to using a default logo.
     * @return Link to partner's logo.
     */
    @Nullable
    String getLogo();
    void setLogo(@Nullable String s);

    /**
     * Name of partner.
     * @return Name of partner - eg "Talent Beyond Boundaries"
     */
    @NonNull
    String getName();
    void setName(@NonNull String s);

    /**
     * Email used to notify partner
     * @return Partner email
     */
    @Nullable
    String getNotificationEmail();
    void setNotificationEmail(@Nullable String notificationEmail);

    /**
     * Status of partner. Partners may be inactivated or deleted.
     * @return Status of partner
     */
    @NonNull
    Status getStatus();
    void setStatus(@NonNull Status s);

    /**
     * Url of partner's website (optional)
     * @return Website url, null if none - eg "https://talentbeyondboundaries.org/"
     */
    @Nullable
    String getWebsiteUrl();
    void setWebsiteUrl(@Nullable String s);

}
