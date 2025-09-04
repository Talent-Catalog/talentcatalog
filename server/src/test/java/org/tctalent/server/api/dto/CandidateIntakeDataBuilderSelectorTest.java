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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateIntakeDataBuilderSelectorTest {

  @Mock CountryService countryService;
  @Mock OccupationService occupationService;

  private CandidateIntakeDataBuilderSelector selector;

  @BeforeEach
  void setUp() {
    // Minimal nested builders used throughout
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder().add("code").add("name"));
    when(occupationService.selectBuilder()).thenReturn(new DtoBuilder().add("id").add("name"));

    selector = new CandidateIntakeDataBuilderSelector(countryService, occupationService);
  }

  @Test
  void selectBuilder_invokesNestedSelectors_expectedCounts() {
    // When we construct the builder, nested selectors are wired immediately
    selector.selectBuilder();

    // countryService used in 5 places:
    // birthCountry, drivingLicenseCountry, candidateCitizenship.nationality,
    // candidateDestination.country, candidateVisaCheck.country
    verify(countryService, times(5)).selectBuilder();

    // occupationService used in 2 places: partnerOccupation, visaJobCheck.occupation
    verify(occupationService, times(2)).selectBuilder();
  }

  @Test
  void build_buildsExpectedNestedShape() {
    // given
    DtoBuilder b = selector.selectBuilder();

    // source as maps mean only need to include fields we assert; missing keys are treated as null
    // and omitted by DtoBuilder
    Map<String, Object> src =
        Map.of(
            "arrestImprison", true,
            "birthCountry", country("PK", "Pakistan"),
            "drivingLicenseCountry", country("CA", "Canada"),

            "candidateCitizenships", List.of(
                Map.of("id", 1L,
                    "nationality", country("PK", "Pakistan"),
                    "hasPassport", true)
            ),

            "candidateDependants", List.of(
                Map.of("id", 10L, "name", "Child One")
            ),

            "candidateDestinations", List.of(
                Map.of("id", 2L, "country", country("GB", "United Kingdom"), "interest", "HIGH")
            ),

            "candidateVisaChecks", List.of(
                Map.of(
                    "id", 5L,
                    "country", country("AU", "Australia"),
                    "candidateVisaJobChecks", List.of(
                        Map.of(
                            "id", 7L,
                            "jobOpp", jobOpp(100L, "Developer", "SF-100"),
                            "occupation", occupation(123L, "Software Engineer")
                        )
                    ),
                    "createdBy", user(9L, "A", "B"),
                    "updatedBy", user(10L, "C", "D")
                )
            ),

            "partnerOccupation", occupation(222L, "Nurse")
        );

    // when
    Map<String, Object> out = b.build(src);

    // then
    // top-level simple
    assertEquals(true, out.get("arrestImprison"));

    // birthCountry / drivingLicenseCountry via CountryService builder
    assertMapEquals(country("PK", "Pakistan"), out.get("birthCountry"));
    assertMapEquals(country("CA", "Canada"), out.get("drivingLicenseCountry"));

    // candidateCitizenships[0].nationality.*
    var ccs = listOfMaps(out.get("candidateCitizenships"));
    assertEquals(1, ccs.size());
    assertEquals(1L, ccs.get(0).get("id"));
    assertMapEquals(country("PK", "Pakistan"), ccs.get(0).get("nationality"));
    assertEquals(true, ccs.get(0).get("hasPassport"));

    // candidateDependants
    var deps = listOfMaps(out.get("candidateDependants"));
    assertEquals(1, deps.size());
    assertEquals(10L, deps.get(0).get("id"));
    assertEquals("Child One", deps.get(0).get("name"));

    // candidateDestinations[0].country.*
    var dests = listOfMaps(out.get("candidateDestinations"));
    assertEquals(1, dests.size());
    assertMapEquals(country("GB", "United Kingdom"), dests.get(0).get("country"));
    assertEquals("HIGH", dests.get(0).get("interest"));

    // candidateVisaChecks[0]...
    var visaChecks = listOfMaps(out.get("candidateVisaChecks"));
    assertEquals(1, visaChecks.size());
    assertMapEquals(country("AU", "Australia"), visaChecks.get(0).get("country"));

    // ...candidateVisaJobChecks[0].occupation + jobOpp
    var jobChecks = listOfMaps(visaChecks.get(0).get("candidateVisaJobChecks"));
    assertEquals(1, jobChecks.size());
    assertMapEquals(occupation(123L, "Software Engineer"), jobChecks.get(0).get("occupation"));
    assertMapEquals(jobOpp(100L, "Developer", "SF-100"), jobChecks.get(0).get("jobOpp"));

    // partnerOccupation at top level
    assertMapEquals(occupation(222L, "Nurse"), out.get("partnerOccupation"));

    // interactions per selectBuilder() call
    verify(countryService, times(5)).selectBuilder();
    verify(occupationService, times(2)).selectBuilder();
  }

  @Test
  void build_omitsNullsAndMissingKeys() {
    DtoBuilder b = selector.selectBuilder();

    // Use a mutable map so we can include nulls (Map.of forbids null)
    Map<String, Object> src = new LinkedHashMap<>();
    src.put("arrestImprison", false);
    src.put("birthCountry", null);            // explicitly null -> should be omitted
    src.put("candidateCitizenships", List.of()); // empty list -> stays empty

    Map<String, Object> out = b.build(src);

    assertEquals(false, out.get("arrestImprison"));
    assertFalse(out.containsKey("birthCountry"));          // null omitted
    assertEquals(List.of(), out.get("candidateCitizenships"));

    verify(countryService, times(5)).selectBuilder();
    verify(occupationService, times(2)).selectBuilder();
  }

  // Helpers methods
  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> listOfMaps(Object o) {
    assertNotNull(o);
    return (List<Map<String, Object>>) o;
  }

  private static void assertMapEquals(Map<String, Object> expected, Object actual) {
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  private static Map<String, Object> country(String code, String name) {
    return Map.of("code", code, "name", name);
  }

  private static Map<String, Object> occupation(long id, String name) {
    return Map.of("id", id, "name", name);
  }

  private static Map<String, Object> jobOpp(long id, String name, String sfId) {
    return Map.of("id", id, "name", name, "sfId", sfId);
  }

  private static Map<String, Object> user(long id, String first, String last) {
    return Map.of("id", id, "firstName", first, "lastName", last);
  }
}
