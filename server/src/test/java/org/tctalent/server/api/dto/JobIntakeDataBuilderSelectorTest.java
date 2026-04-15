/*
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

package org.tctalent.server.api.dto;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.service.db.CountryService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobIntakeDataBuilderSelectorTest {

  @Mock CountryService countryService;

  private JobIntakeDataBuilderSelector selector;

  @BeforeEach
  void setUp() {
    // Country builder used inside jobOppDto()
    when(countryService.selectBuilder())
        .thenReturn(new DtoBuilder().add("code").add("name"));

    selector = new JobIntakeDataBuilderSelector(countryService);
  }

  @Test
  void selectBuilder_invokesCountrySelectorOnce() {
    selector.selectBuilder(); // constructing the builder should request country builder once
    verify(countryService, times(1)).selectBuilder();
  }

  @Test
  void selectBuilder_buildsExpectedShape_withNestedJobOppUserAndCountry() {
    // contact user (nested under jobOpp)
    Map<String, Object> contact = new LinkedHashMap<>();
    contact.put("id", 9L);
    contact.put("firstName", "Alice");
    contact.put("lastName", "Smith");
    contact.put("email", "alice@smith.com");

    // country (nested under jobOpp.countryObject) — built via mocked CountryService.selectBuilder()
    Map<String, Object> country = new LinkedHashMap<>();
    country.put("code", "PK");
    country.put("name", "Pakistan");

    // jobOpp aggregate
    Map<String, Object> jobOpp = new LinkedHashMap<>();
    jobOpp.put("id", 100L);
    jobOpp.put("sfId", "SF-100");
    jobOpp.put("contactUser", contact);
    jobOpp.put("countryObject", country);
    jobOpp.put("createdDate", "2024-01-01T00:00:00Z");
    jobOpp.put("employer", "Acme");
    jobOpp.put("name", "Senior Dev");

    // top-level intake data
    Map<String, Object> joi = new LinkedHashMap<>();
    joi.put("id", 1L);
    joi.put("jobOpp", jobOpp);
    joi.put("salaryRange", "50-60k");
    joi.put("recruitmentProcess", "2 rounds");
    joi.put("employerCostCommitment", "Relocation support");
    joi.put("location", "Toronto");
    joi.put("locationDetails", "Hybrid");
    joi.put("benefits", "Health, Dental");
    joi.put("languageRequirements", "EN B2");
    joi.put("educationRequirements", "BSc CS");
    joi.put("skillRequirements", "Java, Spring");
    joi.put("employmentExperience", "5y+");
    joi.put("occupationCode", "2512");

    Map<String, Object> out = selector.selectBuilder().build(joi);

    // top-level
    assertEquals(1L, out.get("id"));
    assertEquals("50-60k", out.get("salaryRange"));
    assertEquals("2 rounds", out.get("recruitmentProcess"));
    assertEquals("Relocation support", out.get("employerCostCommitment"));
    assertEquals("Toronto", out.get("location"));
    assertEquals("Hybrid", out.get("locationDetails"));
    assertEquals("Health, Dental", out.get("benefits"));
    assertEquals("EN B2", out.get("languageRequirements"));
    assertEquals("BSc CS", out.get("educationRequirements"));
    assertEquals("Java, Spring", out.get("skillRequirements"));
    assertEquals("5y+", out.get("employmentExperience"));
    assertEquals("2512", out.get("occupationCode"));

    // nested: jobOpp
    @SuppressWarnings("unchecked")
    Map<String, Object> jobOppOut = (Map<String, Object>) out.get("jobOpp");
    assertNotNull(jobOppOut);
    assertEquals(100L, jobOppOut.get("id"));
    assertEquals("SF-100", jobOppOut.get("sfId"));
    assertEquals("Acme", jobOppOut.get("employer"));
    assertEquals("Senior Dev", jobOppOut.get("name"));
    assertEquals("2024-01-01T00:00:00Z", jobOppOut.get("createdDate"));

    // nested: jobOpp.contactUser
    @SuppressWarnings("unchecked")
    Map<String, Object> contactOut = (Map<String, Object>) jobOppOut.get("contactUser");
    assertNotNull(contactOut);
    assertEquals(9L, contactOut.get("id"));
    assertEquals("Alice", contactOut.get("firstName"));
    assertEquals("Smith", contactOut.get("lastName"));
    assertEquals("alice@smith.com", contactOut.get("email"));

    // nested: jobOpp.countryObject (built via mocked CountryService.selectBuilder())
    @SuppressWarnings("unchecked")
    Map<String, Object> countryOut = (Map<String, Object>) jobOppOut.get("countryObject");
    assertNotNull(countryOut);
    assertEquals("PK", countryOut.get("code"));
    assertEquals("Pakistan", countryOut.get("name"));

    // verify interaction
    verify(countryService, times(1)).selectBuilder();
  }

  @Test
  void selectBuilder_omitsNullNestedValues() {
    // country exists, contact user is null
    Map<String, Object> country = new LinkedHashMap<>();
    country.put("code", "CA");
    country.put("name", "Canada");

    Map<String, Object> jobOpp = new LinkedHashMap<>();
    jobOpp.put("id", 200L);
    jobOpp.put("sfId", "SF-200");
    jobOpp.put("contactUser", null);               // explicitly null -> omitted by DtoBuilder
    jobOpp.put("countryObject", country);

    Map<String, Object> joi = new LinkedHashMap<>();
    joi.put("id", 2L);
    joi.put("jobOpp", jobOpp);

    Map<String, Object> out = selector.selectBuilder().build(joi);

    @SuppressWarnings("unchecked")
    Map<String, Object> jobOppOut = (Map<String, Object>) out.get("jobOpp");
    assertNotNull(jobOppOut);
    assertEquals(200L, jobOppOut.get("id"));
    assertFalse(jobOppOut.containsKey("contactUser")); // null omitted

    @SuppressWarnings("unchecked")
    Map<String, Object> countryOut = (Map<String, Object>) jobOppOut.get("countryObject");
    assertEquals("CA", countryOut.get("code"));
    assertEquals("Canada", countryOut.get("name"));
  }

}
