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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;

/**
 * Service for managing {@link Partner}
 *
 * @author John Cameron
 */
public interface PartnerService {

    /**
     * Get the Partner with the given id.
     * @param partnerId ID of partner to get
     * @return partner
     * @throws NoSuchObjectException if there is no Partner with this id.
     */
    @NonNull
    Partner getPartner(long partnerId) throws NoSuchObjectException;

    /**
     * Get the default source partner (eg TBB).
     * @return Default source partner (as defined in database - defaultSourcePartner field)
     * @throws NoSuchObjectException if no default source partner is configured
     */
    @NonNull
    Partner getDefaultSourcePartner() throws NoSuchObjectException;

    /**
     * Returns partner who can be auto assigned to a candidate based on the country the candidate
     * is currently located in. Null if none found.
     * <p/>
     * If there is ambiguity - ie more than one partner is found, then returns null.
     * @param country Country associated with partner
     * @return Auto assignable partner
     */
    @Nullable
    Partner getAutoAssignablePartnerByCountry(@Nullable Country country);

    /**
     * Get the partner associated with the given partner abbreviation (case insensitive)
     * @param partnerAbbreviation eg TBB
     * @return Partner associated with partnerAbbreviation or null if abbreviation is null or no
     * partner is found with that abbreviation
     */
    @Nullable
    Partner getPartnerFromAbbreviation(@Nullable String partnerAbbreviation);

    /**
     * Lists all active partners
     * @return
     */
    List<PartnerImpl> listPartners();

    /**
     * Convenience method which just delegates to {@link #search(SearchPartnerRequest)} with an
     * appropriate request.
     * @return All active source partners
     */
    List<PartnerImpl> listActiveSourcePartners();

    /**
     * Convenience method which just delegates to {@link #search(SearchPartnerRequest)} with an
     * appropriate request and returns ALL source partners of any status.
     * @return All source partners
     */
    List<PartnerImpl> listAllSourcePartners();

    /**
     * Get the partners from search request
     * @param request - Search Request
     * @return Matching partners
     */
    List<PartnerImpl> search(SearchPartnerRequest request);

    /**
     * Get the partners as a paged search request
     * @param request - Paged Search Request
     * @return Page of partners
     */
    Page<PartnerImpl> searchPaged(SearchPartnerRequest request);

    /**
     * Create a partner.
     * @param request Request contains partner data
     * @return Created partner
     * @throws InvalidRequestException if required attribute, name, is not present or partner type
     * is unrecognized.
     * @throws NoSuchObjectException if there is an unknown country id in the request
     */
    @NonNull
    PartnerImpl create(UpdatePartnerRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException;

    /**
     * Update the partner with the given ID.
     * @param id of partner to update
     * @param request Request contains updated data
     * @return Updated partner
     * @throws InvalidRequestException if partner type is unrecognized.
     * @throws NoSuchObjectException if there is not a partner with this id.
     */
    @NonNull
    PartnerImpl update(long id, UpdatePartnerRequest request) throws InvalidRequestException, NoSuchObjectException;

    /**
     * Update the given user contact for the given partner and job
     */
    void updateJobContact(Partner partner, SalesforceJobOpp job, User contactUser);
}
