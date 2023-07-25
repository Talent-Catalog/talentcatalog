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

package org.tbbtalent.server.model.db.partner;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;

/**
 * A partner is an organization that works to implement displaced talent mobility.
 * <p/>
 * All Talent Catalog users are associated with just one partner at a time.
 *
 * @author John Cameron
 */
public interface Partner extends SourcePartner {

    /**
     * Abbreviated name of partner, if any.
     * @return Abbreviated name of partner - eg "TBB", or null if no abbreviation.
     */
    @Nullable
    String getAbbreviation();
    void setAbbreviation(@Nullable String s);

    /**
     * True if this partner is the default source partner - associated with candidates who are
     * not clearly associated with any other source partner.
     * <p/>
     * Only one source partner at any given time can be the default.
     * @return True if this is the default source partner
     */
    boolean isDefaultSourcePartner();
    void setDefaultSourcePartner(boolean b);

    boolean isJobCreator();
    void setJobCreator(boolean b);

    boolean isSourcePartner();
    void setSourcePartner(boolean b);

    /**
     * True if this partner is the default job creator.
     * <p/>
     * Only one partner at any given time can be the default.
     * @return True if this is the default job creator
     */
    boolean isDefaultJobCreator();
    void setDefaultJobCreator(boolean b);

    @Nullable
    Long getContextJobId();
    void setContextJobId(@Nullable Long contextJobId);

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
     * Gets the contact associated with {@link #getContextJobId()} if it is not null, otherwise
     * returns the default partner contact, {@link #getDefaultContact()}
     * @return Contact user
     */
    @Nullable
    User getJobContact();

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
     * Salesforce ID (extracted from {@link #getSflink()}).
     */
    @Nullable
    String getSfId();

    /**
     * Url link to corresponding Salesforce Account record, if one exists.
     */
    @Nullable
    String getSflink();
    void setSflink(@Nullable String sflink);

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
