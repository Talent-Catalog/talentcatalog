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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.tctalent.server.util.http.RecordingHttpServer.empty;
import static org.tctalent.server.util.http.RecordingHttpServer.json;

import java.security.KeyPairGenerator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.configuration.SalesforceRecordTypeConfig;
import org.tctalent.server.configuration.SalesforceTbbAccountsConfig;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.OpportunityHistory;
import org.tctalent.server.request.opportunity.UpdateEmployerOpportunityRequest;
import org.tctalent.server.service.db.CandidateDependantService;
import org.tctalent.server.service.db.NextStepProcessingService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.http.RecordingHttpServer;
import org.tctalent.server.util.http.RecordingHttpServer.RecordedRequest;

/**
 * Regression tests for the exact JSON exchanged with Salesforce.
 * <p>
 * SalesforceServiceImpl builds its WebClient with Spring's default codecs, so these tests run the
 * real service against a real local HTTP server and compare the complete request bodies - field
 * names, nulls and nesting - rather than fragments. They also verify decoding of Salesforce's
 * PascalCase / custom field responses into our model classes.
 * </p>
 * <p>
 * The tests make no use of any JSON library API, so they verify behavior independently of the
 * Jackson version. Added before the Spring Boot 4 (Jackson 3) migration - TC-1189.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class SalesforceJsonPayloadTest {

    private static final String ACCESS_TOKEN = "already-have-token";

    @Mock private EmailHelper emailHelper;
    @Mock private SalesforceConfig salesforceConfig;
    @Mock private SalesforceRecordTypeConfig recordTypeConfig;
    @Mock private SalesforceTbbAccountsConfig salesforceTbbAccountsConfig;
    @Mock private CandidateDependantService candidateDependantService;
    @Mock private NextStepProcessingService nextStepProcessingService;

    @BeforeEach
    void setUp() {
        lenient().when(salesforceTbbAccountsConfig.getJordanAccount()).thenReturn("001JORDAN");
        lenient().when(salesforceTbbAccountsConfig.getOtherAccount()).thenReturn("001OTHER");
    }

    // ------------------------------------------------------------------------------------------
    // Request bodies
    // ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("contact upsert sends only the populated contact fields")
    void contactUpsertRequest() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            {"id": "003CONTACT", "success": true, "created": false, "errors": []}
            """))) {
            Candidate candidate = candidate("123", "Jordan");

            service(server).createOrUpdateContact(candidate);

            RecordedRequest request = server.lastRequest();
            assertJsonRequest(request, "PATCH", "/sobjects/Contact/TBBid__c/123");
            //TBBid__c is not in the body: it is the upsert key in the uri.
            //Nationality__c is an explicit null.
            assertJson("""
                {
                  "FirstName": "John",
                  "LastName": "Doe",
                  "Contact_Type__c": "Candidate",
                  "RecordTypeId": "012Uu000000093JIAQ",
                  "Email": "john.doe@example.com",
                  "Gender__c": "male",
                  "MailingCountry": "Jordan",
                  "Nationality__c": null,
                  "AccountId": "001JORDAN",
                  "Intaked__c": "Yes",
                  "Date_of_Birth__c": "1990-05-06",
                  "TC_Status__c": "active",
                  "TC_Account_Created__c": "2026-01-01",
                  "UNHCR_Registered__c": "Yes",
                  "Language_s_Spoken__c": "",
                  "Highest_Educational_Attainment__c": "Bachelor",
                  "Occupation_s__c": "",
                  "TC_Contact_Consent__c": true,
                  "Partner_Contact_Consent__c": false
                }
                """, request.body());
        }
    }

    @Test
    @DisplayName("contact composite upsert sends attributes first in each record")
    void contactCompositeUpsertRequest() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            [
              {"id": "003FIRST", "success": true, "created": true, "errors": []},
              {"id": "003SECOND", "success": true, "created": false, "errors": []}
            ]
            """))) {

            service(server).createOrUpdateContacts(
                List.of(candidate("123", "Jordan"), candidate("456", "Other")));

            RecordedRequest request = server.lastRequest();
            assertJsonRequest(request, "PATCH", "/composite/sobjects/Contact/TBBid__c");
            assertJson("""
                {
                  "allOrNone": false,
                  "records": [
                    {
                      "attributes": {"type": "Contact"},
                      "FirstName": "John",
                      "LastName": "Doe",
                      "Contact_Type__c": "Candidate",
                      "RecordTypeId": "012Uu000000093JIAQ",
                      "Email": "john.doe@example.com",
                      "Gender__c": "male",
                      "MailingCountry": "Jordan",
                      "Nationality__c": null,
                      "AccountId": "001JORDAN",
                      "TBBid__c": 123,
                      "Intaked__c": "Yes",
                      "Date_of_Birth__c": "1990-05-06",
                      "TC_Status__c": "active",
                      "TC_Account_Created__c": "2026-01-01",
                      "UNHCR_Registered__c": "Yes",
                      "Language_s_Spoken__c": "",
                      "Highest_Educational_Attainment__c": "Bachelor",
                      "Occupation_s__c": "",
                      "TC_Contact_Consent__c": true,
                      "Partner_Contact_Consent__c": false
                    },
                    {
                      "attributes": {"type": "Contact"},
                      "FirstName": "John",
                      "LastName": "Doe",
                      "Contact_Type__c": "Candidate",
                      "RecordTypeId": "012Uu000000093JIAQ",
                      "Email": "john.doe@example.com",
                      "Gender__c": "male",
                      "MailingCountry": "Other",
                      "Nationality__c": null,
                      "AccountId": "001OTHER",
                      "TBBid__c": 456,
                      "Intaked__c": "Yes",
                      "Date_of_Birth__c": "1990-05-06",
                      "TC_Status__c": "active",
                      "TC_Account_Created__c": "2026-01-01",
                      "UNHCR_Registered__c": "Yes",
                      "Language_s_Spoken__c": "",
                      "Highest_Educational_Attainment__c": "Bachelor",
                      "Occupation_s__c": "",
                      "TC_Contact_Consent__c": true,
                      "Partner_Contact_Consent__c": false
                    }
                  ]
                }
                """, request.body());
            assertAttributesFirstInEachRecord(request.body());
        }
    }

    @Test
    @DisplayName("job opportunity upsert sends the job opportunity fields")
    void jobOpportunityUpsertRequest() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            {"id": "006JOB", "success": true, "created": false, "errors": []}
            """))) {

            service(server).createOrUpdateJobOpportunity(jobOpportunity());

            RecordedRequest request = server.lastRequest();
            assertJsonRequest(request, "PATCH", "/sobjects/Opportunity/TCid__c/99");
            //RecordTypeField has a public Name field as well as a Lombok getName() getter. Jackson 2
            //sent both "Name" and "name". Jackson 3 sends just "Name" - TC-1189 (D17).
            assertJson("""
                {
                  "RecordType": {"Name": "Employer job"},
                  "Name": "Software Engineer",
                  "AccountId": "001EMPLOYER",
                  "Closing_Comments__c": "Closing comments",
                  "StageName": "Prospect",
                  "NextStep": "Next step",
                  "Next_Step_Due_Date__c": "2026-01-15",
                  "CloseDate": "%s"
                }
                """.formatted(LocalDate.now().plusYears(1)), request.body());
        }
    }

    @Test
    @DisplayName("employer opportunity update sends the Salesforce custom field names, including nulls")
    void employerOpportunityUpdateRequest() throws Exception {
        UpdateEmployerOpportunityRequest update = new UpdateEmployerOpportunityRequest();
        update.setSfJoblink("https://talentbeyondboundaries.lightning.force.com/lightning/r/"
            + "Opportunity/006123456789ABC/view/");
        update.setFolderlink("https://drive.example/root");
        update.setFolderjdlink(null);
        update.setListlink("https://tc.example/list");
        update.setJobId(99L);

        try (RecordingHttpServer server = RecordingHttpServer.start(empty(204))) {

            service(server).updateEmployerOpportunity(update);

            RecordedRequest request = server.lastRequest();
            assertJsonRequest(request, "PATCH", "/sobjects/Opportunity/006123456789ABC");
            //Each field has a public field as well as a Lombok getter (eg getCVs_Folder__c()).
            //Jackson 2 sent each field twice, the second time with a mangled lower case name
            //(eg cvs_Folder__c). Jackson 3 sends each field once, with its Salesforce
            //name - TC-1189 (D17).
            assertJson("""
                {
                  "CVs_Folder__c": "https://drive.example/root",
                  "Job_Description_Folder__c": null,
                  "Talent_Catalog_List__c": "https://tc.example/list",
                  "TCid__c": 99
                }
                """, request.body());
        }
    }

    @Test
    @DisplayName("employer opportunity stage update omits fields which are not supplied")
    void employerOpportunityStageUpdateRequest() throws Exception {
        SalesforceJobOpp job = mock(SalesforceJobOpp.class);
        when(job.getSfId()).thenReturn("006JOB");

        try (RecordingHttpServer server = RecordingHttpServer.start(empty(204))) {

            service(server).updateEmployerOpportunityStage(
                job, JobOpportunityStage.prospect, null, LocalDate.of(2026, 3, 1));

            RecordedRequest request = server.lastRequest();
            assertJsonRequest(request, "PATCH", "/sobjects/Opportunity/006JOB");
            assertJson("""
                {"StageName": "Prospect", "Next_Step_Due_Date__c": "2026-03-01"}
                """, request.body());
        }
    }

    @Test
    @DisplayName("candidate opportunity composite upsert sends attributes first in each record")
    void candidateOpportunityCompositeUpsertRequest() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(
            json(200, """
                {"totalSize": 0, "done": true, "records": []}
                """),
            json(200, """
                [{"id": "006CANDIDATE", "success": true, "created": true, "errors": []}]
                """))) {
            Candidate candidate = mock(Candidate.class);
            User user = new User();
            user.setFirstName("Jane");
            when(candidate.getCandidateNumber()).thenReturn("123");
            when(candidate.getUser()).thenReturn(user);
            when(candidate.getSfId()).thenReturn("003CONTACT");

            SalesforceJobOpp job = mock(SalesforceJobOpp.class);
            when(job.getId()).thenReturn(99L);
            when(job.getSfId()).thenReturn("006JOB");
            when(job.getName()).thenReturn("Developer");
            when(job.getAccountId()).thenReturn("001EMPLOYER");
            when(job.getCandidateOpportunities()).thenReturn(Set.of());
            when(nextStepProcessingService.processNextStep(
                null, "Contact candidate and do intake")).thenReturn("Do intake");

            service(server).createOrUpdateCandidateOpportunities(List.of(candidate), null, job);

            RecordedRequest request = server.lastRequest();
            assertJsonRequest(request, "PATCH",
                "/composite/sobjects/Opportunity/TBBCandidateExternalId__c");
            assertJson("""
                {
                  "allOrNone": false,
                  "records": [
                    {
                      "attributes": {"type": "Opportunity"},
                      "RecordType": {"Name": "Candidate recruitment"},
                      "TBBCandidateExternalId__c": "123-006JOB",
                      "Name": "Jane(123)-Developer",
                      "AccountId": "001EMPLOYER",
                      "Candidate_Contact__c": "003CONTACT",
                      "Parent_Opportunity__c": "006JOB",
                      "CloseDate": "%s",
                      "StageName": "Prospect",
                      "NextStep": "Do intake",
                      "Next_Step_Due_Date__c": "%s"
                    }
                  ]
                }
                """.formatted(LocalDate.now().plusYears(1), LocalDate.now().plusWeeks(2)),
                request.body());
            assertAttributesFirstInEachRecord(request.body());
        }
    }

    // ------------------------------------------------------------------------------------------
    // Response decoding
    // ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("bearer token response is decoded and used for the following request")
    void bearerTokenResponse() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(
            json(200, """
                {
                  "access_token": "new-access-token",
                  "scope": "api",
                  "instance_url": "http://localhost",
                  "id": "identity",
                  "token_type": "Bearer"
                }
                """),
            json(200, """
                {"totalSize": 0, "done": true, "records": []}
                """))) {
            when(salesforceConfig.getBaseClassicUrl()).thenReturn(server.baseUrl());
            when(salesforceConfig.getBaseLoginUrl()).thenReturn(server.baseUrl());
            when(salesforceConfig.getPrivatekey()).thenReturn(privateKeyPem());
            when(salesforceConfig.getConsumerKey()).thenReturn("consumer-key");
            when(salesforceConfig.getUser()).thenReturn("salesforce-user");
            SalesforceServiceImpl service = newService();
            service.afterPropertiesSet();

            service.findContacts("TBBid__c > 0");

            assertEquals("new-access-token", ReflectionTestUtils.getField(service, "accessToken"));
        }
    }

    @Test
    @DisplayName("contact query result is decoded from Salesforce field names, ignoring unknown fields")
    void contactQueryResponse() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            {
              "totalSize": 2,
              "done": true,
              "records": [
                {
                  "attributes": {"type": "Contact", "url": "/services/data/v58.0/sobjects/Contact/003A"},
                  "Id": "003A",
                  "AccountId": "001ACCOUNT",
                  "TBBid__c": 123
                },
                {
                  "attributes": {"type": "Contact"},
                  "Id": "003B",
                  "AccountId": null,
                  "TBBid__c": null
                }
              ]
            }
            """))) {

            List<Contact> contacts = service(server).findContacts("TBBid__c > 0");

            assertAll(
                () -> assertEquals(2, contacts.size()),
                () -> assertEquals("003A", contacts.get(0).getId()),
                () -> assertEquals("001ACCOUNT", contacts.get(0).getAccountId()),
                () -> assertEquals(123L, contacts.get(0).getTbbId()),
                () -> assertEquals("003B", contacts.get(1).getId()),
                () -> assertNull(contacts.get(1).getAccountId()),
                () -> assertNull(contacts.get(1).getTbbId())
            );
        }
    }

    @Test
    @DisplayName("opportunity is decoded with booleans, numbers, nulls and custom field names")
    void opportunityResponse() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            {
              "attributes": {"type": "Opportunity"},
              "Id": "006JOB",
              "Name": "Fetched Job",
              "AccountId": "001ACCOUNT",
              "Candidate_TC_id__c": "123",
              "Closing_Comments__c": "Closed",
              "Closing_Comments_For_Candidate__c": null,
              "Employer_Feedback__c": "Good",
              "IsClosed": true,
              "IsWon": false,
              "CreatedDate": "2026-01-02T03:04:05.000+0000",
              "LastModifiedDate": "2026-02-03T04:05:06.000+0000",
              "NextStep": "Call",
              "Next_Step_Due_Date__c": "2026-03-01",
              "OwnerId": "005OWNER",
              "Opportunity_Score__c": "High",
              "Parent_Opportunity__c": "006PARENT",
              "RecordTypeId": "012RECORD",
              "StageName": "Prospect",
              "TBBCandidateExternalId__c": "123-006JOB",
              "Hiring_Commitment__c": 3
            }
            """))) {

            Opportunity opp = service(server).fetchJobOpportunity("006JOB");

            assertAll(
                () -> assertEquals("006JOB", opp.getId()),
                () -> assertEquals("Fetched Job", opp.getName()),
                () -> assertEquals("001ACCOUNT", opp.getAccountId()),
                () -> assertEquals("123", opp.getCandidateId()),
                () -> assertEquals("Closed", opp.getClosingComments()),
                () -> assertNull(opp.getClosingCommentsForCandidate()),
                () -> assertEquals("Good", opp.getEmployerFeedback()),
                () -> assertTrue(opp.isClosed()),
                () -> assertFalse(opp.isWon()),
                () -> assertEquals("2026-01-02T03:04:05.000+0000", opp.getCreatedDate()),
                () -> assertEquals("2026-02-03T04:05:06.000+0000", opp.getLastModifiedDate()),
                () -> assertEquals("Call", opp.getNextStep()),
                () -> assertEquals("2026-03-01", opp.getNextStepDueDate()),
                () -> assertEquals("005OWNER", opp.getOwnerId()),
                () -> assertEquals("High", opp.getOpportunityScore()),
                () -> assertEquals("006PARENT", opp.getParentOpportunityId()),
                () -> assertEquals("012RECORD", opp.getRecordTypeId()),
                () -> assertEquals("Prospect", opp.getStageName()),
                () -> assertEquals("123-006JOB", opp.getCandidateExternalId()),
                () -> assertEquals(3L, opp.getHiringCommitment())
            );
        }
    }

    @Test
    @DisplayName("account is decoded from Salesforce field names")
    void accountResponse() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            {
              "attributes": {"type": "Account"},
              "Id": "001ACCOUNT",
              "Name": "Employer",
              "Description": "An employer",
              "Website": "https://employer.example",
              "BillingCountry": "Australia",
              "Has_Hired_Internationally__c": "Yes"
            }
            """))) {

            Account account = service(server).findAccount("001ACCOUNT");

            assertAll(
                () -> assertEquals("001ACCOUNT", account.getId()),
                () -> assertEquals("Employer", account.getName()),
                () -> assertEquals("An employer", account.getDescription()),
                () -> assertEquals("https://employer.example", account.getWebsite()),
                () -> assertEquals("Australia", account.getCountry()),
                () -> assertEquals("Yes", account.getHasHiredInternationally())
            );
        }
    }

    @Test
    @DisplayName("opportunity histories are decoded from Salesforce field names")
    void opportunityHistoryResponse() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            {
              "totalSize": 1,
              "done": true,
              "records": [
                {
                  "attributes": {"type": "OpportunityHistory"},
                  "Id": "008HIST",
                  "OpportunityId": "006OPP",
                  "StageName": "Offer",
                  "SystemModstamp": "2026-04-05T06:07:08.000+0000"
                }
              ]
            }
            """))) {

            List<OpportunityHistory> histories =
                service(server).findOpportunityHistories(List.of("006OPP"));

            assertAll(
                () -> assertEquals(1, histories.size()),
                () -> assertEquals("008HIST", histories.get(0).getId()),
                () -> assertEquals("006OPP", histories.get(0).getOpportunityId()),
                () -> assertEquals("Offer", histories.get(0).getStageName()),
                () -> assertEquals("2026-04-05T06:07:08.000+0000",
                    histories.get(0).getSystemModstamp())
            );
        }
    }

    @Test
    @DisplayName("composite upsert results are decoded, including errors")
    void compositeUpsertResultsResponse() throws Exception {
        try (RecordingHttpServer server = RecordingHttpServer.start(json(200, """
            [
              {"id": "003FIRST", "success": true, "created": true, "errors": []},
              {
                "id": null,
                "success": false,
                "created": false,
                "errors": [
                  {"statusCode": "REQUIRED_FIELD_MISSING", "message": "Name is required",
                   "fields": ["LastName"]}
                ]
              }
            ]
            """))) {

            List<Contact> contacts = service(server).createOrUpdateContacts(
                List.of(candidate("123", "Jordan"), candidate("456", "Other")));

            assertAll(
                () -> assertEquals(2, contacts.size()),
                () -> assertEquals("003FIRST", contacts.get(0).getId()),
                () -> assertEquals(123L, contacts.get(0).getTbbId()),
                () -> assertNull(contacts.get(1).getId())
            );
        }
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    private SalesforceServiceImpl service(RecordingHttpServer server) {
        when(salesforceConfig.getBaseClassicUrl()).thenReturn(server.baseUrl());
        SalesforceServiceImpl service = newService();
        ReflectionTestUtils.setField(service, "accessToken", ACCESS_TOKEN);
        return service;
    }

    private SalesforceServiceImpl newService() {
        return new SalesforceServiceImpl(
            emailHelper,
            salesforceConfig,
            recordTypeConfig,
            salesforceTbbAccountsConfig,
            candidateDependantService,
            nextStepProcessingService
        );
    }

    private Candidate candidate(String candidateNumber, String countryName) {
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
        when(candidate.getDob()).thenReturn(LocalDate.of(1990, 5, 6));
        when(candidate.getStatus()).thenReturn(CandidateStatus.active);
        when(candidate.getUnhcrRegistered()).thenReturn(YesNoUnsure.Yes);
        when(candidate.getContactConsentRegistration()).thenReturn(true);
        when(candidate.getContactConsentPartners()).thenReturn(false);
        //Nationality with no name - sent as an explicit null
        when(candidate.getNationality()).thenReturn(mock(Country.class));
        when(candidate.getRelocatedAddress()).thenReturn(null);
        when(candidate.getRelocatedCity()).thenReturn(null);
        when(candidate.getRelocatedState()).thenReturn(null);
        when(candidate.getRelocatedCountry()).thenReturn(null);

        return candidate;
    }

    private SalesforceJobOpp jobOpportunity() {
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

    private static String privateKeyPem() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String encoded = Base64.getEncoder().encodeToString(
            generator.generateKeyPair().getPrivate().getEncoded());
        return "-----BEGIN PRIVATE KEY-----" + encoded + "-----END PRIVATE KEY-----";
    }

    private static void assertJsonRequest(RecordedRequest request, String method, String path) {
        assertAll(
            () -> assertEquals(method, request.method()),
            () -> assertTrue(request.decodedUri().contains(path), request.decodedUri()),
            () -> assertTrue(request.contentType() != null
                && request.contentType().startsWith("application/json"), request.contentType())
        );
    }

    /**
     * Salesforce requires the "attributes" field to be the first field of each composite record.
     */
    private static void assertAttributesFirstInEachRecord(String body) {
        String recordsStart = "\"records\":[";
        int index = body.indexOf(recordsStart);
        assertTrue(index >= 0, body);
        String records = body.substring(index + recordsStart.length());
        for (String record : records.split("\\},\\{")) {
            String start = record.startsWith("{") ? record.substring(1) : record;
            assertTrue(start.startsWith("\"attributes\":"), "Record does not start with attributes: "
                + record);
        }
    }

    /**
     * Strict comparison: same fields (no more, no fewer), same values, explicit nulls must be
     * present, same array order. Object key order is not compared.
     */
    private static void assertJson(String expected, String actual) throws Exception {
        JSONAssert.assertEquals("Actual JSON: " + actual, expected, actual, JSONCompareMode.STRICT);
    }
}
