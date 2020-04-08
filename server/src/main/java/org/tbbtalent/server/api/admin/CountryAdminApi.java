package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.request.country.CreateCountryRequest;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;
import org.tbbtalent.server.service.CountryService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/country")
public class CountryAdminApi {

    private final CountryService countryService;

    @Autowired
    public CountryAdminApi(CountryService countryService) { this.countryService = countryService; }

    @GetMapping()
    public List<Map<String, Object>> listAllCountries() {
        List<Country> countries = countryService.listCountries();
        return countryDto().buildList(countries);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCountryRequest request) {
        Page<Country> countries = this.countryService.searchCountries(request);
        return countryDto().buildPage(countries);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Country country = this.countryService.getCountry(id);
        return countryDto().build(country);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateCountryRequest request) throws EntityExistsException {
        Country country = this.countryService.createCountry(request);
        return countryDto().build(country);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateCountryRequest request) throws EntityExistsException  {

        Country country = this.countryService.updateCountry(id, request);
        return countryDto().build(country);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
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
