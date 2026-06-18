/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.configuration.SalesforceRecordTypeConfig;
import org.tctalent.server.configuration.SalesforceTbbAccountsConfig;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.Opportunity.OpportunityType;
import org.tctalent.server.model.sf.OpportunityHistory;
import org.tctalent.server.request.opportunity.UpdateEmployerOpportunityRequest;
import org.tctalent.server.service.db.CandidateDependantService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class SalesforceServiceImplTest {

  private static final String ACCESS_TOKEN = "already-have-token";

  @Mock private EmailHelper emailHelper;
  @Mock private SalesforceConfig salesforceConfig;
  @Mock private SalesforceRecordTypeConfig recordTypeConfig;
  @Mock private SalesforceTbbAccountsConfig salesforceTbbAccountsConfig;
  @Mock private CandidateDependantService candidateDependantService;
  @Mock private NextStepProcessingService nextStepProcessingService;

  private SalesforceServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new SalesforceServiceImpl(
        emailHelper,
        salesforceConfig,
        recordTypeConfig,
        salesforceTbbAccountsConfig,
        candidateDependantService,
        nextStepProcessingService
    );
  }

  @Test
  void makeExternalIdCombinesCandidateNumberAndJobId() {
    assertEquals("123-SFJOB456", SalesforceServiceImpl.makeExternalId("123", "SFJOB456"));
  }

  @Test
  void generateCandidateOppNameReturnsFullNameWhenWithinSalesforceLimit() {
    Candidate candidate = mock(Candidate.class);
    User user = new User();
    user.setFirstName("John");

    SalesforceJobOpp jobOpp = mock(SalesforceJobOpp.class);

    when(candidate.getUser()).thenReturn(user);
    when(candidate.getCandidateNumber()).thenReturn("123");
    when(jobOpp.getName()).thenReturn("Software Engineer");

    assertEquals(
        "John(123)-Software Engineer",
        service.generateCandidateOppName(candidate, jobOpp)
    );
  }

  @Test
  void generateCandidateOppNameTruncatesLongNameToSalesforceLimit() {
    Candidate candidate = mock(Candidate.class);
    User user = new User();
    user.setFirstName("John");

    SalesforceJobOpp jobOpp = mock(SalesforceJobOpp.class);

    when(candidate.getUser()).thenReturn(user);
    when(candidate.getCandidateNumber()).thenReturn("123");
    when(jobOpp.getName()).thenReturn("Very long job name ".repeat(20));

    String result = service.generateCandidateOppName(candidate, jobOpp);

    assertAll(
        () -> assertEquals(120, result.length()),
        () -> assertEquals("...", result.substring(117))
    );
  }

  @Test
  void getCandidateOpportunityRecordTypeReturnsDefaultForNullCountry() {
    SalesforceJobOpp jobOpp = mock(SalesforceJobOpp.class);
    when(jobOpp.getCountry()).thenReturn(null);

    String result = ReflectionTestUtils.invokeMethod(
        service,
        "getCandidateOpportunityRecordType",
        jobOpp
    );

    assertEquals("Candidate recruitment", result);
  }

  @Test
  void getCandidateOpportunityRecordTypeReturnsDefaultForNonCanadaCountry() {
    SalesforceJobOpp jobOpp = mock(SalesforceJobOpp.class);
    Country country = mock(Country.class);

    when(country.getName()).thenReturn("Jordan");
    when(jobOpp.getCountry()).thenReturn(country);

    String result = ReflectionTestUtils.invokeMethod(
        service,
        "getCandidateOpportunityRecordType",
        jobOpp
    );

    assertEquals("Candidate recruitment", result);
  }

  @Test
  void getCandidateOpportunityRecordTypeReturnsCanadaRecordTypeForCanada() {
    SalesforceJobOpp jobOpp = mock(SalesforceJobOpp.class);
    Country country = mock(Country.class);

    when(country.getName()).thenReturn("Canada");
    when(jobOpp.getCountry()).thenReturn(country);

    String result = ReflectionTestUtils.invokeMethod(
        service,
        "getCandidateOpportunityRecordType",
        jobOpp
    );

    assertEquals("Candidate recruitment (CAN)", result);
  }

  @Test
  void processSfCaseRelocationInfoCountsDependantsAndCandidateByAgeAndGender() {
    CandidateOpportunity candidateOpportunity = mock(CandidateOpportunity.class);
    Candidate candidate = mock(Candidate.class);

    CandidateDependant boy = dependant(LocalDate.now().minusYears(5).minusDays(1), Gender.male);
    CandidateDependant girlWithUnknownDob = dependant(null, Gender.female);
    CandidateDependant man = dependant(LocalDate.now().minusYears(25).minusDays(1), Gender.male);
    CandidateDependant woman = dependant(LocalDate.now().minusYears(25).minusDays(1), Gender.female);
    CandidateDependant childOther = dependant(LocalDate.now().minusYears(8).minusDays(1), Gender.other);
    CandidateDependant adultOther = dependant(LocalDate.now().minusYears(30).minusDays(1), Gender.other);

    when(candidateOpportunity.getRelocatingDependantIds())
        .thenReturn(List.of(1L, 2L, 3L, 4L, 5L, 6L));
    when(candidateDependantService.getDependant(1L)).thenReturn(boy);
    when(candidateDependantService.getDependant(2L)).thenReturn(girlWithUnknownDob);
    when(candidateDependantService.getDependant(3L)).thenReturn(man);
    when(candidateDependantService.getDependant(4L)).thenReturn(woman);
    when(candidateDependantService.getDependant(5L)).thenReturn(childOther);
    when(candidateDependantService.getDependant(6L)).thenReturn(adultOther);
    when(candidate.getGender()).thenReturn(Gender.female);

    Map<String, Integer> result = ReflectionTestUtils.invokeMethod(
        service,
        "processSfCaseRelocationInfo",
        candidateOpportunity,
        candidate
    );

    assertAll(
        () -> assertEquals(1, result.get("relocatingBoys")),
        () -> assertEquals(1, result.get("relocatingGirls")),
        () -> assertEquals(1, result.get("relocatingChildren")),
        () -> assertEquals(1, result.get("relocatingMen")),
        () -> assertEquals(2, result.get("relocatingWomen")),
        () -> assertEquals(1, result.get("relocatingAdults"))
    );
  }

  @Test
  void processSfCaseRelocationInfoHandlesNoDependantsAndUnknownCandidateGender() {
    CandidateOpportunity candidateOpportunity = mock(CandidateOpportunity.class);
    Candidate candidate = mock(Candidate.class);

    when(candidateOpportunity.getRelocatingDependantIds()).thenReturn(null);
    when(candidate.getGender()).thenReturn(null);

    Map<String, Integer> result = ReflectionTestUtils.invokeMethod(
        service,
        "processSfCaseRelocationInfo",
        candidateOpportunity,
        candidate
    );

    assertAll(
        () -> assertEquals(0, result.get("relocatingBoys")),
        () -> assertEquals(0, result.get("relocatingGirls")),
        () -> assertEquals(0, result.get("relocatingChildren")),
        () -> assertEquals(0, result.get("relocatingMen")),
        () -> assertEquals(0, result.get("relocatingWomen")),
        () -> assertEquals(1, result.get("relocatingAdults"))
    );
  }

  @Test
  void fetchCandidateOppGivenJobAndCandidateReturnsMatchingOpportunity() {
    Candidate candidate = mock(Candidate.class);
    CandidateOpportunity matchingOpp = mock(CandidateOpportunity.class);
    SalesforceJobOpp job = mock(SalesforceJobOpp.class);

    when(candidate.getId()).thenReturn(10L);
    when(matchingOpp.getCandidate()).thenReturn(candidate);
    when(job.getCandidateOpportunities()).thenReturn(Set.of(matchingOpp));

    CandidateOpportunity result = ReflectionTestUtils.invokeMethod(
        service,
        "fetchCandidateOppGivenJobAndCandidate",
        job,
        candidate
    );

    assertSame(matchingOpp, result);
  }

  @Test
  void fetchCandidateOppGivenJobAndCandidateReturnsNullWhenNoOpportunityMatches() {
    Candidate candidate = mock(Candidate.class);
    Candidate otherCandidate = mock(Candidate.class);
    CandidateOpportunity otherOpp = mock(CandidateOpportunity.class);
    SalesforceJobOpp job = mock(SalesforceJobOpp.class);

    when(candidate.getId()).thenReturn(10L);
    when(otherCandidate.getId()).thenReturn(20L);
    when(otherOpp.getCandidate()).thenReturn(otherCandidate);
    when(job.getCandidateOpportunities()).thenReturn(Set.of(otherOpp));

    CandidateOpportunity result = ReflectionTestUtils.invokeMethod(
        service,
        "fetchCandidateOppGivenJobAndCandidate",
        job,
        candidate
    );

    assertNull(result);
  }

  @Test
  void employerOpportunityRequestCopiesFieldsFromUpdateRequest() {
    UpdateEmployerOpportunityRequest request = new UpdateEmployerOpportunityRequest();
    request.setFolderlink("https://drive.example/root");
    request.setFolderjdlink("https://drive.example/jd");
    request.setListlink("https://tc.example/list");
    request.setJobId(123L);

    SalesforceServiceImpl.EmployerOpportunityRequest result =
        service.new EmployerOpportunityRequest(request);

    assertAll(
        () -> assertEquals("https://drive.example/root", result.CVs_Folder__c),
        () -> assertEquals("https://drive.example/jd", result.Job_Description_Folder__c),
        () -> assertEquals("https://tc.example/list", result.Talent_Catalog_List__c),
        () -> assertEquals(123L, result.TCid__c)
    );
  }

  @Test
  void employerOppStageUpdateRequestAddsOnlyNonNullFields() {
    SalesforceServiceImpl.EmployerOppStageUpdateRequest result =
        service.new EmployerOppStageUpdateRequest(
            JobOpportunityStage.cvReview,
            "Follow up with employer",
            LocalDate.of(2026, 1, 15)
        );

    assertAll(
        () -> assertEquals("CV review", result.get("StageName")),
        () -> assertEquals("Follow up with employer", result.get("NextStep")),
        () -> assertEquals("2026-01-15", result.get("Next_Step_Due_Date__c"))
    );
  }

  @Test
  void employerOppStageUpdateRequestAllowsAllNullFields() {
    SalesforceServiceImpl.EmployerOppStageUpdateRequest result =
        service.new EmployerOppStageUpdateRequest(null, null, null);

    assertEquals(0, result.size());
  }

  @Test
  void employerOppNameUpdateRequestSetsName() {
    SalesforceServiceImpl.EmployerOppNameUpdateRequest result =
        service.new EmployerOppNameUpdateRequest("New job name");

    assertEquals("New job name", result.get("Name"));
  }

  @Test
  void recordTypeFieldSetsName() {
    SalesforceServiceImpl.RecordTypeField result = service.new RecordTypeField("Employer job");

    assertEquals("Employer job", result.Name);
  }

  @Test
  void compositeAttributesSetsType() {
    SalesforceServiceImpl.CompositeAttributes result =
        new SalesforceServiceImpl.CompositeAttributes("Contact");

    assertEquals("Contact", result.getType());
  }

  @Test
  void contactRequestCompositeCheckSizeReturnsRecordCount() {
    SalesforceServiceImpl.ContactRequestComposite composite = service.new ContactRequestComposite();

    assertEquals(0, composite.checkSize());
  }

  @Test
  void opportunityRequestCompositeCheckSizeReturnsRecordCount() {
    SalesforceServiceImpl.OpportunityRequestComposite composite =
        service.new OpportunityRequestComposite();

    assertEquals(0, composite.checkSize());
  }

  @Test
  void errorRecordToStringAndCreateRecordResultErrorMessageIncludeErrors() {
    SalesforceServiceImpl.ErrorRecord error = new SalesforceServiceImpl.ErrorRecord();
    error.setStatusCode("REQUIRED_FIELD_MISSING");
    error.setMessage("Name is required");
    error.setFields(List.of("Name"));

    SalesforceServiceImpl.CreateRecordResult result = new SalesforceServiceImpl.CreateRecordResult();
    result.errors = List.of(error);

    assertEquals(
        "SalesforceServiceImpl.ErrorRecord(statusCode=REQUIRED_FIELD_MISSING, "
            + "message=Name is required, fields=[Name])\n",
        result.getErrorMessage()
    );
  }

  @Test
  void findContactsReturnsRecordsFromSalesforceQuery() throws Exception {
    String body = """
        {
          "totalSize": 1,
          "done": true,
          "records": [
            {
              "Id": "003CONTACT",
              "AccountId": "001ACCOUNT",
              "TBBid__c": 123
            }
          ]
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Contact> contacts = httpService.findContacts("TBBid__c > 0");

      assertAll(
          () -> assertEquals(1, contacts.size()),
          () -> assertEquals("003CONTACT", contacts.get(0).getId()),
          () -> assertEquals("001ACCOUNT", contacts.get(0).getAccountId()),
          () -> assertEquals(123L, contacts.get(0).getTbbId()),
          () -> assertTrue(server.decodedRequestUri().contains("FROM Contact WHERE TBBid__c > 0"))
      );
    }
  }

  @Test
  void findCandidateContactsDelegatesToFindContactsCondition() throws Exception {
    String body = """
        {
          "totalSize": 1,
          "done": true,
          "records": [
            {
              "Id": "003CONTACT",
              "TBBid__c": 456
            }
          ]
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Contact> contacts = httpService.findCandidateContacts();

      assertAll(
          () -> assertEquals(1, contacts.size()),
          () -> assertEquals("003CONTACT", contacts.get(0).getId()),
          () -> assertTrue(server.decodedRequestUri().contains("FROM Contact WHERE TBBid__c > 0"))
      );
    }
  }

  @Test
  void findContactSendsDuplicateAlertOnlyOnce() throws Exception {
    String body = """
        {
          "totalSize": 2,
          "done": true,
          "records": [
            {
              "Id": "003FIRST",
              "TBBid__c": 123
            },
            {
              "Id": "003SECOND",
              "TBBid__c": 123
            }
          ]
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);
      Candidate candidate = mock(Candidate.class);
      when(candidate.getCandidateNumber()).thenReturn("123");

      Contact firstResult = httpService.findContact(candidate);
      Contact secondResult = httpService.findContact(candidate);

      assertAll(
          () -> assertEquals("003FIRST", firstResult.getId()),
          () -> assertEquals("003FIRST", secondResult.getId())
      );
      verify(emailHelper).sendAlert(
          "Candidate number 123 has more than one Contact record on Salesforce");
    }
  }

  @Test
  void findCandidateOpportunitiesWithConditionAndLimitReturnsRecords() throws Exception {
    String body = opportunityQueryResponse("006OPP", "Candidate Opportunity");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities =
          httpService.findCandidateOpportunities("StageName = 'Prospect'", 5);

      String uri = server.decodedRequestUri();
      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006OPP", opportunities.get(0).getId()),
          () -> assertEquals("Candidate Opportunity", opportunities.get(0).getName()),
          () -> assertTrue(uri.contains("Candidate_TC_id__c > '0'")),
          () -> assertTrue(uri.contains("AND StageName = 'Prospect'")),
          () -> assertTrue(uri.contains("LIMIT 5"))
      );
    }
  }

  @Test
  void findCandidateOpportunityReturnsNullWhenNoRecordsExist() throws Exception {
    String body = """
        {
          "totalSize": 0,
          "done": true,
          "records": []
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      Opportunity result = httpService.findCandidateOpportunity("123", "006JOB");

      assertAll(
          () -> assertNull(result),
          () -> assertTrue(server.decodedRequestUri().contains("TBBCandidateExternalId__c='123-006JOB'"))
      );
    }
  }

  @Test
  void findCandidateOpportunityReturnsFirstRecordWhenMultipleExist() throws Exception {
    String body = """
        {
          "totalSize": 2,
          "done": true,
          "records": [
            {
              "Id": "006FIRST",
              "Name": "First Candidate Opportunity",
              "TBBCandidateExternalId__c": "123-006JOB"
            },
            {
              "Id": "006SECOND",
              "Name": "Second Candidate Opportunity",
              "TBBCandidateExternalId__c": "123-006JOB"
            }
          ]
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      Opportunity result = httpService.findCandidateOpportunity("123", "006JOB");

      assertAll(
          () -> assertEquals("006FIRST", result.getId()),
          () -> assertEquals("First Candidate Opportunity", result.getName())
      );
    }
  }

  @Test
  void fetchOpportunitiesByOpenOnSfForJobBuildsJobRecordTypeQuery() throws Exception {
    when(recordTypeConfig.getEmployerJob()).thenReturn("rt-employer-job");
    when(salesforceConfig.getDaysAgoRecent()).thenReturn(45);

    String body = opportunityQueryResponse("006JOB", "Open Job Opportunity");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities = httpService.fetchOpportunitiesByOpenOnSF(
          OpportunityType.JOB);

      String uri = server.decodedRequestUri();
      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006JOB", opportunities.get(0).getId()),
          () -> assertTrue(uri.contains("RecordTypeId = 'rt-employer-job'")),
          () -> assertTrue(uri.contains("LastStageChangeDate > N_DAYS_AGO:45"))
      );
    }
  }

  @Test
  void fetchOpportunitiesByOpenOnSfForCandidateBuildsCandidateRecordTypeQuery() throws Exception {
    when(recordTypeConfig.getCandidateRecruitment()).thenReturn("rt-candidate");
    when(recordTypeConfig.getCandidateRecruitmentCan()).thenReturn("rt-candidate-can");
    when(salesforceConfig.getDaysAgoRecent()).thenReturn(45);

    String body = opportunityQueryResponse("006CANDIDATE", "Open Candidate Opportunity");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities = httpService.fetchOpportunitiesByOpenOnSF(
          OpportunityType.CANDIDATE);

      String uri = server.decodedRequestUri();
      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006CANDIDATE", opportunities.get(0).getId()),
          () -> assertTrue(uri.contains("RecordTypeId = 'rt-candidate'")),
          () -> assertTrue(uri.contains("OR RecordTypeId = 'rt-candidate-can'"))
      );
    }
  }

  @Test
  void fetchOpportunitiesByIdForJobBuildsIdAndJobRecordTypeQuery() throws Exception {
    when(recordTypeConfig.getEmployerJob()).thenReturn("rt-employer-job");

    String body = opportunityQueryResponse("006JOB", "Job By Id");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities =
          httpService.fetchOpportunitiesById(List.of("006JOB", "006JOB2"), OpportunityType.JOB);

      String uri = server.decodedRequestUri();
      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006JOB", opportunities.get(0).getId()),
          () -> assertTrue(uri.contains("Id IN ('006JOB','006JOB2')")),
          () -> assertTrue(uri.contains("RecordTypeId = 'rt-employer-job'"))
      );
    }
  }

  @Test
  void fetchOpportunitiesByIdForCandidateBuildsIdAndCandidateRecordTypeQuery() throws Exception {
    when(recordTypeConfig.getCandidateRecruitment()).thenReturn("rt-candidate");
    when(recordTypeConfig.getCandidateRecruitmentCan()).thenReturn("rt-candidate-can");

    String body = opportunityQueryResponse("006CANDIDATE", "Candidate By Id");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities = httpService.fetchOpportunitiesById(
          List.of("006CANDIDATE"), OpportunityType.CANDIDATE);

      String uri = server.decodedRequestUri();
      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006CANDIDATE", opportunities.get(0).getId()),
          () -> assertTrue(uri.contains("Id IN ('006CANDIDATE')")),
          () -> assertTrue(uri.contains("RecordTypeId = 'rt-candidate'")),
          () -> assertTrue(uri.contains("OR RecordTypeId = 'rt-candidate-can'"))
      );
    }
  }

  @Test
  void findCandidateOpportunitiesByJobOppsReturnsEmptyListWhenNoIdsProvided() {
    assertTrue(service.findCandidateOpportunitiesByJobOpps().isEmpty());
  }

  @Test
  void findCandidateOpportunitiesByJobOppsReturnsRecordsWhenIdsProvided() throws Exception {
    String body = opportunityQueryResponse("006CANDIDATE", "Candidate For Job");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities =
          httpService.findCandidateOpportunitiesByJobOpps("006JOB", "006JOB2");

      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006CANDIDATE", opportunities.get(0).getId()),
          () -> assertTrue(server.decodedRequestUri()
              .contains("Parent_Opportunity__c IN ('006JOB','006JOB2')"))
      );
    }
  }

  @Test
  void findJobOpportunitiesReturnsRecords() throws Exception {
    String body = opportunityQueryResponse("006JOB", "Job Opportunity");

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<Opportunity> opportunities = httpService.findJobOpportunities();

      assertAll(
          () -> assertEquals(1, opportunities.size()),
          () -> assertEquals("006JOB", opportunities.get(0).getId()),
          () -> assertTrue(server.decodedRequestUri().contains("RecordType.Name='Employer job'"))
      );
    }
  }

  @Test
  void findOpportunityHistoriesReturnsRecords() throws Exception {
    String body = """
        {
          "totalSize": 1,
          "done": true,
          "records": [
            {
              "Id": "008HISTORY",
              "OpportunityId": "006JOB",
              "StageName": "Prospect",
              "SystemModstamp": "2026-01-01T00:00:00.000+0000"
            }
          ]
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      List<OpportunityHistory> histories =
          httpService.findOpportunityHistories(List.of("006JOB", "006JOB2"));

      assertAll(
          () -> assertEquals(1, histories.size()),
          () -> assertEquals("008HISTORY", histories.get(0).getId()),
          () -> assertEquals("006JOB", histories.get(0).getOpportunityId()),
          () -> assertEquals("Prospect", histories.get(0).getStageName()),
          () -> assertTrue(server.decodedRequestUri().contains("OpportunityId IN ('006JOB','006JOB2')"))
      );
    }
  }

  @Test
  void fetchJobOpportunityGetsRecordFields() throws Exception {
    String body = """
        {
          "Id": "006JOB",
          "Name": "Fetched Job",
          "AccountId": "001ACCOUNT",
          "StageName": "Prospect"
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      Opportunity opportunity = httpService.fetchJobOpportunity("006JOB");

      assertAll(
          () -> assertEquals("006JOB", opportunity.getId()),
          () -> assertEquals("Fetched Job", opportunity.getName()),
          () -> assertTrue(server.decodedRequestUri().contains(
              "/services/data/v58.0/sobjects/Opportunity/006JOB/"))
      );
    }
  }

  @Test
  void findAccountReturnsNullWhenIdIsNull() {
    assertNull(service.findAccount(null));
  }

  @Test
  void findOpportunityReturnsNullWhenIdIsNull() {
    assertNull(service.findOpportunity(null));
  }

  @Test
  void updateEmployerOpportunityNameSendsPatchToOpportunity() throws Exception {
    try (TestSalesforceServer server = salesforceServer(204, null)) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      httpService.updateEmployerOpportunityName("006JOB", "Updated job name");

      assertAll(
          () -> assertEquals("PATCH", server.method()),
          () -> assertTrue(server.decodedRequestUri().contains(
              "/services/data/v58.0/sobjects/Opportunity/006JOB")),
          () -> assertTrue(server.requestBody().contains("Updated job name"))
      );
    }
  }

  @Test
  void createOrUpdateContactReturnsContactWithSalesforceId() throws Exception {
    when(salesforceTbbAccountsConfig.getJordanAccount()).thenReturn("jordan-account");

    String body = """
        {
          "id": "003CONTACT",
          "success": true,
          "created": false,
          "errors": []
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);
      Candidate candidate = candidateForContact("123", "Jordan");

      Contact result = httpService.createOrUpdateContact(candidate);

      assertAll(
          () -> assertEquals("003CONTACT", result.getId()),
          () -> assertEquals(123L, result.getTbbId()),
          () -> assertEquals("PATCH", server.method()),
          () -> assertTrue(server.decodedRequestUri().contains(
              "/services/data/v58.0/sobjects/Contact/TBBid__c/123"))
      );
    }
  }

  @Test
  void createOrUpdateContactThrowsWhenUpsertFails() throws Exception {
    when(salesforceTbbAccountsConfig.getLebanonAccount()).thenReturn("lebanon-account");

    String body = """
        {
          "id": null,
          "success": false,
          "created": false,
          "errors": [
            {
              "statusCode": "FIELD_CUSTOM_VALIDATION_EXCEPTION",
              "message": "Bad candidate",
              "fields": ["TBBid__c"]
            }
          ]
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);
      Candidate candidate = candidateForContact("123", "Lebanon");

      SalesforceException exception = assertThrows(
          SalesforceException.class,
          () -> httpService.createOrUpdateContact(candidate)
      );

      assertAll(
          () -> assertTrue(exception.getMessage().contains("Update failed for candidate 123")),
          () -> assertTrue(exception.getMessage().contains("Bad candidate"))
      );
    }
  }

  @Test
  void createOrUpdateContactsMapsSuccessAndFailureResults() throws Exception {
    when(salesforceTbbAccountsConfig.getJordanAccount()).thenReturn("jordan-account");
    when(salesforceTbbAccountsConfig.getOtherAccount()).thenReturn("other-account");

    String body = """
        [
          {
            "id": "003FIRST",
            "success": true,
            "created": false,
            "errors": []
          },
          {
            "id": null,
            "success": false,
            "created": false,
            "errors": [
              {
                "statusCode": "ERROR",
                "message": "Second failed",
                "fields": ["Name"]
              }
            ]
          }
        ]
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);
      Candidate first = candidateForContact("123", "Jordan");
      Candidate second = candidateForContact("456", "Other");

      List<Contact> contacts = httpService.createOrUpdateContacts(List.of(first, second));

      assertAll(
          () -> assertEquals(2, contacts.size()),
          () -> assertEquals("003FIRST", contacts.get(0).getId()),
          () -> assertNull(contacts.get(1).getId()),
          () -> assertTrue(server.decodedRequestUri().contains(
              "/services/data/v58.0/composite/sobjects/Contact/TBBid__c"))
      );
    }
  }

  @Test
  void createOrUpdateContactsThrowsWhenResultCountDoesNotMatchCandidateCount() throws Exception {
    when(salesforceTbbAccountsConfig.getJordanAccount()).thenReturn("jordan-account");
    when(salesforceTbbAccountsConfig.getOtherAccount()).thenReturn("other-account");

    String body = """
        [
          {
            "id": "003ONLY",
            "success": true,
            "created": false,
            "errors": []
          }
        ]
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);
      Candidate first = candidateForContact("123", "Jordan");
      Candidate second = candidateForContact("456", "Other");

      SalesforceException exception = assertThrows(
          SalesforceException.class,
          () -> httpService.createOrUpdateContacts(List.of(first, second))
      );

      assertAll(
          () -> assertTrue(exception.getMessage().contains("Number of results (1)")),
          () -> assertTrue(exception.getMessage().contains("did not match number of candidates (2)"))
      );
    }
  }

  @Test
  void createOrUpdateJobOpportunityReturnsSalesforceId() throws Exception {
    String body = """
        {
          "id": "006JOB",
          "success": true,
          "created": false,
          "errors": []
        }
        """;

    try (TestSalesforceServer server = salesforceServer(200, body)) {
      SalesforceServiceImpl httpService = httpBackedService(server);
      SalesforceJobOpp job = jobOpportunityForUpsert();

      String result = httpService.createOrUpdateJobOpportunity(job);

      assertAll(
          () -> assertEquals("006JOB", result),
          () -> assertTrue(server.decodedRequestUri().contains(
              "/services/data/v58.0/sobjects/Opportunity/TCid__c/99"))
      );
    }
  }

  @Test
  void executeWithRetryWrapsSalesforceErrorBodyForBadRequest() throws Exception {
    try (TestSalesforceServer server = salesforceServer(
        400,
        "[{\"message\":\"Bad SOQL\",\"errorCode\":\"MALFORMED_QUERY\"}]"
    )) {
      SalesforceServiceImpl httpService = httpBackedService(server);

      SalesforceException exception = assertThrows(
          SalesforceException.class,
          () -> httpService.findContacts("bad condition")
      );

      assertAll(
          () -> assertTrue(exception.getMessage().contains("Bad SOQL")),
          () -> assertTrue(exception.getMessage().contains("MALFORMED_QUERY"))
      );
    }
  }

  @Test
  void executeCreateRejectsUnmappedClass() {
    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> ReflectionTestUtils.invokeMethod(service, "executeCreate", new Object())
    );

    assertTrue(exception.getMessage().contains("No mapping to Salesforce"));
  }

  @Test
  void executeUpdateRejectsUnmappedClass() {
    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> ReflectionTestUtils.invokeMethod(service, "executeUpdate", "001", new Object())
    );

    assertTrue(exception.getMessage().contains("No mapping to Salesforce"));
  }

  @Test
  void executeUpsertRejectsUnmappedClass() {
    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> ReflectionTestUtils.invokeMethod(
            service,
            "executeUpsert",
            "External_Id__c",
            "123",
            new Object()
        )
    );

    assertTrue(exception.getMessage().contains("No mapping to Salesforce"));
  }

  @Test
  void executeUpsertsRejectsMoreThanTwoHundredCompositeRecords() {
    SalesforceServiceImpl.ContactRequestComposite composite = service.new ContactRequestComposite();
    composite.setRecords(Collections.nCopies(201, null));

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> ReflectionTestUtils.invokeMethod(
            service,
            "executeUpserts",
            "TBBid__c",
            composite
        )
    );

    assertEquals(
        "Too many records (201) to update in one go. Maximum = 200.",
        exception.getMessage()
    );
  }

  private CandidateDependant dependant(LocalDate dob, Gender gender) {
    CandidateDependant dependant = mock(CandidateDependant.class);
    when(dependant.getDob()).thenReturn(dob);
    when(dependant.getGender()).thenReturn(gender);
    return dependant;
  }

  private SalesforceServiceImpl httpBackedService(TestSalesforceServer server) {
    when(salesforceConfig.getBaseClassicUrl()).thenReturn(server.baseUrl());

    service = new SalesforceServiceImpl(
        emailHelper,
        salesforceConfig,
        recordTypeConfig,
        salesforceTbbAccountsConfig,
        candidateDependantService,
        nextStepProcessingService
    );
    ReflectionTestUtils.setField(service, "accessToken", ACCESS_TOKEN);

    return service;
  }

  private TestSalesforceServer salesforceServer(int status, String body) throws IOException {
    TestSalesforceServer server = new TestSalesforceServer(status, body);
    server.start();
    return server;
  }

  private String opportunityQueryResponse(String id, String name) {
    return """
        {
          "totalSize": 1,
          "done": true,
          "records": [
            {
              "Id": "%s",
              "Name": "%s",
              "AccountId": "001ACCOUNT",
              "Candidate_TC_id__c": "123",
              "StageName": "Prospect",
              "TBBCandidateExternalId__c": "123-006JOB"
            }
          ]
        }
        """.formatted(id, name);
  }

  private Candidate candidateForContact(String candidateNumber, String countryName) {
    Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);

    User user = new User();
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john.doe@example.com");

    Country country = mock(Country.class);
    when(country.getName()).thenReturn(countryName);

    when(candidate.getUser()).thenReturn(user);
    when(candidate.getCandidateNumber()).thenReturn(candidateNumber);
    when(candidate.getCountry()).thenReturn(country);
    when(candidate.getGender()).thenReturn(Gender.male);
    when(candidate.getTopLevelIntakeCompleted()).thenReturn("Yes");
    when(candidate.getTopLevelIntakeCompletedDate()).thenReturn("");
    when(candidate.getCreatedDate()).thenReturn(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
    when(candidate.getCandidateLanguages()).thenReturn(List.of());
    when(candidate.getCandidateOccupations()).thenReturn(List.of());
    when(candidate.getMaxEducationLevel().getName()).thenReturn("Bachelor");

    return candidate;
  }

  private SalesforceJobOpp jobOpportunityForUpsert() {
    SalesforceJobOpp job = mock(SalesforceJobOpp.class, RETURNS_DEEP_STUBS);

    when(job.getId()).thenReturn(99L);
    when(job.getName()).thenReturn("Software Engineer");
    when(job.getEmployerEntity().getSfId()).thenReturn("001EMPLOYER");
    when(job.getClosingComments()).thenReturn("Closing comments");
    when(job.getStage()).thenReturn(JobOpportunityStage.prospect);
    when(job.getNextStep()).thenReturn("Next step");
    when(job.getNextStepDueDate()).thenReturn(LocalDate.of(2026, 1, 15));
    when(job.getJobCreator()).thenReturn(null);

    return job;
  }

  private static class TestSalesforceServer implements AutoCloseable {
    private final int status;
    private final String body;
    private final AtomicReference<String> requestUri = new AtomicReference<>();
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>("");
    private final AtomicInteger requestCount = new AtomicInteger();
    private HttpServer server;

    private TestSalesforceServer(int status, String body) {
      this.status = status;
      this.body = body;
    }

    private void start() throws IOException {
      server = HttpServer.create(new InetSocketAddress(0), 0);
      server.createContext("/", this::handle);
      server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
      requestCount.incrementAndGet();
      method.set(exchange.getRequestMethod());
      requestUri.set(exchange.getRequestURI().toString());
      requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

      if (body == null) {
        exchange.sendResponseHeaders(status, -1);
      } else {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
      }

      exchange.close();
    }

    private String baseUrl() {
      return "http://localhost:" + server.getAddress().getPort() + "/";
    }

    private String decodedRequestUri() {
      return URLDecoder.decode(requestUri.get(), StandardCharsets.UTF_8);
    }

    private String method() {
      return method.get();
    }

    private String requestBody() {
      return requestBody.get();
    }

    @SuppressWarnings("unused")
    private int requestCount() {
      return requestCount.get();
    }

    @Override
    public void close() {
      server.stop(0);
    }
  }
}
