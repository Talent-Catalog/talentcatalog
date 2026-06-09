/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.country.SearchCountryRequest;
import org.tctalent.server.request.country.UpdateCountryRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.TranslationService;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private TranslationService translationService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CountryServiceImpl service;

    @Test
    void afterPropertiesSet_buildsDestinationCountriesFromConfiguredNames() {
        Country canada = country(1L, "Canada", Status.active);
        Country australia = country(2L, "Australia", Status.active);
        ReflectionTestUtils.setField(
            service, "tcDestinations", new String[] {"Canada", "Missing", "Australia"});
        when(countryRepository.findByNameIgnoreCase("Canada")).thenReturn(canada);
        when(countryRepository.findByNameIgnoreCase("Missing")).thenReturn(null);
        when(countryRepository.findByNameIgnoreCase("Australia")).thenReturn(australia);

        service.afterPropertiesSet();

        assertEquals(List.of(canada, australia), service.getTCDestinations());
        assertTrue(service.isTCDestination(canada.getId()));
        assertFalse(service.isTCDestination(99L));
    }

    @Test
    void listCountries_whenRestrictedUserHasSourceCountries_returnsOnlyPermittedActiveCountries() {
        Country sourceCountry = country(10L, "Jordan", Status.active);
        User user = new User();
        user.setSourceCountries(Set.of(sourceCountry));
        List<Country> countries = List.of(sourceCountry);
        when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
        when(countryRepository.findByStatusAndSourceCountries(Status.active, user.getSourceCountries()))
            .thenReturn(countries);

        List<Country> result = service.listCountries(true);

        assertSame(countries, result);
        verify(countryRepository).findByStatusAndSourceCountries(
            Status.active, user.getSourceCountries());
        verify(countryRepository, never()).findByStatus(Status.active);
        verify(translationService).translate(countries, "country");
    }

    @Test
    void listCountries_whenRestrictedUserHasNoSourceCountries_returnsAllActiveCountries() {
        User user = new User();
        List<Country> countries = List.of(country(20L, "Lebanon", Status.active));
        when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
        when(countryRepository.findByStatus(Status.active)).thenReturn(countries);

        List<Country> result = service.listCountries(true);

        assertSame(countries, result);
        verify(countryRepository).findByStatus(Status.active);
        verify(countryRepository, never()).findByStatusAndSourceCountries(eq(Status.active), any());
        verify(translationService).translate(countries, "country");
    }

    @Test
    void listCountries_whenUnrestricted_returnsAllActiveCountries() {
        List<Country> countries = List.of(country(30L, "Uganda", Status.active));
        when(authService.getLoggedInUser()).thenReturn(Optional.empty());
        when(countryRepository.findByStatus(Status.active)).thenReturn(countries);

        List<Country> result = service.listCountries(false);

        assertSame(countries, result);
        verify(countryRepository).findByStatus(Status.active);
        verify(countryRepository, never()).findByStatusAndSourceCountries(eq(Status.active), any());
        verify(translationService).translate(countries, "country");
    }

    @Test
    void searchCountries_whenLanguageProvided_translatesPageContent() {
        SearchCountryRequest request = new SearchCountryRequest();
        request.setLanguage("es");
        Country country = country(40L, "Spain", Status.active);
        Page<Country> page = new PageImpl<>(List.of(country));
        when(countryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(authService.getLoggedInUser()).thenReturn(Optional.empty());

        Page<Country> result = service.searchCountries(request);

        assertSame(page, result);
        verify(translationService).translate(page.getContent(), "country", "es");
    }

    @Test
    void getCountry_loadsCountryFromCacheAndReusesCache() {
        Country country = country(50L, "Kenya", Status.active);
        when(countryRepository.findAll()).thenReturn(List.of(country));

        Country firstResult = service.getCountry(country.getId());
        Country secondResult = service.getCountry(country.getId());

        assertSame(country, firstResult);
        assertSame(country, secondResult);
        verify(countryRepository).findAll();
    }

    @Test
    void getCountry_whenCountryDoesNotExist_throwsNoSuchObjectException() {
        when(countryRepository.findAll()).thenReturn(List.of(country(60L, "Rwanda", Status.active)));

        assertThrows(NoSuchObjectException.class, () -> service.getCountry(61L));
    }

    @Test
    void findByIsoCode_whenCountryExists_returnsCountry() {
        Country country = country(70L, "Germany", Status.active);
        when(countryRepository.findByIsoCode("DE")).thenReturn(Optional.of(country));

        assertSame(country, service.findByIsoCode("DE"));
    }

    @Test
    void findByIsoCode_whenCountryDoesNotExist_throwsNoSuchObjectException() {
        when(countryRepository.findByIsoCode("XX")).thenReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> service.findByIsoCode("XX"));
    }

    @Test
    void createCountry_whenNameIsUnique_savesNewCountry() {
        UpdateCountryRequest request = countryRequest("New Country", Status.active);
        when(countryRepository.findByNameIgnoreCase("New Country")).thenReturn(null);
        when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Country result = service.createCountry(request);

        assertEquals("New Country", result.getName());
        assertEquals(Status.active, result.getStatus());
        verify(countryRepository).save(result);
    }

    @Test
    void createCountry_whenNameAlreadyExists_throwsEntityExistsException() {
        UpdateCountryRequest request = countryRequest("Canada", Status.active);
        Country existingCountry = country(80L, "Canada", Status.active);
        when(countryRepository.findByNameIgnoreCase("Canada")).thenReturn(existingCountry);

        assertThrows(EntityExistsException.class, () -> service.createCountry(request));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_whenCountryExistsAndNameIsUnique_updatesAndSavesCountry() {
        Country country = country(90L, "Old Name", Status.active);
        UpdateCountryRequest request = countryRequest("New Name", Status.inactive);
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));
        when(countryRepository.findByNameIgnoreCase("New Name")).thenReturn(null);
        when(countryRepository.save(country)).thenReturn(country);

        Country result = service.updateCountry(country.getId(), request);

        assertSame(country, result);
        assertEquals("New Name", result.getName());
        assertEquals(Status.inactive, result.getStatus());
        verify(countryRepository).save(country);
    }

    @Test
    void updateCountry_whenAnotherCountryHasRequestedName_throwsEntityExistsException() {
        Country country = country(100L, "Original", Status.active);
        Country duplicate = country(101L, "Duplicate", Status.active);
        UpdateCountryRequest request = countryRequest("Duplicate", Status.active);
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));
        when(countryRepository.findByNameIgnoreCase("Duplicate")).thenReturn(duplicate);

        assertThrows(EntityExistsException.class, () -> service.updateCountry(country.getId(), request));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateCountry_whenCountryDoesNotExist_throwsNoSuchObjectException() {
        UpdateCountryRequest request = countryRequest("Missing", Status.active);
        when(countryRepository.findById(110L)).thenReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> service.updateCountry(110L, request));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void deleteCountry_whenCountryHasCandidates_throwsEntityReferencedException() {
        Country country = country(120L, "Referenced", Status.active);
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));
        when(candidateRepository.findByCountryId(country.getId())).thenReturn(List.of(new Candidate()));

        assertThrows(EntityReferencedException.class, () -> service.deleteCountry(country.getId()));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void deleteCountry_whenCountryExistsAndIsNotReferenced_marksCountryDeleted() {
        Country country = country(130L, "Unused", Status.active);
        when(countryRepository.findById(country.getId())).thenReturn(Optional.of(country));
        when(candidateRepository.findByCountryId(country.getId())).thenReturn(List.of());

        assertTrue(service.deleteCountry(country.getId()));

        assertEquals(Status.deleted, country.getStatus());
        verify(countryRepository).save(country);
    }

    @Test
    void deleteCountry_whenCountryDoesNotExist_returnsFalse() {
        when(countryRepository.findById(140L)).thenReturn(Optional.empty());
        when(candidateRepository.findByCountryId(140L)).thenReturn(List.of());

        assertFalse(service.deleteCountry(140L));
        verify(countryRepository, never()).save(any(Country.class));
    }

    @Test
    void updateIsoCodes_updatesKnownCountriesAndReturnsUnknownCountryNames() {
        Country afghanistan = country(150L, " Afghanistan ", Status.active);
        Country unknown = country(151L, "Atlantis", Status.active);
        List<Country> countries = List.of(afghanistan, unknown);
        when(authService.getLoggedInUser()).thenReturn(Optional.empty());
        when(countryRepository.findByStatus(Status.active)).thenReturn(countries);

        String result = service.updateIsoCodes();

        assertEquals("Atlantis", result);
        assertEquals("AF", afghanistan.getIsoCode());
        assertNull(unknown.getIsoCode());
        verify(countryRepository).save(afghanistan);
        verify(countryRepository, never()).save(unknown);
        verify(translationService).translate(countries, "country");
    }

    private static Country country(Long id, String name, Status status) {
        Country country = new Country(name, status);
        country.setId(id);
        return country;
    }

    private static UpdateCountryRequest countryRequest(String name, Status status) {
        UpdateCountryRequest request = new UpdateCountryRequest();
        request.setName(name);
        request.setStatus(status);
        return request;
    }
}
