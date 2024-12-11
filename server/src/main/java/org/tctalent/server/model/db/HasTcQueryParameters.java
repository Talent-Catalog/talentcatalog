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

package org.tctalent.server.model.db;

/**
 * Interface for objects which contain all the supported TC query parameters.
 *
 * @author John Cameron
 */
public interface HasTcQueryParameters {
    /**
     * Partner parameter as specified by "p=" query parameter
     */
    String getPartnerAbbreviation();

    /**
     * Referrer parameter as specified by "r=" query parameter
     */
    String getReferrerParam();

    /**
     * Campaign as specified by "utm_campaign" query parameter
     */
    String getUtmCampaign();
    /**
     * Content as specified by "utm_content" query parameter
     */
    String getUtmContent();

    /**
     * Medium as specified by "utm_medium" query parameter
     */
    String getUtmMedium();
    /**
     * Source as specified by "utm_source" query parameter
     */
    String getUtmSource();

    /**
     * Term as specified by "utm_term" query parameter
     */
    String getUtmTerm();

}
