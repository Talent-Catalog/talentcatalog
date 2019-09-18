package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.service.CountryService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

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
        List<Country> countries = countryService.listCountries();
        return countryDto().buildList(countries);
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
