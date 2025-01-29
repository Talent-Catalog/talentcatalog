/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.tctalent.server.configuration.SalesforceConfig;
import org.tctalent.server.configuration.SalesforceRecordTypeConfig;
import org.tctalent.server.configuration.SalesforceTbbAccountsConfig;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.model.sf.Contact;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.Opportunity.OpportunityType;
import org.tctalent.server.model.sf.OpportunityHistory;
import org.tctalent.server.request.candidate.EmployerCandidateDecision;
import org.tctalent.server.request.candidate.EmployerCandidateFeedbackData;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.opportunity.UpdateEmployerOpportunityRequest;
import org.tctalent.server.service.db.CandidateDependantService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.SalesforceHelper;
import reactor.core.publisher.Mono;

/**
 * Standard implementation of Salesforce service
 * <p/>
 * See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_what_is_rest_api.htm
 * <p/>
 *
 * @see #executeUpsert and other execute methods for other refs to Salesforce doc.
 * <p/>
 * Basically we need to construct a simple Java object containing the required SF fields which will
 * be converted to a Json object by {@link WebClient} methods and included in the body of the HTTP
 * request sent to SF. These Java objects are defined here in nested classes like: {@link
 * ContactRequest} and {@link CandidateOpportunityRequest}.
 * <p/>
 * Operating on multiple records in a single HTTP request is called a "composite" request by SF. See
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/using_composite_resources.htm
 * In this code we use sObject Collections - see https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_composite_sobject_tree_flat.htm
 * Basically this means passing an array of records in the HTTP body. Each record is basically the
 * request object plus extra attributes. For example the record in a composite contact request is a
 * {@link ContactRecordComposite}, and the multiple contact request is a {@link
 * ContactRequestComposite}, which is just an array of ContactRecordComposite (plus a couple of
 * other fields - eg allOrNone).
 * <p/>
 * Notes:
 * <ul>
 *   <li>Field names in classes used to communicate with SF need to have names that match the
 *   internal SF field name</li>
 * </ul>
 * <p/>
 * The standard Salesforce doc on this stuff is typically poor.
 * The Trailhead is worth a look - start here:
 * https://trailhead.salesforce.com/content/learn/modules/api_basics/api_basics_overview
 */
@Service
@Slf4j
public class SalesforceServiceImpl implements SalesforceService, InitializingBean {
    private static final String apiVersion = "v58.0";

    /*
     * Unique Salesforce external ID's used to determine in Salesforce "upserts" whether
     * a corresponding Salesforce record exists which should be updated, or whether a new
     * Salesforce record needs to be created.
     * @see #executeUpsert(String, String, Object)
     */
    /**
     * Unique external id for Salesforce Contact records.
     * <p/>
     * The TC candidate number is used as the unique value for this field
     */
    private static final String candidateNumberSFFieldName = "TBBid__c";

    /**
     * Unique external id for Salesforce Candidate opportunity records
     * <p/>
     * The TC candidate number plus the SF ID of the associated job opportunity  is used as
     * the unique value for this field
     */
    private static final String candidateOpportunitySFFieldName = "TBBCandidateExternalId__c";

    /**
     * Unique external id for Salesforce Job opportunity records
     * <p/>
     * The ID of the associated job opportunity record in the TC is used as the unique value for
     * this field
     */
    private static final String jobOpportunitySFFieldName = "TCid__c";


    /**
     * Value of Salesforce ContactType field, indicating that the Contact is a Talent Catalog
     * candidate.
     */
    private static final String candidateContactTypeSFFieldValue = "Candidate";
    private static final String contactRecordTypeId = "012Uu000000093JIAQ";

    private boolean alertedDuplicateSFRecord = false;

    private final Map<Class<?>, String> classSfPathMap = new HashMap<>();
    private final Map<Class<?>, String> classSfCompositePathMap = new HashMap<>();

    /**
     * If you are adding a new field to get from SF (in strings below), make sure you add the field to {@link Opportunity}.
      */
    private final String contactRetrievalFields =
        "Id,AccountId," + candidateNumberSFFieldName;

    private final String commonOpportunityFields =
        "Id,Name,AccountId,Closing_Comments__c,CreatedDate,LastModifiedDate,NextStep,Next_Step_Due_Date__c,StageName,IsClosed,IsWon";
    private final String candidateOpportunityRetrievalFields =
        commonOpportunityFields +
        ",Employer_Feedback__c,Closing_Comments_For_Candidate__c,Parent_Opportunity__c,Candidate_TC_id__c,"
            + candidateOpportunitySFFieldName;
    private final String jobOpportunityRetrievalFields =
        commonOpportunityFields +
        ",RecordTypeId,OwnerId,Hiring_Commitment__c,Opportunity_Score__c";

    private final EmailHelper emailHelper;

    private final SalesforceConfig salesforceConfig;
    private final SalesforceRecordTypeConfig salesforceRecordTypeConfig;
    private final SalesforceTbbAccountsConfig salesforceTbbAccountsConfig;
    private final CandidateDependantService candidateDependantService;

    private PrivateKey privateKey;

    private final WebClient webClient;

    /**
     * This is the accessToken required for all Salesforce REST API calls. It should appear in the
     * Authorization header, prefixed by "Bearer ".
     * <p/>
     * The token is retrieved from Salesforce by calling {@link #requestAccessToken()}
     * <p/>
     * Access tokens expire so they need to be rerequested periodically.
     */
    private String accessToken = null;

    @Autowired
    public SalesforceServiceImpl(EmailHelper emailHelper, SalesforceConfig salesforceConfig,
        SalesforceRecordTypeConfig salesforceRecordTypeConfig, SalesforceTbbAccountsConfig salesforceTbbAccountsConfig,
        CandidateDependantService candidateDependantService) {
        this.emailHelper = emailHelper;
        this.salesforceConfig = salesforceConfig;
        this.salesforceRecordTypeConfig = salesforceRecordTypeConfig;
        this.salesforceTbbAccountsConfig = salesforceTbbAccountsConfig;
        this.candidateDependantService = candidateDependantService;

        classSfPathMap.put(ContactRequest.class, "Contact");
        classSfPathMap.put(EmployerOpportunityRequest.class, "Opportunity");
        classSfPathMap.put(EmployerOppStageUpdateRequest.class, "Opportunity");
        classSfPathMap.put(JobOpportunityRequest.class, "Opportunity");
        classSfPathMap.put(EmployerOppNameUpdateRequest.class, "Opportunity");
        classSfCompositePathMap.put(ContactRequestComposite.class, "Contact");
        classSfCompositePathMap.put(OpportunityRequestComposite.class, "Opportunity");

        WebClient.Builder builder =
            WebClient.builder()
                .baseUrl(
                    salesforceConfig.getBaseClassicUrl() + "services/data/" + apiVersion)
                .defaultHeader("Content_Type", "application/json")
                .defaultHeader("Accept", "application/json");

        webClient = builder.build();
    }

    private Map<String, String> buildCandidateOppsMap(Iterable<Candidate> candidates,
        String jobOpportunityId) {
        Map<String, String> idCandidateNumberMap = new HashMap<>();
        for (Candidate candidate : candidates) {
            String id = makeExternalId(candidate.getCandidateNumber(), jobOpportunityId);
            idCandidateNumberMap.put(id, candidate.getCandidateNumber());
        }
        return idCandidateNumberMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        privateKey = privateKeyFromPkcs8(salesforceConfig.getPrivatekey());
    }

    @Override
    @NonNull
    public Contact createOrUpdateContact(@NonNull Candidate candidate)
        throws WebClientException, SalesforceException {

        //Create a contact request using data from the candidate
        ContactRequest contactRequest = new ContactRequest(null, candidate);

        //Upsert request bodies should not include the TBBid because it is used as the unique key
        //used to identify the record to be updated - specified in the PATCH uri, not in the
        //request body.
        contactRequest.remove(candidateNumberSFFieldName);

        //Execute and decode the response
        UpsertResult result = executeUpsert(candidateNumberSFFieldName,
            candidate.getCandidateNumber(), contactRequest);

        assert result != null;
        if (!result.isSuccess()) {
            throw new SalesforceException("Update failed for candidate "
                + candidate.getCandidateNumber()
                + ": " + result.getErrorMessage());
        }

        Contact contact = new Contact(candidate);
        contact.setId(result.getId());

        return contact;
    }

    @Override
    @NonNull
    public List<Contact> createOrUpdateContacts(@NonNull Collection<Candidate> candidates)
        throws WebClientException, SalesforceException {
        List<ContactRecordComposite> contactRequests = new ArrayList<>();
        for (Candidate candidate : candidates) {
            //Create a contact request using data from the candidate
            ContactRecordComposite contactRequest = new ContactRecordComposite(candidate);
            contactRequests.add(contactRequest);
        }
        ContactRequestComposite req = new ContactRequestComposite();
        req.setRecords(contactRequests);

        //Execute and decode the response
        UpsertResult[] results =
            executeUpserts(candidateNumberSFFieldName, req);

        if (results.length != candidates.size()) {
            //This is a fatal error because if the numbers don't match we don't
            //know how to match results to candidates.
            throw new SalesforceException(
                "Number of results (" + results.length
                    + ") did not match number of candidates ("
                    + candidates.size() + ")");
        }

        //Extract the contacts from the returned results.
        //Failed results will not have the Salesforce id set.
        List<Contact> contacts = new ArrayList<>();
        int count = 0;
        for (Candidate candidate : candidates) {
            Contact contact = new Contact(candidate);
            UpsertResult result = results[count++];
            if (result.isSuccess()) {
                contact.setId(result.getId());
            } else {
                LogBuilder.builder(log)
                    .action("CreateOrUpdateContacts")
                    .message("Update failed for candidate "
                        + candidate.getCandidateNumber()
                        + ": " + result.getErrorMessage())
                    .logError();
            }
            contacts.add(contact);
        }

        return contacts;
    }

