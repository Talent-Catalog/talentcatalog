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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.util.dto.DtoBuilder;

import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.service.db.UserService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CandidateBuilderSelector.
 */
@ExtendWith(MockitoExtension.class)
class CandidateBuilderSelectorTest {

  @Mock CandidateOpportunityService candidateOpportunityService;
  @Mock CountryService countryService;
  @Mock OccupationService occupationService;
  @Mock UserService userService;

  private CandidateBuilderSelector selector;

  @BeforeEach
  void setUp() {
    selector = new CandidateBuilderSelector(
        candidateOpportunityService, countryService, occupationService, userService);
  }

  @Test
  void publicIdOnly_buildsOnlyPublicId_andDoesNotTouchServices() {
    var b = selector.selectBuilder(DtoType.PUBLIC_ID_ONLY);

    Map<String, Object> out = b.build(Map.of(
        "publicId", "cand-123",
        "id", 99L,            // should be ignored
        "candidateNumber", "C-001"    // should be ignored
    ));

    assertEquals("cand-123", out.get("publicId"));
    assertFalse(out.containsKey("id"));
    assertFalse(out.containsKey("candidateNumber"));

    verifyNoInteractions(countryService, occupationService, candidateOpportunityService, userService);
  }

  @Test
  void minimal_buildsExpectedUserShape_andDoesNotTouchServices() {
    var b = selector.selectBuilder(DtoType.MINIMAL);

    Map<String, Object> src = Map.of(
        "id", 7L,
        "candidateNumber", "C-007",
        "publicId", "cand-007",
        "user", Map.of(
            "username", "bond",
            "email", "007@mi6.gov",
            "firstName", "James",
            "lastName", "Bond",
            "partner", Map.of("id", 42L, "name", "MI6")
        )
    );

    Map<String, Object> out = b.build(src);

    assertEquals(7L, out.get("id"));
    assertEquals("C-007", out.get("candidateNumber"));
    assertEquals("cand-007", out.get("publicId"));

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) out.get("user");
    assertEquals("bond", user.get("username"));
    assertEquals("007@mi6.gov", user.get("email"));
    assertEquals("James", user.get("firstName"));
    assertEquals("Bond", user.get("lastName"));

    @SuppressWarnings("unchecked")
    Map<String, Object> partner = (Map<String, Object>) user.get("partner");
    assertEquals(42L, partner.get("id"));
    assertEquals("MI6", partner.get("name"));

