package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;
import org.tbbtalent.server.service.CountryService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/country")
public class CountryAdminApi implements 
        ITableApi<SearchCountryRequest, UpdateCountryRequest, UpdateCountryRequest> {

    private final CountryService countryService;

    @Autowired
    public CountryAdminApi(CountryService countryService) { this.countryService = countryService; }

    @Override
    public @NotNull List<Map<String, Object>> list() {
        List<Country> countries = countryService.listCountries();
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
