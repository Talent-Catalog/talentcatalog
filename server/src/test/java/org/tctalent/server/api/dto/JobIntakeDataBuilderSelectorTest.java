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

import lombok.Getter;
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
    // given
    var contact = new UserStub()
        .setId(9L).setFirstName("Alice").setLastName("Smith").setEmail("alice@smith.com");

    var country = new CountryStub()
        .setCode("PK").setName("Pakistan");

    var jobOpp = new JobOppStub()
        .setId(100L).setSfId("SF-100").setContactUser(contact)
        .setCountryObject(country).setCreatedDate("2024-01-01T00:00:00Z")
        .setEmployer("Acme").setName("Senior Dev");

    var joi = new JobIntakeDataStub()
        .setId(1L)
        .setJobOpp(jobOpp)
        .setSalaryRange("50-60k")
        .setRecruitmentProcess("2 rounds")
        .setEmployerCostCommitment("Relocation support")
        .setLocation("Toronto")
        .setLocationDetails("Hybrid")
        .setBenefits("Health, Dental")
        .setLanguageRequirements("EN B2")
        .setEducationRequirements("BSc CS")
        .setSkillRequirements("Java, Spring")
        .setEmploymentExperience("5y+")
        .setOccupationCode("2512");

    // when
    Map<String, Object> out = selector.selectBuilder().build(joi);

    // then (top-level)
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
    // given: contactUser is null; country builder still used
    var jobOpp = new JobOppStub()
        .setId(200L).setSfId("SF-200")
        .setContactUser(null)
        .setCountryObject(new CountryStub().setCode("CA").setName("Canada"));

    var joi = new JobIntakeDataStub()
        .setId(2L)
        .setJobOpp(jobOpp);

    // when
    Map<String, Object> out = selector.selectBuilder().build(joi);

    // then
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

  // Minimal stubs

  @Getter
  public static class JobIntakeDataStub {
    private Long id;
    private JobOppStub jobOpp;
    private String salaryRange, recruitmentProcess, employerCostCommitment;
    private String location, locationDetails, benefits, languageRequirements;
    private String educationRequirements, skillRequirements, employmentExperience, occupationCode;

    public JobIntakeDataStub setId(Long v) { id = v; return this; }
    public JobIntakeDataStub setJobOpp(JobOppStub v) { jobOpp = v; return this; }
    public JobIntakeDataStub setSalaryRange(String v) { salaryRange = v; return this; }
    public JobIntakeDataStub setRecruitmentProcess(String v) { recruitmentProcess = v; return this; }
    public JobIntakeDataStub setEmployerCostCommitment(String v) { employerCostCommitment = v; return this; }
    public JobIntakeDataStub setLocation(String v) { location = v; return this; }
    public JobIntakeDataStub setLocationDetails(String v) { locationDetails = v; return this; }
    public JobIntakeDataStub setBenefits(String v) { benefits = v; return this; }
    public JobIntakeDataStub setLanguageRequirements(String v) { languageRequirements = v; return this; }
    public JobIntakeDataStub setEducationRequirements(String v) { educationRequirements = v; return this; }
    public JobIntakeDataStub setSkillRequirements(String v) { skillRequirements = v; return this; }
    public JobIntakeDataStub setEmploymentExperience(String v) { employmentExperience = v; return this; }
    public JobIntakeDataStub setOccupationCode(String v) { occupationCode = v; return this; }
  }

  @Getter
  public static class JobOppStub {
    private Long id; private String sfId;
    private UserStub contactUser;
    private CountryStub countryObject;
    private String createdDate, employer, name;

    public JobOppStub setId(Long v) { id = v; return this; }
    public JobOppStub setSfId(String v) { sfId = v; return this; }
    public JobOppStub setContactUser(UserStub v) { contactUser = v; return this; }
    public JobOppStub setCountryObject(CountryStub v) { countryObject = v; return this; }
    public JobOppStub setCreatedDate(String v) { createdDate = v; return this; }
    public JobOppStub setEmployer(String v) { employer = v; return this; }
    public JobOppStub setName(String v) { name = v; return this; }
  }

  @Getter
  public static class UserStub {
    private Long id; private String firstName; private String lastName; private String email;

    public UserStub setId(Long v) { id = v; return this; }
    public UserStub setFirstName(String v) { firstName = v; return this; }
    public UserStub setLastName(String v) { lastName = v; return this; }
    public UserStub setEmail(String v) { email = v; return this; }
  }

  @Getter
  public static class CountryStub {
    private String code; private String name;

    public CountryStub setCode(String v) { code = v; return this; }
    public CountryStub setName(String v) { name = v; return this; }
  }
}