    verifyNoInteractions(countryService, occupationService, candidateOpportunityService, userService);
  }

  @Test
  void preview_omitsNonPreviewFields_andKeepsCore() {
    var b = selector.selectBuilder(DtoType.PREVIEW);

    // Provide source including fields that are ONLY added when !PREVIEW
    Map<String, Object> src = Map.of(
        "id", 77L,
        "country", country("PK", "Pakistan"),
        "candidateOpportunities", List.of(
            Map.of(
                "id", 1001L,
                "name", "Senior Java",
                "jobOpp", Map.of("id", 501L, "name", "Role", "country", country("CA","Canada")),
                "stage", "applied",
                "nextStep", "review",
                "nextStepDueDate", "2024-01-01",
                // these should be dropped in PREVIEW:
                "closingComments", "secret",
                "createdBy", Map.of("id", 1L),
                "updatedBy", Map.of("id", 2L)
            )
        ),
        // top-level fields that are added ONLY when !PREVIEW:
        "contextNote", "should-not-appear",
        "shareableNotes", "should-not-appear"
    );

    Map<String, Object> out = b.build(src);

    // Core fields present
    assertEquals(77L, out.get("id"));
    assertMapEquals(country("PK", "Pakistan"), out.get("country"));

    // candidateOpportunities: core present, PREVIEW-only extras absent
    var opps = listOfMaps(out.get("candidateOpportunities"));
    assertEquals(1, opps.size());
    assertEquals(1001L, opps.get(0).get("id"));
    assertEquals("Senior Java", opps.get(0).get("name"));
    assertEquals("applied", opps.get(0).get("stage"));
    assertEquals("review", opps.get(0).get("nextStep"));
    assertEquals("2024-01-01", opps.get(0).get("nextStepDueDate"));
    @SuppressWarnings("unchecked")
    Map<String, Object> job = (Map<String, Object>) opps.get(0).get("jobOpp");
    assertEquals(501L, job.get("id"));
    assertEquals("Role", job.get("name"));
    assertMapEquals(country("CA", "Canada"), job.get("country"));
    assertFalse(opps.get(0).containsKey("closingComments"));
    assertFalse(opps.get(0).containsKey("createdBy"));
    assertFalse(opps.get(0).containsKey("updatedBy"));

    // Top-level: fields added only when !PREVIEW should be absent
    assertFalse(out.containsKey("contextNote"));
    assertFalse(out.containsKey("shareableNotes"));
    assertFalse(out.containsKey("candidateProperties"));
    assertFalse(out.containsKey("shareableCv"));
    assertFalse(out.containsKey("shareableDoc"));
    assertFalse(out.containsKey("listShareableCv"));
    assertFalse(out.containsKey("listShareableDoc"));
    assertFalse(out.containsKey("miniIntakeCompletedBy"));
    assertFalse(out.containsKey("fullIntakeCompletedBy"));

    verify(countryService, atLeastOnce()).selectBuilder();
    verifyNoInteractions(occupationService); // not used on PREVIEW path here
  }

  @Test
  void full_buildsCountryNationalityAndJobCountry_andWiresVisibilityDeps() {
    when(userService.getLoggedInUser()).thenReturn(null);
    when(candidateOpportunityService.findJobCreatorPartnerOpps(any())).thenReturn(List.of());
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder().add("code").add("name"));

    var b = selector.selectBuilder(DtoType.FULL); // triggers user/opps + wires country builder

    Map<String, Object> src = Map.of(
        "country", country("PK", "Pakistan"),
        "nationality", country("GB", "United Kingdom"),
        "candidateOpportunities", List.of(
            Map.of(
                "id", 1001L,
                "jobOpp", Map.of(
                    "id", 501L,
                    "name", "Senior Java",
                    "submissionList", Map.of("id", 777L),
                    "country", country("CA", "Canada")
                )
            )
        )
    );

    Map<String, Object> out = b.build(src);

    assertMapEquals(country("PK", "Pakistan"), out.get("country"));
    assertMapEquals(country("GB", "United Kingdom"), out.get("nationality"));

    var opps = listOfMaps(out.get("candidateOpportunities"));
    assertEquals(1, opps.size());

    @SuppressWarnings("unchecked")
    Map<String, Object> job = (Map<String, Object>) opps.get(0).get("jobOpp");
    assertEquals(501L, job.get("id"));
    assertEquals("Senior Java", job.get("name"));
    assertMapEquals(country("CA", "Canada"), job.get("country"));

    // Interactions: user path (null user) + partner opps + country builder used (>=3 times)
    verify(userService, times(1)).getLoggedInUser();
    verify(candidateOpportunityService, times(1)).findJobCreatorPartnerOpps(any());
    verify(countryService, atLeast(3)).selectBuilder();
    verifyNoInteractions(occupationService);
  }

  @Test
  void api_buildsExtendedCollectionsWithNestedCountryAndOccupation() {
    // Arrange visibility + nested builders
    var admin = mock(User.class);
    when(admin.getRole()).thenReturn(Role.systemadmin); // full visibility for API user
    when(admin.getPartner()).thenReturn(null);

    when(userService.getLoggedInUser()).thenReturn(admin);
    when(candidateOpportunityService.findJobCreatorPartnerOpps(any())).thenReturn(List.of());
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder().add("code").add("name"));
    when(occupationService.selectBuilder()).thenReturn(new DtoBuilder().add("id").add("name"));

    var b = selector.selectBuilder(DtoType.API);

    Map<String, Object> src = Map.of(
        "country", country("PK", "Pakistan"),
        "nationality", country("GB", "United Kingdom"),
        "candidateDestinations", List.of(
            Map.of("id", 1L, "country", country("DE", "Germany"))
        ),
        "candidateOccupations", List.of(
            Map.of("id", 2L, "occupation", occupation(123L, "Software Engineer"))
        ),
        "candidateJobExperiences", List.of(
            Map.of("id", 3L, "companyName", "Acme", "country", country("CA", "Canada"))
        ),
        "candidateEducations", List.of(
            Map.of("id", 4L, "country", country("TR", "Türkiye"))
        ),
        "candidateCitizenships", List.of(
            Map.of("id", 5L, "hasPassport", true, "nationality", country("PK", "Pakistan"))
        ),
        "candidateVisaChecks", List.of(
            Map.of(
                "id", 6L,
                "country", country("AU", "Australia"),
                "candidateVisaJobChecks", List.of(
                    Map.of(
                        "id", 7L,
                        "occupation", occupation(999L, "Nurse"),
                        "jobOpp", Map.of("id", 77L, "country", country("NZ", "New Zealand"))
                    )
                )
            )
        ),
        "relocatedCountry", country("SE", "Sweden"),
        "partnerOccupation", occupation(222L, "Architect")
    );

    Map<String, Object> out = b.build(src);

    // spot-check a few extended/API structures
    assertMapEquals(country("PK", "Pakistan"), out.get("country"));

    var dests = listOfMaps(out.get("candidateDestinations"));
    assertMapEquals(country("DE", "Germany"), dests.get(0).get("country"));

    var occs = listOfMaps(out.get("candidateOccupations"));
    assertMapEquals(occupation(123L, "Software Engineer"), occs.get(0).get("occupation"));

    var exps = listOfMaps(out.get("candidateJobExperiences"));
    assertMapEquals(country("CA", "Canada"), exps.get(0).get("country"));

    var edus = listOfMaps(out.get("candidateEducations"));
    assertMapEquals(country("TR", "Türkiye"), edus.get(0).get("country"));

    var cits = listOfMaps(out.get("candidateCitizenships"));
    assertMapEquals(country("PK", "Pakistan"), cits.get(0).get("nationality"));

    var visaChecks = listOfMaps(out.get("candidateVisaChecks"));
    assertMapEquals(country("AU", "Australia"), visaChecks.get(0).get("country"));

    var jobChecks = listOfMaps(visaChecks.get(0).get("candidateVisaJobChecks"));
    assertMapEquals(occupation(999L, "Nurse"), jobChecks.get(0).get("occupation"));

    @SuppressWarnings("unchecked")
    Map<String, Object> jobOpp = (Map<String, Object>) jobChecks.get(0).get("jobOpp");
    assertMapEquals(country("NZ", "New Zealand"), jobOpp.get("country"));

    assertMapEquals(occupation(222L, "Architect"), out.get("partnerOccupation"));

    // We don't assert exact call counts (fragile), just that both nested selectors are used.
    verify(countryService, atLeastOnce()).selectBuilder();
    verify(occupationService, atLeastOnce()).selectBuilder();
  }

  // Helper methods

  @SuppressWarnings("unchecked")
  private static void assertMapEquals(Map<String, Object> expected, Object actual) {
    assertNotNull(actual);
    assertEquals(expected, (Map<String, Object>) actual);
  }

  @SuppressWarnings("unchecked")
  private static java.util.List<java.util.Map<String, Object>> listOfMaps(Object o) {
    assertNotNull(o);
    return (java.util.List<java.util.Map<String, Object>>) o;
  }

  private static Map<String, Object> country(String code, String name) {
    return Map.of("code", code, "name", name);
  }

  private static Map<String, Object> occupation(long id, String name) {
    return Map.of("id", id, "name", name);
  }
}
