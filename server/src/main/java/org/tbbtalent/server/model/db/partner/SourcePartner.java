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

import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Country;

/**
 * A source partner works with displaced talent candidate users, encouraging them to register with
 * the Talent Catalog, and then assisting them to find international employment and ultimately to
 * relocate to new lives in a destination country.
 * <p/>
 * Each back end/admin users of the Talent Catalog is an employee of a source partner.
 * The branding of the Talent Catalog displayed to a back end user is defined by the user's
 * source partner.
 * <p/>
 * Each candidate user is associated at any given time with a single source partner.
 * The branding of the Talent Catalog displayed to a candidate is defined by the candidate's
 * source partner.
 *
 * @author John Cameron
 */
public interface SourcePartner extends Partner {

    /**
     * Url of registration landing page.
     * <p/>
     * Typically this page is located somewhere on the partner's website, providing information
     * about the partner and the Talent Catalog. It will contain a button or some other link
     * directing the user to the registration url - eg https://tctalent.org?p=tbb which is the entry
     * point for this application.
     * <p/>
     * For example, Talent Beyond Boundaries currently has a registration landing page at
     * https://www.talentbeyondboundaries.org/talentcatalog/register
     * @return Url of landing page. Null if none.
     */
    @Nullable
    String getRegistrationLandingPage();
    void setRegistrationLandingPage(@Nullable String s);

    /**
     * Source countries that this source partner operates in.
     * <p/>
     * If countries are specified, this can be used to assist in the assignment of a newly
     * registered candidate to a source partner - based on the country where the candidate is
     * currently located.
     * @return Associated source countries - not null but may be empty
     */
    @NonNull
    Set<Country> getSourceCountries();
    void setSourceCountries(@NonNull Set<Country> countries);

    /**
     * True if this partner is the default source partner - associated with candidates who are
     * not clearly associated with any other source partner.
     * <p/>
     * Only one source partner at any given time can be the default.
     * @return True if this is the default source partner
     */
    boolean isDefaultSourcePartner();
    void setDefaultSourcePartner(boolean b);
}
