package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.request.country.CreateCountryRequest;
import org.tbbtalent.server.request.country.SearchCountryRequest;
import org.tbbtalent.server.request.country.UpdateCountryRequest;

import java.util.List;

public interface CountryService {

    List<Country> list();

    List<Country> listCountries();

    Page<Country> searchCountries(SearchCountryRequest request);

    Country getCountry(long id);

    Country createCountry(CreateCountryRequest request) throws EntityExistsException;

    Country updateCountry(long id, UpdateCountryRequest request) throws EntityExistsException;

    boolean deleteCountry(long id) throws EntityReferencedException;

}
