package org.tbbtalent.server.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;

public interface CountryService {

    List<Country> listCountries();

    List<Country> listCountries(String selectedLanguage);

    Page<Country> searchCountries(SearchCountryRequest request);

    Country getCountry(long id);

    Country createCountry(UpdateCountryRequest request) throws EntityExistsException;

    Country updateCountry(long id, UpdateCountryRequest request) throws EntityExistsException;

    boolean deleteCountry(long id) throws EntityReferencedException;

}
