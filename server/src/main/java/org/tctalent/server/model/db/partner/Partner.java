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

import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.User;

/**
 * This is the superset of all types of partners.
 * <p/>
 * A partner is an organization that works to implement displaced talent mobility.
 * <p/>
 * All Talent Catalog users are associated with just one partner at a time.
 *
 * @author John Cameron
 */
public interface Partner extends SourcePartner, RecruiterPartner, EmployerPartner {

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

    /**
     * Gets the contact associated with {@link #getContextJobId()} if it is not null, otherwise
     * returns the default partner contact, {@link #getDefaultContact()}
     * @return Contact user
     */
    @Nullable
    User getJobContact();

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

}
