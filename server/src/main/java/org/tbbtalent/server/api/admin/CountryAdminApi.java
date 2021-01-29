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

package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/country")
public class CountryAdminApi implements 
        ITableApi<SearchCountryRequest, UpdateCountryRequest, UpdateCountryRequest> {

    private final CountryService countryService;

    @Autowired
    public CountryAdminApi(CountryService countryService) { this.countryService = countryService; }

    @Override
    public @NotNull List<Map<String, Object>> list() {
        List<Country> countries = countryService.listCountries(false);
        return countryDto().buildList(countries);
    }

    @GetMapping("restricted")
    public @NotNull List<Map<String, Object>> listRestricted() {
        List<Country> countries = countryService.listCountries(true);
        return countryDto().buildList(countries);
    }

    @GetMapping("destinations")
    public @NotNull List<Map<String, Object>> listTBBDestinations() {
        List<Country> countries = countryService.getTBBDestinations();
        return countryDto().buildList(countries);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            @Valid SearchCountryRequest request) {
        Page<Country> countries = this.countryService.searchCountries(request);
        return countryDto().buildPage(countries);
    }

    @Override
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        Country country = this.countryService.getCountry(id);
        return countryDto().build(country);
    }

    @Override
    public @NotNull Map<String, Object> create(@Valid UpdateCountryRequest request) 
            throws EntityExistsException {
        Country country = this.countryService.createCountry(request);
        return countryDto().build(country);
    }

    @Override
    public @NotNull Map<String, Object> update(
            long id, @Valid UpdateCountryRequest request) 
            throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        Country country = this.countryService.updateCountry(id, request);
        return countryDto().build(country);
    }

    @Override
    public boolean delete(long id) 
            throws EntityReferencedException, InvalidRequestException {
        return this.countryService.deleteCountry(id);
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
