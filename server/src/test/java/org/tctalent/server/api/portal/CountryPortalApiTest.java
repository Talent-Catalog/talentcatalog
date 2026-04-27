package org.tctalent.server.api.portal;/*
 * Copyright (c) 2025 Talent Catalog.
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

class CountryPortalApiTest {

  @Mock
  private CountryService countryService;

  @InjectMocks
  private CountryPortalApi countryPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
  }

  @Test
  void testListAllCountries_Success() {
    List<Country> countries = List.of(createSampleCountry());
    when(countryService.listCountries(false)).thenReturn(countries);

    List<Map<String, Object>> result = countryPortalApi.listAllCountries();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> countryDto = result.get(0);
    assertEquals(1L, countryDto.get("id"));
    assertEquals("Test Country", countryDto.get("name"));
    verify(countryService).listCountries(false);
    verify(countryService).selectBuilder();
  }

  @Test
  void testListAllCountries_EmptyList() {
    when(countryService.listCountries(false)).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = countryPortalApi.listAllCountries();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(countryService).listCountries(false);
    verify(countryService).selectBuilder();
  }

  @Test
  void testListTCDestinations_Success() {
    List<Country> countries = List.of(createSampleCountry());
    when(countryService.getTCDestinations()).thenReturn(countries);

    List<Map<String, Object>> result = countryPortalApi.listTCDestinations();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> countryDto = result.get(0);
    assertEquals(1L, countryDto.get("id"));
    assertEquals("Test Country", countryDto.get("name"));
    verify(countryService).getTCDestinations();
    verify(countryService).selectBuilder();
  }

  @Test
  void testListTCDestinations_EmptyList() {
    when(countryService.getTCDestinations()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = countryPortalApi.listTCDestinations();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(countryService).getTCDestinations();
    verify(countryService).selectBuilder();
  }

  private Country createSampleCountry() {
    Country country = new Country();
    country.setId(1L);
    country.setName("Test Country");
    return country;
  }
}