    @NonNull
    @Override
    public String createOrUpdateJobOpportunity(SalesforceJobOpp job) {
        //Create a contact request using data from the candidate
        JobOpportunityRequest request = new JobOpportunityRequest(null, job);

        //Upsert request bodies should not include the external id because it is used as the unique
        //key used to identify the record to be updated - specified in the PATCH uri, not in the
        //request body.
        request.remove(jobOpportunitySFFieldName);

        String tcId = job.getId().toString();
        //Execute and decode the response
        UpsertResult result = executeUpsert(jobOpportunitySFFieldName, tcId, request);

        assert result != null;
        if (!result.isSuccess()) {
            throw new SalesforceException("Update failed for job "
                + tcId
                + ": " + result.getErrorMessage());
        }

        return result.getId();
    }

    @Override
    public void createOrUpdateCandidateOpportunities(
        List<Candidate> candidates, CandidateOpportunityParams candidateOppParams,
        SalesforceJobOpp jobOpportunity)
        throws WebClientException, SalesforceException {

        LogBuilder.builder(log)
            .action("CreateOrUpdateCandidateOpportunities")
            .message("Looking for opps for " + candidates.size() +" candidates")
            .logInfo();

        //Find out which candidates already have opportunities (so just need to be updated)
        //and which need opportunities to be created.
        //Note that we need to store candidateNumbers - not the Candidate entities themselves
        //Otherwise checking membership of this list (ie contains) does not work because
        //Hibernate proxies of entities do not equal the actual entities.
        List<String> candidateNumbersWithNoOpp = selectCandidatesWithNoOpp(candidates, jobOpportunity);

        LogBuilder.builder(log)
            .action("CreateOrUpdateCandidateOpportunities")
            .message("Need to create opps for " + candidateNumbersWithNoOpp.size() + " candidates")
            .logInfo();

        String recordType = getCandidateOpportunityRecordType(jobOpportunity);

        //Now build requests of candidate opportunities we want to create or update
        List<CandidateOpportunityRecordComposite> opportunityRequests = new ArrayList<>();
        for (Candidate candidate : candidates) {
            boolean create = candidateNumbersWithNoOpp.contains(candidate.getCandidateNumber());

            LogBuilder.builder(log)
                .action("CreateOrUpdateCandidateOpportunities")
                .message((create ? "Create" : "Update") + " opp for " + candidate.getCandidateNumber())
                .logInfo();

            //Build candidate opportunity request (create or update as needed)
            CandidateOpportunityRecordComposite opportunityRequest =
                new CandidateOpportunityRecordComposite(recordType,
                    candidate, jobOpportunity, create);
            opportunityRequests.add(opportunityRequest);

            //Now set any requested stage name and next step
            String stageName = null;
            String nextStep = null;
            LocalDate nextStepDueDate = null;
            String closingComments = null;
            String closingCommentsForCandidate = null;
            String employerFeedback = null;
            Map<String, Integer> relocationInfo = null;
            if (candidateOppParams != null) {
                final CandidateOpportunityStage stage = candidateOppParams.getStage();
                stageName = stage == null ? null : stage.getSalesforceStageName();
                nextStep = candidateOppParams.getNextStep();
                nextStepDueDate = candidateOppParams.getNextStepDueDate();
                closingComments = candidateOppParams.getClosingComments();
                closingCommentsForCandidate = candidateOppParams.getClosingCommentsForCandidate();
                employerFeedback = candidateOppParams.getEmployerFeedback();
                relocationInfo = candidateOppParams.getRelocationInfo();

                // If relocationInfo not already included in params and new stage is 'Offer',
                // update the SF case relocation info - just to assist with monitoring & evaluation,
                // a failsafe in case admin users haven't clicked the 'Update case stats' button
                // when updating relocating dependant info, which can be set on a visa job check
                // or directly on the Candidate Opp via the 'Upload' tab.
                // Typically, would use CandidateOpportunityService here, but that would create a
                // dependency cycle between beans — so instead querying the SalesforceJobOpp to get
                // the CandidateOpportunity required for processSfCaseRelocationInfo()
                if (relocationInfo == null && stage == CandidateOpportunityStage.offer) {
                    Optional<CandidateOpportunity> candidateOpp =
                        jobOpportunity.getCandidateOpportunities()
                            .stream()
                            .filter(opp -> opp.getCandidate().getId().equals(candidate.getId()))
                            .findFirst();
                    if (candidateOpp.isPresent()) {
                        relocationInfo = processSfCaseRelocationInfo(candidateOpp.get(), candidate);
                    }
                }
            }

            //Always need to specify a stage name when creating a new opp
            if (create && stageName == null) {
                stageName = "Prospect";
            }

            if (stageName != null) {
                opportunityRequest.setStageName(stageName);
            }
            if (nextStep != null) {
                opportunityRequest.setNextStep(nextStep);
            }
            if (nextStepDueDate != null) {
                opportunityRequest.setNextStepDueDate(nextStepDueDate);
            }
            if (closingComments != null) {
                opportunityRequest.setClosingComments(closingComments);
            }
            if (closingCommentsForCandidate != null) {
                opportunityRequest.setClosingCommentsForCandidate(closingCommentsForCandidate);
            }
            if (employerFeedback != null) {
                opportunityRequest.setEmployerFeedback(employerFeedback);
            }
            if (relocationInfo != null) {
                opportunityRequest.setRelocatingBoys(relocationInfo.get("relocatingBoys"));
                opportunityRequest.setRelocatingGirls(relocationInfo.get("relocatingGirls"));
                opportunityRequest.setRelocatingChildren(relocationInfo.get("relocatingChildren"));
                opportunityRequest.setRelocatingMen(relocationInfo.get("relocatingMen"));
                opportunityRequest.setRelocatingWomen(relocationInfo.get("relocatingWomen"));
                opportunityRequest.setRelocatingAdults(relocationInfo.get("relocatingAdults"));
            }
        }

        executeCandidateOpportunityRequests(opportunityRequests);
    }

    private List<String> selectCandidatesWithNoOpp(List<Candidate> candidates,
        SalesforceJobOpp jobOpportunity) throws SalesforceException {

        //First creating a map of all candidates indexed by their what their unique
        //opportunity id should be.
        Map<String, String> idCandidateNumberMap =
            buildCandidateOppsMap(candidates, jobOpportunity.getSfId());

        //Now find the ids we actually have for candidate opportunities for this job.
        List<Opportunity> opps = findCandidateOpportunitiesByJobOpps(jobOpportunity.getSfId());

        LogBuilder.builder(log)
            .action("SelectCandidatesWithNoOpp")
            .message("Found " + opps.size() + " candidate opps on SF for job " + jobOpportunity.getId())
            .logInfo();

        //Remove these from map, leaving just those that need to be created
        for (Opportunity opp : opps) {
            idCandidateNumberMap.remove(opp.getCandidateExternalId());
        }

        //Extract these candidates from the map.
        return new ArrayList<>(idCandidateNumberMap.values());
    }

    @Override
    @Nullable
    public Contact findContact(@NonNull Candidate candidate)
        throws GeneralSecurityException, WebClientException {

        String candidateNumber = candidate.getCandidateNumber();

        //Note that the fields requested in the query should match the fields
        //in the Contact record.
        String query =
            "SELECT " + contactRetrievalFields +
                " FROM Contact WHERE " +
                candidateNumberSFFieldName + "=" + candidateNumber;

        ClientResponse response = executeQuery(query);
        ContactQueryResult contacts = response.bodyToMono(ContactQueryResult.class).block();

        //Retrieve the contact from the response
        Contact contact = null;
        if (contacts != null) {
            if (contacts.totalSize > 0) {
                final int nContacts = contacts.records.size();
                if (nContacts > 0) {
                    contact = contacts.records.get(0);
                    if (nContacts > 1) {
                        //We have multiple contacts in Salesforce for the same
                        //TBB candidate. There should only be one.
                        final String msg = "Candidate number " + candidateNumber +
                            " has more than one Contact record on Salesforce";

                        LogBuilder.builder(log)
                            .action("FindContact")
                            .message(msg)
                            .logWarn();

                        if (!alertedDuplicateSFRecord) {
                            emailHelper.sendAlert(msg);
                            alertedDuplicateSFRecord = true;
                        }
                    }
                }
            }
        }

        return contact;
    }

    @Override
    public List<Contact> findCandidateContacts() throws WebClientException {
        return findContacts(candidateNumberSFFieldName + " > 0");
    }

    @Override
    public @Nullable Opportunity findCandidateOpportunity(String candidateNumber, String jobSfId) {
        final String externalId = makeExternalId(candidateNumber, jobSfId);
        final List<Opportunity> opportunities = findCandidateOpportunities(
            candidateOpportunitySFFieldName + "='" + externalId + "'");
        if (opportunities.size() > 1) {
            LogBuilder.builder(log)
                .action("FindCandidateOpportunity")
                .message("Multiple SF candidate opportunities for externalId " + externalId)
                .logError();
        }
        return opportunities.size() == 0 ? null : opportunities.get(0);
    }

