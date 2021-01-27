/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
}
