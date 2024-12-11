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

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.request.country.SearchCountryRequest;
import org.tctalent.server.request.country.UpdateCountryRequest;
import org.tctalent.server.service.db.CountryService;

@RestController
@RequestMapping("/api/admin/country")
@RequiredArgsConstructor
public class CountryAdminApi implements
        ITableApi<SearchCountryRequest, UpdateCountryRequest, UpdateCountryRequest> {

    private final CountryService countryService;

    @Override
    public @NotNull List<Map<String, Object>> list() {
        List<Country> countries = countryService.listCountries(false);
        return countryService.selectBuilder().buildList(countries);
    }

    @GetMapping("restricted")
    public @NotNull List<Map<String, Object>> listRestricted() {
        List<Country> countries = countryService.listCountries(true);
        return countryService.selectBuilder().buildList(countries);
    }

    @GetMapping("destinations")
    public @NotNull List<Map<String, Object>> listTCDestinations() {
        List<Country> countries = countryService.getTCDestinations();
        return countryService.selectBuilder().buildList(countries);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            @Valid SearchCountryRequest request) {
        Page<Country> countries = countryService.searchCountries(request);
        return countryService.selectBuilder().buildPage(countries);
    }

    @Override
    public @NotNull Map<String, Object> get(long id, DtoType dtoType) throws NoSuchObjectException {
        Country country = countryService.getCountry(id);
        return countryService.selectBuilder().build(country);
    }

    @Override
    public @NotNull Map<String, Object> create(@Valid UpdateCountryRequest request)
            throws EntityExistsException {
        Country country = countryService.createCountry(request);
        return countryService.selectBuilder().build(country);
    }

    @Override
    public @NotNull Map<String, Object> update(
            long id, @Valid UpdateCountryRequest request)
            throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        Country country = countryService.updateCountry(id, request);
        return countryService.selectBuilder().build(country);
    }

    @Override
    public boolean delete(long id)
            throws EntityReferencedException, InvalidRequestException {
        return countryService.deleteCountry(id);
    }
}