    @NonNull
    @Override
    public List<Opportunity> findCandidateOpportunities(String condition)
        throws WebClientException {

        return findCandidateOpportunities(condition, 0);
    }

    @Override
    public List<Opportunity> findCandidateOpportunities(String condition, int limit) {

        //Extra condition of Candidate_TC_id__c > '0' rules out Job opportunities for which
        //it will be ''.
        String query =
            "SELECT " + candidateOpportunityRetrievalFields +
                " FROM Opportunity WHERE Candidate_TC_id__c > '0'";
        if (condition != null) {
            query += " AND " + condition;
        }
        query += "  ORDER BY Id";
        if (limit != 0) {
            query += " LIMIT " + limit;
        }

        ClientResponse response = executeQuery(query);
        OpportunityQueryResult result =
            response.bodyToMono(OpportunityQueryResult.class).block();

        //Retrieve the contact from the response
        List<Opportunity> opportunities = new ArrayList<>();
        if (result != null) {
            opportunities = result.records;
        }

        return opportunities;
    }

    @Override
    @NonNull
    public List<Contact> findContacts(String condition) throws WebClientException {
        String query =
            "SELECT " + contactRetrievalFields +
                " FROM Contact WHERE " + condition;

        ClientResponse response = executeQuery(query);
        ContactQueryResult result =
            response.bodyToMono(ContactQueryResult.class).block();

        //Retrieve the contact from the response
        List<Contact> contacts = new ArrayList<>();
        if (result != null) {
            contacts = result.records;
        }

        return contacts;
    }

    public String generateCandidateOppName(
        @NonNull Candidate candidate, @NonNull SalesforceJobOpp jobOpp) {
        String provisionalName = candidate.getUser().getFirstName()  +
            "(" + candidate.getCandidateNumber() + ")-" + jobOpp.getName();
        // SF character limit for opportunity titles = 120
        return provisionalName.length() > 120 ?
            provisionalName.substring(0, 117) + "..." : provisionalName;
    }

    static class ContactQueryResult extends QueryResult {

        public List<Contact> records;
    }

    @Override
    public List<Opportunity> fetchOpportunitiesByOpenOnSF(OpportunityType type)
        throws SalesforceException {
        List<Opportunity> opps = new ArrayList<>();

            String query = switch (type) {
                case JOB ->
                    "SELECT " + jobOpportunityRetrievalFields
                    + " FROM Opportunity"
                    + " WHERE IsClosed = false"
                    + " AND LastStageChangeDate > N_DAYS_AGO:" + salesforceConfig.getDaysAgoRecent()
                    + " AND RecordTypeId = '" + salesforceRecordTypeConfig.getEmployerJob() + "'";

                case CANDIDATE ->
                    "SELECT " + candidateOpportunityRetrievalFields
                    + " FROM Opportunity WHERE "
                    + "(IsClosed = false AND LastStageChangeDate > N_DAYS_AGO:"
                    + salesforceConfig.getDaysAgoRecent() + "))"
                    + " AND (RecordTypeId = '"
                    + salesforceRecordTypeConfig.getCandidateRecruitment() + "'"
                    + " OR RecordTypeId = '"
                    + salesforceRecordTypeConfig.getCandidateRecruitmentCan() + "')";

                default ->
                    throw new IllegalArgumentException("Unsupported OpportunityType: " + type);
            };

            ClientResponse response = executeQuery(query);

            OpportunityQueryResult result =
                response.bodyToMono(OpportunityQueryResult.class).block();

            // Retrieve the records from the response
            if (result != null) {
                opps = result.records;
            }
        return opps;
    }

    @Override
    public List<Opportunity> fetchOpportunitiesById(
        Collection<String> sfIds, OpportunityType type
    ) throws SalesforceException {
        List<Opportunity> opps = new ArrayList<>();
        //Construct the String of IDs for the WHERE clause
        final String idsAsString = sfIds.stream().map(s -> "'" + s + "'")
            .collect(Collectors.joining(","));

        String query = switch (type) {
            case JOB ->
                "SELECT " + jobOpportunityRetrievalFields
                + " FROM Opportunity WHERE"
                + " Id IN (" + idsAsString + ")"
                + " AND RecordTypeId = '" + salesforceRecordTypeConfig.getEmployerJob() + "'";

            case CANDIDATE ->
                "SELECT " + candidateOpportunityRetrievalFields +
                " FROM Opportunity WHERE "
                + "(Id IN (" + idsAsString + ")"
                + " AND (RecordTypeId = '" + salesforceRecordTypeConfig.getCandidateRecruitment() + "'"
                + " OR RecordTypeId = '" + salesforceRecordTypeConfig.getCandidateRecruitmentCan() + "')";

            default -> throw new IllegalArgumentException("Unsupported OpportunityType: " + type);
        };

        ClientResponse response = executeQuery(query);

        OpportunityQueryResult result =
            response.bodyToMono(OpportunityQueryResult.class).block();

        // Retrieve the records from the response
        if (result != null) {
            opps = result.records;
        }
        return opps;
    }

    @Nullable
    @Override
    public Opportunity fetchJobOpportunity(String id) throws SalesforceException {
        return findOpportunity(id, jobOpportunityRetrievalFields);
    }

    @NonNull
    @Override
    public List<Opportunity> findCandidateOpportunitiesByJobOpps(String... jobOpportunityIds)
        throws SalesforceException {
        List<Opportunity> opps = new ArrayList<>();

        if (jobOpportunityIds.length > 0) {

            String idsConcatenated = Arrays.stream(jobOpportunityIds)
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(","));
            String query =
                "SELECT " + candidateOpportunityRetrievalFields +
                    " FROM Opportunity WHERE Parent_Opportunity__c IN (" + idsConcatenated + ")";

            ClientResponse response = executeQuery(query);

            OpportunityQueryResult result =
                response.bodyToMono(OpportunityQueryResult.class).block();

            //Retrieve the contact from the response
            if (result != null) {
                opps = result.records;
            }
        }

