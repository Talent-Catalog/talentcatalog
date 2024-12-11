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
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.request.country.SearchCountryRequest;
import org.tctalent.server.request.country.UpdateCountryRequest;
import org.tctalent.server.util.dto.DtoBuilder;

public interface CountryService {

    List<Country> listCountries(Boolean restricted);

    Page<Country> searchCountries(SearchCountryRequest request);

    /**
     * Return country with given id
     * @param id ID of country
     * @return Country corresponding to id
     * @throws NoSuchObjectException if no country is found with that id.
     */
    @NonNull
    Country getCountry(long id) throws NoSuchObjectException;

    /**
     * Find country matching given name (case insensitive).
     * @param name Country name
     * @return country or null if none found
     */
    @Nullable
    Country findCountryByName(String name);

    Country createCountry(UpdateCountryRequest request) throws EntityExistsException;

    Country updateCountry(long id, UpdateCountryRequest request) throws EntityExistsException;

    boolean deleteCountry(long id) throws EntityReferencedException;

    List<Country> getTCDestinations();

    DtoBuilder selectBuilder();

    /**
     * Sets the country ISO codes of all countries with names matching the English country names
     * returned by Java's Locale class.
     * @return String containing names of countries in the data base which did not find a match
     * among the names returned by Locale - and which, therefore, did not have their ISO code set.
     */
    String updateIsoCodes();
}
