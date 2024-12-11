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

package org.tctalent.server.api.portal;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.service.db.CountryService;

@RestController()
@RequestMapping("/api/portal/country")
public class CountryPortalApi {

    private final CountryService countryService;

    @Autowired
    public CountryPortalApi(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllCountries() {
        List<Country> countries = countryService.listCountries(false);
        return countryService.selectBuilder().buildList(countries);
    }

    @GetMapping("destinations")
    public @NotNull List<Map<String, Object>> listTCDestinations() {
        List<Country> countries = countryService.getTCDestinations();
        return countryService.selectBuilder().buildList(countries);
    }
}