        return opps;
    }

    static class OpportunityQueryResult extends QueryResult {

        public List<Opportunity> records;
    }

    @Override
    @Nullable
    public <T> T findRecordFieldsFromId(
        String objectType, String id, String fields, Class<T> cl)
        throws SalesforceException, WebClientException {
        ClientResponse response = executeRecordFieldsGet(objectType, id, fields);
        T result = response.bodyToMono(cl).block();
        return result;
    }

    @Override
    public List<Opportunity> findJobOpportunities()
        throws GeneralSecurityException, WebClientException {
        String query =
            "SELECT " + jobOpportunityRetrievalFields +
                " FROM Opportunity WHERE RecordType.Name='Employer job' "
                + "and IsClosed=false and Probability >= 60 and AccountCountry__c != 'USA'";

        ClientResponse response = executeQuery(query);

        OpportunityQueryResult result =
            response.bodyToMono(OpportunityQueryResult.class).block();

        //Retrieve the opps from the response
        List<Opportunity> opps = null;
        if (result != null) {
            opps = result.records;
        }

        return opps;
    }

    static class OpportunityHistoryQueryResult extends QueryResult {
        public List<OpportunityHistory> records;
    }

    @NonNull
    @Override
    public List<OpportunityHistory> findOpportunityHistories(List<String> opportunityIds)
        throws SalesforceException, WebClientException {

        String ids = opportunityIds.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));

        String query =
            "SELECT OpportunityId, StageName, SystemModstamp" +
                " FROM OpportunityHistory WHERE OpportunityId IN (" + ids
                + ") ORDER BY OpportunityId,SystemModstamp DESC";
        ClientResponse response = executeQuery(query);

        OpportunityHistoryQueryResult result =
            response.bodyToMono(OpportunityHistoryQueryResult.class).block();

        //Retrieve the objects from the response
        List<OpportunityHistory> history = new ArrayList<>();
        if (result != null) {
            history = result.records;
        }

        return history;
    }

    @Override
    @Nullable
    public Account findAccount(String sfId)
        throws SalesforceException, WebClientException {
        Account account = null;
        if (sfId != null) {
            try {
                account = findRecordFieldsFromId(
                    "Account", sfId, "Id,Name,BillingCountry,Description,Has_Hired_Internationally__c,Website", Account.class);
            } catch (NotFound ex) {
                //Just return null if not found
            }
        }
        return account;
    }

    @Override
    @Nullable
    public Opportunity findOpportunity(String sfId)
        throws SalesforceException, WebClientException {
        return findOpportunity(sfId, "Id,Name,AccountId,OwnerId,AccountCountry__c");
    }

    private Opportunity findOpportunity(String sfId, String fields)
        throws SalesforceException, WebClientException {
        Opportunity opportunity = null;
        if (sfId != null) {
            try {
                opportunity = findRecordFieldsFromId(
                    "Opportunity", sfId, fields, Opportunity.class);
            } catch (NotFound ex) {
                //Just return null opportunity if not found
            }
        }
        return opportunity;
    }

    @Override
    public void updateContact(Candidate candidate)
        throws GeneralSecurityException {
        //Create a contact request using data from the candidate
        ContactRequest contactRequest = new ContactRequest(null, candidate);

        String salesforceId = candidate.getSfId();
        if (salesforceId == null) {
            throw new SalesforceException(
                "Could not find candidate " +
                    candidate.getCandidateNumber() +
                    " on Salesforce from sflink " +
                    candidate.getSflink());
        }

        //Execute the update request
        executeUpdate(salesforceId, contactRequest);
    }

    @Override
    public void updateCandidateOpportunities(
        List<EmployerCandidateFeedbackData> feedbacks, SalesforceJobOpp jobOpportunity)
        throws WebClientException, SalesforceException {

        String recordType = getCandidateOpportunityRecordType(jobOpportunity);

        //Now build requests of candidate opportunities we want to update
        List<CandidateOpportunityRecordComposite> opportunityRequests =
            buildCandidateOpportunityRequests(feedbacks, recordType, jobOpportunity);

        executeCandidateOpportunityRequests(opportunityRequests);
    }

    private void executeCandidateOpportunityRequests(
        List<CandidateOpportunityRecordComposite> requests) throws SalesforceException {

        OpportunityRequestComposite req = new OpportunityRequestComposite();
        req.setRecords(requests);

        //Execute and decode the response
        UpsertResult[] results = executeUpserts(candidateOpportunitySFFieldName, req);

        if (results.length != requests.size()) {
            //This is a fatal error because if the numbers don't match we don't
            //know how to match results to requests.
            throw new SalesforceException(
                "Number of results (" + results.length
                    + ") did not match number of requests ("
                    + requests.size() + ")");
        }

        //Log any failures
        int count = 0;
        List<String> errors = new ArrayList<>();
        for (CandidateOpportunityRecordComposite request : requests) {
            UpsertResult result = results[count++];
            if (!result.isSuccess()) {
                LogBuilder.builder(log)
                    .action("ExecuteCandidateOpportunityRequests")
                    .message("Update failed for opportunity "
                        + request.getName()
                        + ": " + result.getErrorMessage())
                    .logError();
                // Create list of errors to return to admin portal
                errors.add(request.getName() + ": " + result.getErrorMessage());
            }
        }
        if (!errors.isEmpty()) {
            throw new SalesforceException("The following update/s failed: " + String.join(",", errors) + ")");
        }
    }

    private List<CandidateOpportunityRecordComposite> buildCandidateOpportunityRequests(
        List<EmployerCandidateFeedbackData> feedbacks, String recordType,
        SalesforceJobOpp jobOpportunity) throws SalesforceException {

        //Figure out which candidates need an opp created.
        //Extract all candidates from feedback.
        List<Candidate> candidates = feedbacks.stream()
            .map(EmployerCandidateFeedbackData::getCandidate)
            .collect(Collectors.toList());
        List<String> candidateNumbersWithNoOpp = selectCandidatesWithNoOpp(candidates, jobOpportunity);

        //Now build the requests
        List<CandidateOpportunityRecordComposite> requests = new ArrayList<>();
        for (EmployerCandidateFeedbackData feedback : feedbacks) {
            final String notes = feedback.getEmployerCandidateNotes();
            final EmployerCandidateDecision decision = feedback.getEmployerCandidateDecision();
            if (notes != null || decision != null) {
                //We have something useful feedback to save

                //Figure our whether this candidate needs an opp created
                final Candidate candidate = feedback.getCandidate();
                boolean create = candidateNumbersWithNoOpp.contains(candidate.getCandidateNumber());

                //Build and add the request
                CandidateOpportunityRecordComposite request = new CandidateOpportunityRecordComposite(
                    recordType, candidate, jobOpportunity, create);
                requests.add(request);

                if (notes != null) {
                    request.setEmployerFeedback(notes);
                }

                if (decision == null) {
                    if (create) {
                        //If we are creating this opp for the first time, we need a stage.
                        //Given that we have sent to employer, set to a review stage
                        request.setStageName("CV review");
                    }
                } else {
                    switch (decision) {
                        case JobOffer:
                            request.setStageName("Offer");
                            break;
                        case NoJobOffer:
                            request.setStageName("No job offer");
                            break;
                    }
                }
            }
        }

        return requests;
    }

    private String getCandidateOpportunityRecordType(SalesforceJobOpp opportunity) {
        Country country = opportunity.getCountry();
        String recordType = "Candidate recruitment";
        if (country != null && "Canada".equals(country.getName())) {
            recordType = "Candidate recruitment (CAN)";
        }
        return recordType;
    }

    @Override
    public void updateEmployerOpportunity(UpdateEmployerOpportunityRequest request)
        throws SalesforceException {

        String sfJoblink = request.getSfJoblink();

        //Get id of job opportunity.
        String jobOpportunityId = SalesforceHelper.extractIdFromSfUrl(sfJoblink);

        if (jobOpportunityId != null) {
            EmployerOpportunityRequest sfRequest = new EmployerOpportunityRequest(request);

            executeUpdate(jobOpportunityId, sfRequest);
        }
    }

    @Override
    public void updateEmployerOpportunityStage(
        String sfId, JobOpportunityStage stage, String nextStep, LocalDate dueDate)
        throws SalesforceException, WebClientException {

        EmployerOppStageUpdateRequest sfRequest =
            new EmployerOppStageUpdateRequest(stage, nextStep, dueDate);

        executeUpdate(sfId, sfRequest);
    }

    @Override
    public void updateEmployerOpportunityName(String sfId, String jobName)
        throws SalesforceException, WebClientException {
        EmployerOppNameUpdateRequest sfRequest =
            new EmployerOppNameUpdateRequest(jobName);

        executeUpdate(sfId, sfRequest);
    }

    /**
     * Execute general purpose Salesforce create.
     * <p/>
     * For details on Salesforce create, see https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_sobject_create.htm
     *
     * @param obj Object supplying data used to create corresponding Salesforce record.
     * @return CreateRecordResult - contains SF id of created record.
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private CreateRecordResult executeCreate(Object obj)
        throws SalesforceException, WebClientException {

        Class<?> cl = obj.getClass();
        String path = classSfPathMap.get(cl);
        if (path == null) {
            throw new InvalidRequestException(
                "No mapping to Salesforce for objects of class " + cl.getSimpleName());
        }

        WebClient.RequestHeadersSpec<?> spec = webClient.post()
            .uri("/sobjects/" + path)
            .body(Mono.just(obj), cl);

        ClientResponse response = executeWithRetry(spec);
        CreateRecordResult result = response.bodyToMono(CreateRecordResult.class).block();
        return result;
    }

    /**
     * Execute general purpose Salesforce update.
     * <p/>
     * For details on Salesforce update, see https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_update_fields.htm
     * <p/>
     * Note that there is no body in the response - so this method just returns void
     *
     * @param id  Salesforce id for the record
     * @param obj Object supplying data used to update the Salesforce record.
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private void executeUpdate(String id, Object obj)
        throws SalesforceException, WebClientException {

        Class<?> cl = obj.getClass();
        String path = classSfPathMap.get(cl);
        if (path == null) {
            throw new InvalidRequestException(
                "No mapping to Salesforce for objects of class " + cl.getSimpleName());
        }

        WebClient.RequestHeadersSpec<?> spec = webClient.patch()
            .uri("/sobjects/" + path + "/" + id)
            .body(Mono.just(obj), cl);

        ClientResponse response = executeWithRetry(spec);

        //Only a 204 response is expected - and no body.
        if (response.statusCode().value() != 204) {
            WebClientException ex = response.createException().block();
            assert ex != null;
            throw ex;
        }
    }

    /**
     * Execute general purpose Salesforce upsert (insert - ie create - if it doesn't exist,
     * otherwise update if it does) based on a unique external id. For example TBBid__c, passing in
     * a candidate's candidateNumber as the id.
     * <p/>
     * For details on Salesforce upsert, see https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_upsert.htm
     * <p/>
     * In recent versions of API (since 46.0) you always get a response with a body containing the
     * SF id.
     * <ul>
     *     <li>create - HTTP status 201 (Created)</li>
     *     <li>update - HTTP status 200 (OK) </li>
     * </ul>
     *
     * @param externalIdName Name of SF field containing a unique id used to identify SF records.
     *                       For example the SF TBBid__c field on Contact records which we populate
     *                       with candidate's candidateNumber.
     * @param id             Value of the externalID
     * @param obj            Object supplying data used to update the Salesforce record.
     * @return ClientResponse
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private UpsertResult executeUpsert(String externalIdName, String id, Object obj)
        throws SalesforceException, WebClientException {

        Class<?> cl = obj.getClass();
        String path = classSfPathMap.get(cl);
        if (path == null) {
            throw new InvalidRequestException(
                "No mapping to Salesforce for objects of class " + cl.getSimpleName());
        }

        WebClient.RequestHeadersSpec<?> spec = webClient.patch()
            .uri("/sobjects/" + path + "/" + externalIdName + "/" + id)
            .body(Mono.just(obj), cl);

        ClientResponse response = executeWithRetry(spec);
        UpsertResult result = response.bodyToMono(UpsertResult.class).block();
        return result;
    }

    /**
     * See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_composite_sobjects_collections_upsert.htm
     *
     * @param externalIdName Name of SF field containing an unique id used to identify SF records.
     *                       For example the SF TBBid__c field on Contact records which we populate
     *                       with candidate's candidateNumber.
     * @param obj            Object supplying data used to update the Salesforce record.
     * @return ClientResponses
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private UpsertResult[] executeUpserts(String externalIdName, HasSize obj)
        throws SalesforceException {

        Class<?> cl = obj.getClass();
        String path = classSfCompositePathMap.get(cl);
        if (path == null) {
            throw new InvalidRequestException(
                "No mapping to Salesforce for objects of class " + cl.getSimpleName());
        }

        if (obj.checkSize() > 200) {
            throw new InvalidRequestException(
                "Too many records (" + obj.checkSize()
                    + ") to update in one go. Maximum = 200.");
        }

        WebClient.RequestHeadersSpec<?> spec = webClient.patch()
            .uri("/composite/sobjects/" + path + "/" + externalIdName)
            .body(Mono.just(obj), cl);

        ClientResponse response = executeWithRetry(spec);
        UpsertResult[] results = response.bodyToMono(UpsertResult[].class).block();

        return results;
    }

    /**
     * Execute general purpose Salesforce query.
     * <p/>
     * For details on Salesforce queries, see https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_query.htm
     *
     * @param query Query to be executed
     * @return ClientResponse - can use bodyToMono method to extract into an object.
     * @throws SalesforceException If there are errors relating to keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private ClientResponse executeQuery(String query)
        throws SalesforceException, WebClientException {

        WebClient.RequestHeadersSpec<?> spec = webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/query")
                .queryParam("q", query).build());

        return executeWithRetry(spec);
    }

    /**
     * General purpose Get fields from a record of a given type See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_get_field_values.htm
     *
     * @param objectType Type of record
     * @param id         ID of record
     * @param fields     Fields requested
     * @return ClientResponse resulting from Get
     * @throws SalesforceException if there is a problem with our keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private ClientResponse executeRecordFieldsGet(
        String objectType, String id, String fields)
        throws SalesforceException, WebClientException {
        WebClient.RequestHeadersSpec<?> spec = webClient.get()
            .uri("/sobjects/" + objectType + "/" + id +
                "/?fields=" + fields);

        return executeWithRetry(spec);
    }

    /**
     * Executes a request with a retry if the accessToken validity has expired in which case another
     * accessToken is automatically requested.
     *
     * @param spec for the request
     * @return ClientResponse received
     * @throws SalesforceException if there is a problem with our keys and digital signing.
     * @throws WebClientException       if there is a problem connecting to Salesforce
     */
    private ClientResponse executeWithRetry(WebClient.RequestHeadersSpec<?> spec)
        throws SalesforceException {
        if (accessToken == null) {
            accessToken = requestAccessToken();
        }
        spec.headers(headers -> headers.put("Authorization",
            Collections.singletonList("Bearer " + accessToken)));

        //Catch below connection reset exception that has been seen
        //reactor.core.Exceptions$ReactiveException: java.io.IOException: Connection reset by peer
        ClientResponse clientResponse;
        try {
            clientResponse = spec.exchange().block();
        } catch (Exception ex) {
            //Do one automatic retry after a wait.
            LogBuilder.builder(log)
                .action("ExecuteWithRetry")
                .message("Problem with Salesforce connection")
                .logWarn(ex);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LogBuilder.builder(log)
                    .action("ExecuteWithRetry")
                    .message("Interrupted wait for Salesforce retry")
                    .logWarn(e);
            }
            clientResponse = spec.exchange().block();
        }
        if (clientResponse == null || clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
            LogBuilder.builder(log)
                .action("ExecuteWithRetry")
                .message("Getting new token from Salesforce")
                .logInfo();

            //Get new token and try again
            accessToken = requestAccessToken();
            spec.headers(headers -> headers.put("Authorization",
                Collections.singletonList("Bearer " + accessToken)));
            LogBuilder.builder(log)
                .action("ExecuteWithRetry")
                .message("Connecting to Salesforce with new token")
                .logInfo();
            clientResponse = spec.exchange().block();
        }

        if (clientResponse == null) {
            throw new RuntimeException("Null client response to Salesforce request");
        } else if (clientResponse.statusCode().value() == 300 ||
            clientResponse.statusCode().value() == 400) {
            //Pull out the extra info on the error provided by Salesforce.
            //See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/errorcodes.htm
            String errorInfo = clientResponse.bodyToMono(String.class).block();
            //Create an exception and use their message. The exception by itself
            //is no use because it doesn't decode the above Salesforce error
            //info from the body of the response.
            WebClientException ex = clientResponse.createException().block();
            assert ex != null;

            //Create our own exception with the extra info.
            throw new SalesforceException(ex.getMessage() + ": " + errorInfo);
        } else if (clientResponse.statusCode().value() > 300) {
            WebClientException ex = clientResponse.createException().block();
            assert ex != null;
            throw ex;
        }
        return clientResponse;
    }

    /**
     * Requests an accessToken from Salesforce.
     * <p/>
     * This involves making a special, digitally signed, JWT Bearer Token request to SF.
     * <p/>
     * See https://help.salesforce.com/articleView?id=remoteaccess_oauth_jwt_flow.htm&type=5
     *
     * @return Access token provided by Salesforce
     * @throws SalesforceException if there is a problem with our keys and digital signing.
     */
    private @NonNull
    String requestAccessToken() throws SalesforceException {
        String jwtBearerToken = makeJwtBearerToken();

        WebClient client = WebClient
            .create(salesforceConfig.getBaseLoginUrl() + "services/oauth2/token");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type",
            "urn:ietf:params:oauth:grant-type:jwt-bearer");
        params.add("assertion", jwtBearerToken);

        BearerTokenResponse response = client.post()
            .uri(uriBuilder -> uriBuilder.queryParams(params).build())
            .header("Content_Type",
                "application/x-www-form-urlencoded")
            .retrieve()
            .bodyToMono(BearerTokenResponse.class)
            .block();

        if (response == null) {
            throw new SalesforceException("Null BearerTokenResponse");
        }

        return response.access_token;
    }

    /**
     * Returns a new JWTBearerToken valid for the next 3 minutes.
     * <p/>
     * The token is signed using TalentBeyondBoundaries private key associated with the certificate
     * associated with the tbbtalent "Connected App" in TBB's Salesforce.
     * <p/>
     * This can be used to get a new access token from Salesforce using the following HTTP request
     * to SF: POST https://login.salesforce.com/services/oauth2/token ?grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=[bearer
     * token]
     * <p>
     * Content-Type: application/x-www-form-urlencoded"
     * <p/>
     * If all goes well it will return a Bearer access token in a response like this: {
     * "access_token": "00D1N000002EPj7!AR0AQIaT.sdLMZlsU0Jlm7EPrrWghps9K025kno0nGwg6nf3KmQ9maLukiy20hvnqI1VUw9CY7GhoLIdgN8QsdYOoAk91azE",
     * "scope": "custom_permissions web openid api id full", "instance_url":
     * "https://talentbeyondboundaries.my.salesforce.com", "id": "https://login.salesforce.com/id/00D1N000002EPj7UAG/0051N000005qGt3QAE",
     * "token_type": "Bearer" }
     * <p/>
     * This code is based on https://help.salesforce.com/articleView?id=remoteaccess_oauth_jwt_flow.htm&type=5
     *
     * @return JWTBearerToken
     * @throws SalesforceException If there are signing problems
     */
    private String makeJwtBearerToken() throws SalesforceException {
        String header = "{\"alg\":\"RS256\"}";
        String claimTemplate = "'{'" +
            "\"iss\": \"{0}\", " +
            "\"sub\": \"{1}\", " +
            "\"aud\": \"{2}\", " +
            "\"exp\": \"{3}\"" +
            "'}'";

        StringBuilder token = new StringBuilder();

        //Encode the JWT Header and add it to our string to sign
        token.append(Encoders.BASE64URL.encode(header.getBytes(StandardCharsets.UTF_8)));

        //Separate with a period
        token.append(".");

        //Create the JWT Claims Object
        String[] claimArray = new String[4];
        claimArray[0] = salesforceConfig.getConsumerKey();
        claimArray[1] = salesforceConfig.getUser();
        claimArray[2] = salesforceConfig.getBaseLoginUrl();
        claimArray[3] = Long.toString((System.currentTimeMillis() / 1000) + 180);
        MessageFormat claims;
        claims = new MessageFormat(claimTemplate);
        String payload = claims.format(claimArray);

        //Add the encoded claims object
        token.append(Encoders.BASE64URL.encode(payload.getBytes(StandardCharsets.UTF_8)));

        try {
            //Sign the JWT Header + "." + JWT Claims Object
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(token.toString().getBytes(StandardCharsets.UTF_8));
            String signedPayload = Encoders.BASE64URL.encode(signature.sign());

            //Separate with a period
            token.append(".");

            //Add the encoded signature
            token.append(signedPayload);
        } catch (GeneralSecurityException ex) {
            throw new SalesforceException(ex.getMessage());
        }

        return token.toString();
    }

    private PrivateKey privateKeyFromPkcs8(String privateKeyPem)
        throws GeneralSecurityException {

        // strip the headers and new lines
        privateKeyPem = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKeyPem = privateKeyPem.replace("-----END PRIVATE KEY-----", "");
        privateKeyPem = privateKeyPem.replaceAll(System.lineSeparator(), "");

        byte[] encoded = Decoders.BASE64.decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    static String makeExternalId(String candidateNumber, String jobId) {
        return candidateNumber + "-" + jobId;
    }

    static class BearerTokenResponse {

        public String access_token;
        public String scope;
        public String instance_url;
        public String id;
        public String token_type;
    }

    static abstract class QueryResult {

        public int totalSize;
        public boolean done;
    }

    @Getter
    static class CreateRecordResult {

        public String id;
        public boolean success;
        public List<ErrorRecord> errors;

        public String getErrorMessage() {
            StringBuilder builder = new StringBuilder();
            for (ErrorRecord error : getErrors()) {
                builder.append(error).append('\n');
            }
            return builder.toString();
        }
    }

    @Getter
    static class UpsertResult extends CreateRecordResult {

        public boolean created;
    }

    @Getter
    @Setter
    @ToString
    static class ErrorRecord {

        public String statusCode;
        public String message;
        public List<String> fields;
    }


    /**
     * This is the core information that is sent in the body of Salesforce HTTP requests
     * representing the Salesforce Contact fields to be populated in a create or update.
     * <p/>
     * However we normally send multiple updates at the same time as described here:
     * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_composite_sobject_tree_flat.htm
     * <p/>
     * In that scenario, each of these objects is prepended with a {@link CompositeAttributes}
     * object. This is represented by the associated {@link ContactRecordComposite}.
     * <p/>
     * The request containing these multiple updates is represented by {@link
     * ContactRequestComposite}
     * <p/>
     * Note that attributes are stored in the Map superclass rather than being explicit fields (as
     * our original code was written). This is so that we can do partial updates - just supplying
     * the fields that we want to change - rather than supplying all fields every time which is what
     * happens when you have fixed field attributes.
     * <p/>
     * A LinkedHashMap is used because we need some control over the order of the fields serialized
     * to JSON and sent in the request. In particular the "attributes" field has to come first if it
     * is present (as it is in "composite" requests).
     */
    class ContactRequest extends LinkedHashMap<String, Object> {

        public ContactRequest(@Nullable CompositeAttributes attributes, Candidate candidate) {
            //If present, attributes must be set first - so that it appears first in the JSON
            //serialization (thanks to using LinkedHashMap rather than a normal HashMap)
            if (attributes != null) {
                setAttributes(attributes);
            }

            User user = candidate.getUser();
            setFirstName(user.getFirstName());
            setLastName(user.getLastName());

            //Set Contact type = candidate - NB: this is a custom field and not related to SF record type
            setContactType(candidateContactTypeSFFieldValue);

            //Set Record Type - this is the actual SF field that denotes picklist and page layout attribution
            setRecordType(contactRecordTypeId);

            //Add partner account id
            Partner partner = user.getPartner();
            //Update candidate partner Salesforce account id
            if (partner != null) {
                String partnerSfAccountId = partner.getSfId();
                if (partnerSfAccountId != null) {
                    setSourcePartnerAccountId(partnerSfAccountId);
                }
            }

            final String email = user.getEmail();
            setEmail(email);

            final Gender gender = candidate.getGender();
            if (gender != null) {
                setGender(gender.toString());
            }

            final String country = candidate.getCountry().getName();
            setMailingCountry(country);

            final Country countryOfNationality = candidate.getNationality();
            if (countryOfNationality != null) {
                setNationality(countryOfNationality.getName());
            }

            //Set account id based on candidate's country
            switch (country) {
                case "Jordan":
                    setAccountId(salesforceTbbAccountsConfig.getJordanAccount());
                    break;
                case "Lebanon":
                    setAccountId(salesforceTbbAccountsConfig.getLebanonAccount());
                    break;
                default:
                    setAccountId(salesforceTbbAccountsConfig.getOtherAccount());
            }

            setTCid(Long.valueOf(candidate.getCandidateNumber()));

            final String intaked = candidate.getTopLevelIntakeCompleted();
            setIntaked(intaked);

            final String intakeDate = candidate.getTopLevelIntakeCompletedDate();
            if (!"".equals(intakeDate)) {
                setIntakeDate(intakeDate);
            }

            if (candidate.getDob() != null) {
            final String dateOfBirth = String.valueOf(candidate.getDob());
            setDateOfBirth(dateOfBirth);
            }

            final String status = String.valueOf(candidate.getStatus());
            setStatus(status);

            final String tcAccountCreated = String.valueOf(candidate.getCreatedDate());
            setTcAccountCreated(tcAccountCreated.substring(0, 9));

            final String unhcrRegistered = String.valueOf(candidate.getUnhcrRegistered());
            if (unhcrRegistered != null) {
                setUnhcrRegistered(unhcrRegistered);
            }

            List<CandidateLanguage> candidateLanguagesList = candidate.getCandidateLanguages();
            final String languagesSpoken = candidateLanguagesList.stream()
                .map(candidateLanguage -> Optional.ofNullable(candidateLanguage.getLanguage())
                    .map(Language::getName)
                    .orElse(""))
                .collect(Collectors.joining("; "));
            setLanguagesSpoken(languagesSpoken);

            final String englishSpeakingLevel = getSpecificLanguageSpeakingLevel(candidateLanguagesList, "English");
            if (englishSpeakingLevel != null) {
                setEnglishSpeakingLevel(englishSpeakingLevel);
            }

            final String frenchSpeakingLevel = getSpecificLanguageSpeakingLevel(candidateLanguagesList, "French");
            if (frenchSpeakingLevel != null) {
                setFrenchSpeakingLevel(frenchSpeakingLevel);
            }

            final String germanSpeakingLevel = getSpecificLanguageSpeakingLevel(candidateLanguagesList, "German");
            if (germanSpeakingLevel != null) {
                setGermanSpeakingLevel(germanSpeakingLevel);
            }

            final String spanishSpeakingLevel = getSpecificLanguageSpeakingLevel(candidateLanguagesList, "Spanish");
            if (spanishSpeakingLevel != null) {
                setSpanishSpeakingLevel(spanishSpeakingLevel);
            }

            final String maxEducationLevel = candidate.getMaxEducationLevel().getName();
            if (maxEducationLevel != null) {
                setMaxEducationLevel(maxEducationLevel);
            }

            List<CandidateOccupation> candidateOccupationsList = candidate.getCandidateOccupations();
            final String occupations = candidateOccupationsList.stream()
                .map(candidateOccupation -> Optional.ofNullable(candidateOccupation.getOccupation())
                    .map(Occupation::getName)
                    .orElse(""))
                .collect(Collectors.joining("; "));
            setOccupations(occupations);

            final boolean tcContactConsent = candidate.getContactConsentRegistration();
            setTcContactConsent(tcContactConsent);

            final boolean partnerContactConsent = candidate.getContactConsentPartners();
            setPartnerContactConsent(partnerContactConsent);

            final boolean monitoringEvaluationConsent = getMonitoringEvaluationConsentBoolean(candidate);
            setMonitoringEvaluationConsent(monitoringEvaluationConsent);

            if (candidate.getRelocatedAddress() != null) {
                final String relocatedAddress = candidate.getRelocatedAddress();
                setRelocatedAddress(relocatedAddress);
            }

            if (candidate.getRelocatedCity() != null) {
                final String relocatedCity = candidate.getRelocatedCity();
                setRelocatedCity(relocatedCity);
            }

            if (candidate.getRelocatedState() != null) {
                final String relocatedState = candidate.getRelocatedState();
                setRelocatedState(relocatedState);
            }

            if (candidate.getRelocatedCountry() != null) {
                final String relocatedCountry = candidate.getRelocatedCountry().getName();
                setRelocatedCountry(relocatedCountry);
            }
        }

        private String getSpecificLanguageSpeakingLevel(List<CandidateLanguage> candidateLanguagesList, String languageToFind) {
            CandidateLanguage languageToCheck = candidateLanguagesList.stream()
                .filter(candidateLanguage -> languageToFind.equals(candidateLanguage.getLanguage().getName()))
                .findAny()
                .orElse(null);
            if (languageToCheck != null) {
                return languageToCheck.getSpokenLevel() == null ? null :
                    String.valueOf(languageToCheck.getSpokenLevel().getName());
            } else {
                return null;
            }

        }

        /**
         * Need to set the string enum to a boolean to match the SF field (checkbox). This boolean field is then used in survey workflows.
         * Kept the string value on the TC as we can capture Yes/No which provides a bit more detail on the response
         * (e.g. difference between No vs NULL) but need to convert this to boolean for our SF workflow.
         * Only explicitly answering Yes will render true. No, NoResponse & NULL values render false.
         * @param candidate Candidate whose monitoring evaluation consent we are syncing to SF
         * @return consent value, true = Yes or false = No/NoResponse/null
         */
        private boolean getMonitoringEvaluationConsentBoolean(Candidate candidate) {
            return candidate.getMonitoringEvaluationConsent() == YesNo.Yes;
        }

        public void setAccountId(String accountId) {
            super.put("AccountId", accountId);
        }

        public void setAttributes(CompositeAttributes attributes) {
            put("attributes", attributes);
        }

        public void setContactType(String contactType) {
            super.put("Contact_Type__c", contactType);
        }

        public void setRecordType(String contactRecordTypeId) {
            super.put("RecordTypeId", contactRecordTypeId);
        }

        public void setEmail(String email) {
            super.put("Email", email);
        }

        public void setFirstName(String firstName) {
            super.put("FirstName", firstName);
        }

        public void setGender(String gender) {
            super.put("Gender__c", gender);
        }

        public void setLastName(String lastName) {
            super.put("LastName", lastName);
        }

        public void setMailingCountry(String mailingCountry) {
            super.put("MailingCountry", mailingCountry);
        }

        public void setNationality(String nationalityCountry) {
            super.put("Nationality__c", nationalityCountry);
        }

        public void setId(String id) {
            super.put("Id", id);
        }

        public void setSourcePartnerAccountId(String sourcePartnerAccountId) {
            super.put("Source_Partner__c", sourcePartnerAccountId);
        }

        public void setTCid(Long tcId) {
            super.put(candidateNumberSFFieldName, tcId);
        }

        public void setDateOfBirth(String dateOfBirth) { super.put("Date_of_Birth__c", dateOfBirth); }

        public void setStatus(String status) { super.put("TC_Status__c", status); }

        public void setTcAccountCreated(String tcAccountCreated) { super.put("TC_Account_Created__c", tcAccountCreated); }

        public void setUnhcrRegistered(String unhcrRegistered) { super.put("UNHCR_Registered__c", unhcrRegistered); }

        public void setLanguagesSpoken(String languagesSpoken) { super.put("Language_s_Spoken__c", languagesSpoken); }

        public void setEnglishSpeakingLevel(String englishSpeakingLevel) { super.put("English_Speaking_Level__c", englishSpeakingLevel); }

        public void setFrenchSpeakingLevel(String frenchSpeakingLevel) { super.put("French_Speaking_Level__c", frenchSpeakingLevel); }

        public void setGermanSpeakingLevel(String germanSpeakingLevel) { super.put("German_Speaking_Level__c", germanSpeakingLevel); }

        public void setSpanishSpeakingLevel(String spanishSpeakingLevel) { super.put("Spanish_Speaking_Level__c", spanishSpeakingLevel); }

        public void setMaxEducationLevel(String maxEducationLevel) { super.put("Highest_Educational_Attainment__c", maxEducationLevel); }

        public void setOccupations(String occupations) { super.put("Occupation_s__c", occupations); }

        public void setIntaked(String intaked) { super.put("Intaked__c", intaked); }

        public void setIntakeDate(String intakeDate) { super.put("Intake_Date__c", intakeDate); }

        public void setTcContactConsent(boolean tcContactConsent) { super.put("TC_Contact_Consent__c", tcContactConsent); }

        public void setPartnerContactConsent(boolean partnerContactConsent) { super.put("Partner_Contact_Consent__c", partnerContactConsent); }

        public void setMonitoringEvaluationConsent(boolean monitoringEvaluationConsent) { super.put("Monitoring_Evaluation_Consent__c", monitoringEvaluationConsent); }

        public void setRelocatedAddress(String relocatedAddress) {
            super.put("Relocated_Street__c", relocatedAddress);
        }

        public void setRelocatedCity(String relocatedCity) {
            super.put("Relocated_City__c", relocatedCity);
        }

        public void setRelocatedState(String relocatedState) {
            super.put("Relocated_State_Province__c", relocatedState);
        }

        public void setRelocatedCountry(String relocatedCountry) {
            super.put("Relocated_Country__c", relocatedCountry);
        }
    }

    /**
     * Single request containing multiple Contact updates.
     * <p/>
     * See doc for {@link ContactRequest}
     */
    @Getter
    @Setter
    @ToString
    class ContactRequestComposite implements HasSize {

        public boolean allOrNone = false;
        public List<ContactRecordComposite> records = new ArrayList<>();

        @Override
        public int checkSize() {
            return records.size();
        }
    }

    /**
     * Wrapper for each {@link ContactRequest} in a {@link ContactRequestComposite}
     * <p/>
     * See doc for {@link ContactRequest}
     */
    @Getter
    @Setter
    @ToString(callSuper = true)
    class ContactRecordComposite extends ContactRequest {

        public ContactRecordComposite(Candidate candidate) {
            super(new CompositeAttributes("Contact"), candidate);
        }
    }

    /**
     * See doc for {@link ContactRequest}
     */
    @Getter
    @Setter
    @ToString
    class EmployerOpportunityRequest {

        /**
         * Link (url) to Google list folder.
         * <p/>
         * Note:The field used to be called CVs folder. It is now displayed as "List folder" in SF
         * but the field still has the old internal name
         */
        public String CVs_Folder__c;

        /**
         * Link (url) to Google Job Description folder
         */
        public String Job_Description_Folder__c;

        /**
         * Link (url) to Talent Catalog list
         */
        public String Talent_Catalog_List__c;

        /**
         * ID of Talent Catalog job entity
         */
        public Long TCid__c;

        public EmployerOpportunityRequest(UpdateEmployerOpportunityRequest request) {
            //Note that we now store the Root folder link in what used to be called the CVs folder
            //on SF. It is now displayed in SF as "List folder" - but it still has the old
            //internal name "CVs_Folder__c".
            //We no longer automatically create a CVs folder since we moved to publishing
            //Google sheets.
            CVs_Folder__c = request.getFolderlink();
            Job_Description_Folder__c = request.getFolderjdlink();
            Talent_Catalog_List__c = request.getListlink();
            TCid__c = request.getJobId();
        }
    }

    class EmployerOppStageUpdateRequest extends LinkedHashMap<String, Object> {

        public EmployerOppStageUpdateRequest(
            @Nullable JobOpportunityStage stage, @Nullable String nextStep, @Nullable LocalDate dueDate) {

            //Copy across to SF fields
            if (stage != null) {
                put("StageName", stage.toString());
            }
            if (nextStep != null) {
                put("NextStep", nextStep);
            }
            if (dueDate != null) {
                put("Next_Step_Due_Date__c", dueDate.toString());
            }
        }
    }

    class EmployerOppNameUpdateRequest extends HashMap<String, String> {

        public EmployerOppNameUpdateRequest(@NotBlank String jobName) {
            put("Name", jobName);
        }
    }

    @Getter
    @Setter
    @ToString
    class RecordTypeField {

        public String Name;

        public RecordTypeField(String recordTypeName) {
            Name = recordTypeName;
        }
    }

    /**
     * See doc for {@link ContactRequest}
     */
    class CandidateOpportunityRequest extends LinkedHashMap<String, Object> {

        public CandidateOpportunityRequest(
            @Nullable CompositeAttributes attributes,
            String recordType, Candidate candidate,
            SalesforceJobOpp jobOpportunity, boolean create) {

            if (attributes != null) {
                setAttributes(attributes);
            }

            setRecordType(new RecordTypeField(recordType));

            String candidateNumber = candidate.getCandidateNumber();
            setExternalCandidateOppId(makeExternalId(candidateNumber, jobOpportunity.getSfId()));

            User user = candidate.getUser();
            Partner partner = user.getPartner();

            //Update candidate partner Salesforce account id
            if (partner != null) {
                String partnerSfAccountId = partner.getSfId();
                if (partnerSfAccountId != null) {
                    setSourcePartnerAccountId(partnerSfAccountId);
                }
            }

            if (create) {
                setName(generateCandidateOppName(candidate, jobOpportunity));

                setAccountId(jobOpportunity.getAccountId());
                setCandidateContactId(candidate.getSfId());
                setOwnerId(jobOpportunity.getOwnerId());
                setParentOpportunityId(jobOpportunity.getSfId());

                LocalDateTime close = LocalDateTime.now().plusYears(1);
                setCloseDate(close.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        }

        public String getName() {
            return (String) get("Name");
        }

        public void setAttributes(CompositeAttributes attributes) {
            put("attributes", attributes);
        }

        /**
         * This is the Salesforce record type for this opportunity. There are two types of TBB
         * Candidate opportunity: "Candidate recruitment" and the Canada specific "Candidate
         * recruitment (CAN)"
         *
         * @param recordType Salesforce record type
         */
        public void setRecordType(RecordTypeField recordType) {
            put("RecordType", recordType);
        }

        /**
         * Id of associated Account.
         */
        public void setAccountId(String accountId) {
            put("AccountId", accountId);
        }

        /**
         * Id of associated Candidate contact record
         */
        public void setCandidateContactId(String candidateContactId) {
            put("Candidate_Contact__c", candidateContactId);
        }

        /**
         * Required field for creating opportunity - set to a year from now
         */
        public void setCloseDate(String closeDate) {
            put("CloseDate", closeDate);
        }

        /**
         * Comments explaining why the opportunity was closed
         */
        public void setClosingComments(String comments) {
            put("Closing_Comments__c", comments);
        }

        public void setClosingCommentsForCandidate(String commentsForCandidate) {
            put("Closing_Comments_For_Candidate__c", commentsForCandidate);
        }

        /**
         * Employer feedback notes
         */
        public void setEmployerFeedback(String employerFeedback) {
            put("Employer_Feedback__c", employerFeedback);
        }

        /**
         * Name of candidate job opportunity
         */
        public void setName(String name) {
            put("Name", name);
        }

        /**
         * ID job opportunity owner
         */
        public void setOwnerId(String ownerId) {
            put("OwnerId", ownerId);
        }

        /**
         * Id of associated Job opportunity record
         */
        public void setParentOpportunityId(String jobOpportunityId) {
            put("Parent_Opportunity__c", jobOpportunityId);
        }

        /**
         * Id of associated source partner account record
         */
        public void setSourcePartnerAccountId(String sourcePartnerAccountId) {
            put("Source_Partner__c", sourcePartnerAccountId);
        }

        /**
         * Set to stage - default is "Prospect"
         */
        public void setStageName(String stageName) {
            put("StageName", stageName);
        }

        /**
         * Opportunity next step
         */
        public void setNextStep(String nextStep) {
            put("NextStep", nextStep);
        }

        /**
         * Opportunity next step due date
         */
        public void setNextStepDueDate(LocalDate nextStepDueDate) {
            put("Next_Step_Due_Date__c", nextStepDueDate.toString());
        }

        /**
         * This is the unique external id that defines all the candidate job opportunities that we
         * are going to "upsert". It is constructed from the candidate id and the job opportunity
         * id.
         */
        public void setExternalCandidateOppId(String externalCandidateOppId) {
            put(candidateOpportunitySFFieldName, externalCandidateOppId);
        }

        public void setRelocatingBoys(Integer relocatingBoys) {
            put("Relocating_boys__c", relocatingBoys);
        }

        public void setRelocatingGirls(Integer relocatingGirls) {
            put("Relocating_girls__c", relocatingGirls);
        }

        public void setRelocatingMen(Integer relocatingMen) {
            put("Relocating_men__c", relocatingMen);
        }

        public void setRelocatingWomen(Integer relocatingWomen) {
            put("Relocating_women__c", relocatingWomen);
        }

        // These two categories are for when gender given as 'other' or not specified by candidate.
        // Labeled as such on SF, so it should be quite clear to end users of the stats.
        public void setRelocatingChildren(Integer relocatingChildren) {
            put("Relocating_children_gender_other__c", relocatingChildren);
        }

        public void setRelocatingAdults(Integer relocatingAdults) {
            put("Relocating_adults_gender_other__c", relocatingAdults);
        }
    }

    /**
     * See doc for {@link ContactRequest}
     */
    class JobOpportunityRequest extends LinkedHashMap<String, Object> {

        public JobOpportunityRequest(
            @Nullable CompositeAttributes attributes, SalesforceJobOpp jobOpp) {

            if (attributes != null) {
                setAttributes(attributes);
            }

            setRecordType(new RecordTypeField("Employer job"));

            Partner partner = jobOpp.getJobCreator();

            //Update job creator partner Salesforce account id
            if (partner != null) {
                String partnerSfAccountId = partner.getSfId();
                if (partnerSfAccountId != null) {
                    setRecruiterPartnerAccountId(partnerSfAccountId);
                }
            }

            setName(jobOpp.getName());

            setAccountId(jobOpp.getEmployerEntity().getSfId());
            //todo Close date and other fields - including fields defaulting from Employer (Account) - eg office size etc, Country

            setClosingComments(jobOpp.getClosingComments());

            final JobOpportunityStage stage = jobOpp.getStage();
            String stageName = stage == null ? null : stage.getSalesforceStageName();

            setStageName(stageName);
            setNextStep(jobOpp.getNextStep());
            setNextStepDueDate(jobOpp.getNextStepDueDate());
            setClosingComments(jobOpp.getClosingComments());

            LocalDateTime close = LocalDateTime.now().plusYears(1);
            setCloseDate(close.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            setExternalId(jobOpp.getId().toString());
        }

        public String getName() {
            return (String) get("Name");
        }

        public void setAttributes(CompositeAttributes attributes) {
            put("attributes", attributes);
        }

        /**
         * This is the Salesforce record type for job opportunities.
         *
         * @param recordType Salesforce record type
         */
        public void setRecordType(RecordTypeField recordType) {
            put("RecordType", recordType);
        }

        /**
         * Id of associated Account.
         */
        public void setAccountId(String accountId) {
            put("AccountId", accountId);
        }

        /**
         * Required field for creating opportunity - set to a year from now
         */
        public void setCloseDate(String closeDate) {
            put("CloseDate", closeDate);
        }

        /**
         * Comments explaining why the opportunity was closed
         */
        public void setClosingComments(String comments) {
            put("Closing_Comments__c", comments);
        }

        /**
         * Name of opportunity
         */
        public void setName(String name) {
            put("Name", name);
        }

        /**
         * ID of opportunity owner
         */
        public void setOwnerId(String ownerId) {
            put("OwnerId", ownerId);
        }

        /**
         * Id of associated recruiter partner account record
         */
        public void setRecruiterPartnerAccountId(String partnerAccountId) {
            put("Recruiter_Partner__c", partnerAccountId);
        }

        /**
         * Set to stage - default is "Prospect"
         */
        public void setStageName(String stageName) {
            put("StageName", stageName);
        }

        /**
         * Opportunity next step
         */
        public void setNextStep(String nextStep) {
            put("NextStep", nextStep);
        }

        /**
         * Opportunity next step due date
         */
        public void setNextStepDueDate(@Nullable LocalDate nextStepDueDate) {
            put("Next_Step_Due_Date__c", nextStepDueDate == null ? null : nextStepDueDate.toString());
        }

        /**
         * This is the unique external id that defines all the job opportunities that we
         * are going to "upsert".
         */
        public void setExternalId(String externalId) {
            put(jobOpportunitySFFieldName, externalId);
        }
    }

    /**
     * See doc for {@link ContactRequestComposite}
     */
    @Getter
    @Setter
    @ToString
    class OpportunityRequestComposite implements HasSize {

        public boolean allOrNone = false;
        public List<CandidateOpportunityRecordComposite> records = new ArrayList<>();

        @Override
        public int checkSize() {
            return records.size();
        }
    }

    /**
     * See doc for {@link ContactRecordComposite}
     */
    @Getter
    @Setter
    @ToString(callSuper = true)
    class CandidateOpportunityRecordComposite extends CandidateOpportunityRequest {

        public CandidateOpportunityRecordComposite(String recordType, Candidate candidate,
            SalesforceJobOpp jobOpportunity, boolean create) {
            super(new CompositeAttributes("Opportunity"),
                recordType, candidate, jobOpportunity, create);
        }
    }

    @Getter
    @Setter
    static class CompositeAttributes {

        public String type;

        public CompositeAttributes(String type) {
            this.type = type;
        }
    }

    interface HasSize {

        /**
         * Note that it is not getSize, so that it doesn't look like an attribute when the body is
         * being extracted.
         *
         * @return size
         */
        int checkSize();
    }

    private Map<String, Integer> processSfCaseRelocationInfo(CandidateOpportunity candidateOpportunity,
        Candidate relocatingCandidate) throws NoSuchObjectException {

        // Get the dependants if any (can return null and processing will continue, which we want)
        List<CandidateDependant> relocatingDependants = getRelocatingDependants(candidateOpportunity);

        // Initiate values to populate SF candidate opp relocation info fields
        int relocatingBoys = 0;
        int relocatingGirls = 0;
        int relocatingMen = 0;
        int relocatingWomen = 0;
        // These two categories are for when gender given as 'other' or not specified by candidate.
        // Labeled as such on SF, so it should be quite clear to end users of the stats.
        int relocatingAdults = 0;
        int relocatingChildren = 0;

        // Process dependents if any and update values accordingly
        if(relocatingDependants != null) {
            for (CandidateDependant relocatingDependant : relocatingDependants) {

                // At present DOB can be null at intake, so we need to escape error here by
                // specifying a value — a dependant is statistically most likely to be a child.
                boolean isChild = true;

                if(relocatingDependant.getDob() != null) {
                    isChild = (Period.between(relocatingDependant.getDob(), LocalDate.now()))
                        .getYears() < 18;
                }

                // If gender check returns null, attribute to gender non-specific count
                // (It's also not a required field.)
                Gender gender = relocatingDependant.getGender();

                if (isChild) {
                    if (Gender.male == gender) {
                        relocatingBoys++;
                    } else if (Gender.female == gender) {
                        relocatingGirls++;
                    } else {
                        relocatingChildren++;
                    }
                } else {
                    if (Gender.male == gender) {
                        relocatingMen++;
                    } else if (Gender.female == gender) {
                        relocatingWomen++;
                    } else {
                        relocatingAdults++;
                    }
                }
            }
        }

        // Process candidate who will always be an adult
        Gender gender = relocatingCandidate.getGender();
        if(Gender.male == gender) {
            relocatingMen++;
        } else if(Gender.female == gender) {
            relocatingWomen++;
        } else {
            relocatingAdults++;
        }

        Map<String, Integer> relocationInfo = new HashMap<>();
        relocationInfo.put("relocatingBoys", relocatingBoys);
        relocationInfo.put("relocatingGirls", relocatingGirls);
        relocationInfo.put("relocatingChildren", relocatingChildren);
        relocationInfo.put("relocatingMen", relocatingMen);
        relocationInfo.put("relocatingWomen", relocatingWomen);
        relocationInfo.put("relocatingAdults", relocatingAdults);

        return relocationInfo;
    }

    /**
     * Gets relocating dependants listed on a given candidate opportunity. Methods sits here as it causes a
     * circular dependency if it sits on the candidate opportunity service
     * @param candidateOpportunity instance of {@link CandidateOpportunity}
     * @return list of candidate dependant objects or null if there aren't any for that assessment
     * @throws NoSuchObjectException if there's no candidate dependant with a given id
     */
    private List<CandidateDependant> getRelocatingDependants(CandidateOpportunity candidateOpportunity)
        throws NoSuchObjectException {
        List<Long> relocatingDependantIds = candidateOpportunity.getRelocatingDependantIds();

        return relocatingDependantIds != null ?
            relocatingDependantIds
                .stream()
                .map(candidateDependantService::getDependant)
                .collect(Collectors.toList()) : null;
    }

    @Override
    public void updateSfCaseRelocationInfo(CandidateOpportunity candidateOpportunity)
        throws NoSuchObjectException, SalesforceException, WebClientException {

        // Get the relocating candidate and add to list
        Candidate relocatingCandidate = candidateOpportunity.getCandidate();
        List<Candidate> candidateList = Collections.singletonList(relocatingCandidate);

        // Process the relocation info and add to candidate opportunity params
        Map<String, Integer> relocationInfo = processSfCaseRelocationInfo(
            candidateOpportunity, relocatingCandidate);

        CandidateOpportunityParams candidateOppParams = new CandidateOpportunityParams();
        candidateOppParams.setRelocationInfo(relocationInfo);

        // Get the SF job opp that this visa assessment and candidate opp relate to
        SalesforceJobOpp sfJobOpp = candidateOpportunity.getJobOpp();

        // Update the candidate opp
        createOrUpdateCandidateOpportunities(candidateList, candidateOppParams, sfJobOpp);
    }

}
