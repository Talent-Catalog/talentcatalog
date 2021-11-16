/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;

import java.util.List;

public interface CountryService {

    List<Country> listCountries(Boolean restricted);

    Page<Country> searchCountries(SearchCountryRequest request);

    Country getCountry(long id);

    Country createCountry(UpdateCountryRequest request) throws EntityExistsException;

    Country updateCountry(long id, UpdateCountryRequest request) throws EntityExistsException;

    boolean deleteCountry(long id) throws EntityReferencedException;

    List<Country> getTBBDestinations();

    /**
     * Sets the country ISO codes of all countries with names matching the English country names
     * returned by Java's Locale class.
     * @return String containing names of countries in the data base which did not find a match
     * among the names returned by Locale - and which, therefore, did not have their ISO code set.
     */
    String updateIsoCodes();
}
