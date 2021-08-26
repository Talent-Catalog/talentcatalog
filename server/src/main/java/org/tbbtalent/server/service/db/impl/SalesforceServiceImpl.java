/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.SalesforceException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.model.sf.Opportunity;
import org.tbbtalent.server.request.candidate.SalesforceOppParams;
import org.tbbtalent.server.request.opportunity.UpdateEmployerOpportunityRequest;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.email.EmailHelper;
import reactor.core.publisher.Mono;

/**
 * Standard implementation of Salesforce service
 * <p/>
 * See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_what_is_rest_api.htm
 * <p/>
 * @see #executeUpsert and other execute methods for other refs to Salesforce doc.
 * <p/>
 * Basically we need to construct a simple Java object containing the required SF fields which will
 * be converted to a Json object by {@link WebClient} methods and included in the body of the
 * HTTP request sent to SF.
 * These Java objects are defined here in nested classes like: {@link ContactRequest} and 
 * {@link CandidateOpportunityRequest}.
 * <p/>
 * Operating on multiple records in a single HTTP request is called a "composite" request by SF.
 * See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/using_composite_resources.htm
 * In this code we use sObject Collections - 
 * see https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_composite_sobject_tree_flat.htm
 * Basically this means passing an array of records in the HTTP body. Each record is basically
 * the request object plus extra attributes. For example the record in a composite contact request
 * is a {@link ContactRecordComposite}, and the multiple contact request is a 
 * {@link ContactRequestComposite}, which is just an array of ContactRecordComposite (plus a couple
 * of other fields - eg allOrNone).
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
 *
 * @author John Cameron
 */
@Service
public class SalesforceServiceImpl implements SalesforceService, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SalesforceServiceImpl.class);
    private static final String apiVersion = "v51.0";
    private static final String candidateNumberSFFieldName = "TBBid__c";
    private static final String candidateOpportunitySFFieldName = "TBBCandidateExternalId__c";
    
    private boolean alertedDuplicateSFRecord = false;

    private final Map<Class<?>,String> classSfPathMap = new HashMap<>();
    private final Map<Class<?>,String> classSfCompositePathMap = new HashMap<>();

    private final String contactRetrievalFields = 
            "Id,AccountId," + candidateNumberSFFieldName;
     
    
    private final EmailHelper emailHelper;

    @Value("${salesforce.privatekey}")
    private String privateKeyStr;
    
    private PrivateKey privateKey;

    @Value("${salesforce.tbb.jordanAccount}")
    private String tbbJordanAccountId;

    @Value("${salesforce.tbb.lebanonAccount}")
    private String tbbLebanonAccountId;

    @Value("${salesforce.tbb.otherAccount}")
    private String tbbOtherAccountId;

    @Value("${salesforce.tbb.tbbAccount}")
    private String tbbAccountId;

    private final WebClient webClient;
    
    /**
     * This is the accessToken required for all Salesforce REST API
     * calls. It should appear in the Authorization header, prefixed by
     * "Bearer ".
     * <p/>
     * The token is retrieved from Salesforce by calling 
     * {@link #requestAccessToken()}
     * <p/>
     * Access tokens expire so they need to be rerequested periodically.
     */
    private String accessToken = null;

    @Autowired
    public SalesforceServiceImpl(EmailHelper emailHelper) {
        this.emailHelper = emailHelper;
        
        classSfPathMap.put(ContactRequest.class, "Contact");
        classSfPathMap.put(EmployerOpportunityRequest.class, "Opportunity");
        classSfCompositePathMap.put(ContactRequestComposite.class, "Contact");
        classSfCompositePathMap.put(OpportunityRequestComposite.class, "Opportunity");
        
        WebClient.Builder builder = 
                WebClient.builder()
                .baseUrl("https://talentbeyondboundaries.my.salesforce.com/services/data/" + apiVersion)
                .defaultHeader("Content_Type","application/json")
                .defaultHeader("Accept","application/json");

        webClient = builder.build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        privateKey = privateKeyFromPkcs8(privateKeyStr);
    }

    @Override
    @NonNull
    public Contact createContact(@NonNull Candidate candidate)
            throws GeneralSecurityException, WebClientException,
            SalesforceException {

        //Create a contact request using data from the candidate
        ContactRequest contactRequest = new ContactRequest(candidate);

        //And decode the response
        CreateRecordResult result = executeCreate(contactRequest);

        assert result != null;
        if (!result.success) {
            throw new SalesforceException("Create failed for candidate "
                    + candidate.getCandidateNumber()
                    + ": " + result.getErrorMessage());
        }


        Contact contact = new Contact(candidate);
        contact.setId(result.id);

        return contact;
    }

    @Override
    @NonNull
    public Contact createOrUpdateContact(@NonNull Candidate candidate)
            throws GeneralSecurityException, WebClientException,
            SalesforceException {

        //Create a contact request using data from the candidate
        ContactRequest contactRequest = new ContactRequest(candidate);
        
        //Upsert request bodies should not include the TBBid because it is used as the unique key
        //used to identify the record to be updated - specified in the PATCH uri, not in the
        //request body.
        contactRequest.setTBBid__c(null);

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
            throws GeneralSecurityException, WebClientException, SalesforceException {
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
                log.error("Update failed for candidate "
                        + candidate.getCandidateNumber()
                        + ": " + result.getErrorMessage());
            }
            contacts.add(contact);
        }
        
        return contacts;
    }

    @Override
    public void createOrUpdateCandidateOpportunities(
        List<Candidate> candidates, SalesforceOppParams salesforceOppParams, String sfJoblink) 
            throws GeneralSecurityException, WebClientException, SalesforceException {

        //Get id job opportunity.  
        String jobOpportunityId = extractIdFromSfUrl(sfJoblink);

        List<Candidate> candidatesToProcess;

        //Opportunity Stage is a required field. If don't have one, we can't touch existing 
        //candidate opportunities because we need to leave their existing stage unchanged, but
        //can create new opportunities - supplying the default initial stage of "Prospect".
        //If we have been supplied with a stage, then we can update and create - using that stage.

        String stageName = null;
        String nextStep = null;
        if (salesforceOppParams != null) {
            stageName = salesforceOppParams.getStageName();
            nextStep = salesforceOppParams.getNextStep();
        }
        boolean createOnly = stageName == null;
        
        if (!createOnly) {
            //We will be processing all candidates, creating new ones and updating existing ones
            //with the supplied stage.
            candidatesToProcess = candidates;
        } else {
            //We have no defined stage, so we will just be processing (ie creating) any missing
            //candidate opportunities.
            
            //We figure out which candidates have opportunities still to be
            //created by first creating a map of candidates indexed by their unique
            //opportunity id - constructed from the candidate number and the 
            //job opportunity id.
            Map<String, Candidate> idCandidateMap = new HashMap<>();
            for (Candidate candidate : candidates) {
                String id = makeExternalId(candidate.getCandidateNumber(),
                    jobOpportunityId);
                idCandidateMap.put(id, candidate);
            }

            //Now find all ids for existing candidate opportunities for this job.
            //Then remove candidates from the map with those ids (because there
            //is no need to create an opp for them - they have one)
            List<String> externalIds = findCandidateOpportunities(jobOpportunityId);
            for (String externalId : externalIds) {
                idCandidateMap.remove(externalId);
            }
            
            //The candidates to process will just be ones who need opportunities created
            candidatesToProcess = new ArrayList<>(idCandidateMap.values());
        }
        
        if (candidatesToProcess.size() > 0) {
            //First get some more info about the job: its name, its 
            //associated account and country
            Opportunity opportunity = findOpportunity(jobOpportunityId);
            String jobOpportunityName = opportunity == null ? null : opportunity.getName();
            if (jobOpportunityName == null) {
                throw new SalesforceException(
                    "Could not find name for job opportunity " + sfJoblink);
            }
            String jobAccountId = opportunity.getAccountId();
            String jobOwnerId = opportunity.getOwnerId();
            String country = opportunity.getAccountCountry__c();
            String recordType = "Candidate recruitment";
            if ("Canada".equals(country)) {
                recordType = "Candidate recruitment (CAN)";
            }

            //Now build requests of candidate opportunities we want to create
            List<CandidateOpportunityRecordComposite> opportunityRequests = new ArrayList<>();

            for (Candidate candidate : candidatesToProcess) {
                //Create a request using data from the candidate
                CandidateOpportunityRecordComposite opportunityRequest =
                    new CandidateOpportunityRecordComposite(recordType,
                        candidate, stageName, nextStep, jobOpportunityName, jobOpportunityId,
                        jobAccountId, jobOwnerId);
                opportunityRequests.add(opportunityRequest);
            }
            OpportunityRequestComposite req = new OpportunityRequestComposite();
            req.setRecords(opportunityRequests);

            //Execute and decode the response
            UpsertResult[] results =
                executeUpserts(candidateOpportunitySFFieldName, req);

            if (results.length != opportunityRequests.size()) {
                //This is a fatal error because if the numbers don't match we don't 
                //know how to match results to requests.
                throw new SalesforceException(
                    "Number of results (" + results.length
                        + ") did not match number of requests ("
                        + opportunityRequests.size() + ")");
            }

            //Log any failures
            int count = 0;
            for (CandidateOpportunityRecordComposite request : opportunityRequests) {
                UpsertResult result = results[count++];
                if (!result.isSuccess()) {
                    log.error("Update failed for opportunity "
                        + request.getName()
                        + ": " + result.getErrorMessage());
                }
            }
        }
    }

    /**
     * Extracts the Salesforce record id from the Salesforce url of a record.
     * @param url Url of a Salesforce record
     * @return Salesforce id or null if the url wasn't a valid record url 
     */
    public static @Nullable String extractIdFromSfUrl(String url) {
        return extractFieldFromSfUrl(url, 2);
    }

    /**
     * Extracts the Salesforce object type (ag Account, Contact, Opportunity) 
     * from the Salesforce url of a record.
     * @param url Url of a Salesforce record
     * @return Salesforce object type or null if the url wasn't a valid record url 
     */
    public static @Nullable String extractObjectTypeFromSfUrl(String url) {
        return extractFieldFromSfUrl(url, 1);
    }

    /**
     * Extracts the Salesforce record id from the Salesforce url of a record.
     * @param url Url of a Salesforce record
     * @return Salesforce id or null if the url wasn't a valid record url 
     */
    private static @Nullable String extractFieldFromSfUrl(String url, int fieldNum) {
        if (url == null) {
            return null;
        }

        //https://salesforce.stackexchange.com/questions/1653/what-are-salesforce-ids-composed-of
        String pattern =
                //This is the standard prefix for our Salesforce.        
                "https://talentbeyondboundaries.lightning.force.com/" +

                        //This part just checks for 15 or more "word" characters with
                        //no "punctuation" - eg . or /.
                        //That will be the Salesforce id.
                        //It should be preceeded by the record type surrounded
                        //by "/".
                        ".*/([\\w]+)/([\\w]{15,})[^\\w]?.*";

        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(url);

        final int groupCount = m.groupCount();
        if (m.find() && groupCount == 2) {
            return m.group(fieldNum);
        } else {
            return null;
        }
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
                        log.warn(msg);
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
    public List<Contact> findCandidateContacts() 
            throws GeneralSecurityException, WebClientException {
        return findContacts("TBBid__c > 0");
    }

    @Override
    public List<Contact> findContacts(String condition) 
            throws GeneralSecurityException, WebClientException {
        String query =
                "SELECT " + contactRetrievalFields + 
                        " FROM Contact WHERE " + condition;

        ClientResponse response = executeQuery(query);
        ContactQueryResult result = 
                response.bodyToMono(ContactQueryResult.class).block();

        //Retrieve the contact from the response 
        List<Contact> contacts = null;
        if (result != null) {
            contacts = result.records;
        }
        
        return contacts;
    }

    static class ContactQueryResult extends QueryResult {
        public List<Contact> records;
    }


    private List<String> findCandidateOpportunities(String jobOpportunityId) 
            throws GeneralSecurityException {
        String query =
                "SELECT " + candidateOpportunitySFFieldName +
                        " FROM Opportunity WHERE Parent_Opportunity__c='" +
                        jobOpportunityId + "'";

        ClientResponse response = executeQuery(query);

        OpportunityQueryResult result =
                response.bodyToMono(OpportunityQueryResult.class).block();
        
        //Retrieve the contact from the response 
        List<String> ids = new ArrayList<>();
        if (result != null) {
            for (OpportunityQueryResult.Opp record : result.records) {
                ids.add(record.TBBCandidateExternalId__c);
            }
        }

        return ids;
    }

    static class OpportunityQueryResult extends QueryResult {
        public List<Opp> records;

        static class Opp {
            public String TBBCandidateExternalId__c;
        }
    }

    @Override
    @Nullable
    public <T> T findRecordFieldsFromId(
            String objectType, String id, String fields, Class<T> cl) 
            throws GeneralSecurityException, WebClientException {
        ClientResponse response = executeRecordFieldsGet(objectType, id, fields);
        T result = response.bodyToMono(cl).block();
        return result;
    }

    @Override
    @Nullable
    public Opportunity findOpportunity(String sfId)
            throws GeneralSecurityException, WebClientException {
        Opportunity opportunity = null;
        if (sfId != null) {
            opportunity = findRecordFieldsFromId(
                    "Opportunity", sfId,
                    "Name,AccountId,OwnerId,AccountCountry__c", Opportunity.class);
        }
        return opportunity;
    }

    @Override
    public void updateContact(Candidate candidate) 
            throws GeneralSecurityException {
        //Create a contact request using data from the candidate
        ContactRequest contactRequest = new ContactRequest(candidate);

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
    public void updateEmployerOpportunity(UpdateEmployerOpportunityRequest request)
        throws GeneralSecurityException {
        
        String sfJoblink = request.getSfJoblink();

        //Get id of job opportunity.  
        String jobOpportunityId = extractIdFromSfUrl(sfJoblink);

        EmployerOpportunityRequest sfRequest = new EmployerOpportunityRequest(request);

        executeUpdate(jobOpportunityId, sfRequest);
    }

    /**
     * Execute general purpose Salesforce create.
     * <p/>
     * For details on Salesforce create, see 
     * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_sobject_create.htm     
     * @param obj Object supplying data used to create corresponding Salesforce
     *            record.
     * @return CreateRecordResult - contains SF id of created record.
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private CreateRecordResult executeCreate(Object obj)
            throws GeneralSecurityException, WebClientException {

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
     * For details on Salesforce update, see
     * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_update_fields.htm
     * <p/>
     * Note that there is no body in the response - so this method just returns void
     * @param id Salesforce id for the record
     * @param obj Object supplying data used to update the Salesforce record.
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private void executeUpdate(String id, Object obj)
            throws GeneralSecurityException, WebClientException {

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
        if (response.rawStatusCode() != 204) {
            WebClientException ex = response.createException().block();
            assert ex != null;
            throw ex;
        }
    }

    /**
     * Execute general purpose Salesforce upsert (insert - ie create - 
     * if it doesn't exist, otherwise update if it does) based on a unique
     * external id. For example TBBid__c, passing in a candidate's 
     * candidateNumber as the id.
     * <p/>
     * For details on Salesforce upsert, see
     * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_upsert.htm
     * <p/>
     * In recent versions of API (since 46.0) you always get a response with
     * a body containing the SF id.
     * <ul>
     *     <li>create - HTTP status 201 (Created)</li>
     *     <li>update - HTTP status 200 (OK) </li>
     * </ul>
     * @param externalIdName Name of SF field containing an unique id used to 
     *       identify SF records. For example the SF TBBid__c field on Contact
     *       records which we populate with candidate's candidateNumber.*                       
     * @param id Value of the externalID 
     * @param obj Object supplying data used to update the Salesforce record.
     * @return ClientResponse  
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private UpsertResult executeUpsert(String externalIdName, String id, Object obj)
            throws GeneralSecurityException, WebClientException {

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
     * @param externalIdName Name of SF field containing an unique id used to 
     *       identify SF records. For example the SF TBBid__c field on Contact
     *       records which we populate with candidate's candidateNumber.                       
     * @param obj Object supplying data used to update the Salesforce record.
     * @return ClientResponses
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private UpsertResult[] executeUpserts(String externalIdName, HasSize obj) 
            throws GeneralSecurityException {
        
        Class<?> cl = obj.getClass();
        String path = classSfCompositePathMap.get(cl);
        if (path == null) {
            throw new InvalidRequestException(
                    "No mapping to Salesforce for objects of class " + cl.getSimpleName());
        }

        if (obj.checkSize() > 200) {
            throw new InvalidRequestException(
                    "Too many records (" + obj.checkSize() 
                            + ") to update in one go. Maximum = 200." );
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
     * For details on Salesforce queries, see 
     * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_query.htm
     * @param query Query to be executed 
     * @return ClientResponse - can use bodyToMono method to extract into an object.
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private ClientResponse executeQuery(String query) 
            throws GeneralSecurityException, WebClientException {

        WebClient.RequestHeadersSpec<?> spec = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/query")
                        .queryParam("q", query).build());
        
        return executeWithRetry(spec);
    }

    /**
     * General purpose Get fields from a record of a given type
     * See https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_get_field_values.htm
     * @param objectType Type of record
     * @param id ID of record
     * @param fields Fields requested
     * @return ClientResponse resulting from Get
     * @throws GeneralSecurityException if there is a problem with our keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private ClientResponse executeRecordFieldsGet(
            String objectType, String id, String fields) 
            throws GeneralSecurityException, WebClientException {
        WebClient.RequestHeadersSpec<?> spec = webClient.get()
                .uri("/sobjects/" + objectType + "/" + id + 
                        "/?fields=" + fields);

        return executeWithRetry(spec);
    }

    /**
     * Executes a request with a retry if the accessToken validity has expired
     * in which case another accessToken is automatically requested.
     * @param spec for the request 
     * @return ClientResponse received
     * @throws GeneralSecurityException if there is a problem with our keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private ClientResponse executeWithRetry(WebClient.RequestHeadersSpec<?> spec) 
            throws GeneralSecurityException, SalesforceException {
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
        } catch (Exception ex ){
            //Do one automatic retry after a wait.
            log.warn("Problem with Salesforce connection" + ex);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Interrupted wait for Salesforce retry", ex);
            }
            clientResponse = spec.exchange().block();
        }
        if (clientResponse == null ||  clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
            log.info("Getting new token from Salesforce");
            //Get new token and try again
            accessToken = requestAccessToken();
            spec.headers(headers -> headers.put("Authorization",
                    Collections.singletonList("Bearer " + accessToken)));
            log.info("Connecting to Salesforce with new token");
            clientResponse = spec.exchange().block();
        }

        if (clientResponse == null) {
            throw new RuntimeException("Null client response to Salesforce request");
        } else if (clientResponse.rawStatusCode() == 300 || 
                clientResponse.rawStatusCode() == 400) {
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
        } else if (clientResponse.rawStatusCode() > 300) {
            WebClientException ex = clientResponse.createException().block();
            assert ex != null;
            throw ex;
        }
        return clientResponse;
    }

    /**
     * Requests an accessToken from Salesforce.
     * <p/>
     * This involves making a special, digitally signed, JWT Bearer Token 
     * request to SF.
     * <p/>
     * See
     * https://help.salesforce.com/articleView?id=remoteaccess_oauth_jwt_flow.htm&type=5
     * @return Access token provided by Salesforce
     * @throws GeneralSecurityException if there is a problem with our keys
     * and digital signing.
     */
    private @NonNull String requestAccessToken() throws GeneralSecurityException {
        String jwtBearerToken = makeJwtBearerToken();
        
        WebClient client = WebClient
                .create("https://login.salesforce.com/services/oauth2/token");
        
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
            throw new GeneralSecurityException("Null BearerTokenResponse");
        }
        
        return response.access_token;
    }
    
    /**
     * Returns a new JWTBearerToken valid for the next 3 minutes.
     * <p/>
     * The token is signed using TalentBeyondBoundaries private key associated
     * with the certificate associated with the tbbtalent "Connected App"
     * in TBB's Salesforce.
     * <p/>
     * This can be used to get a new access token from Salesforce using the
     * following HTTP request to SF:
     * POST https://login.salesforce.com/services/oauth2/token
     * ?grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=[bearer token]
     * 
     * Content-Type: application/x-www-form-urlencoded"
     * <p/>
     * If all goes well it will return a Bearer access token in a response like
     * this:
     * {
     *     "access_token": "00D1N000002EPj7!AR0AQIaT.sdLMZlsU0Jlm7EPrrWghps9K025kno0nGwg6nf3KmQ9maLukiy20hvnqI1VUw9CY7GhoLIdgN8QsdYOoAk91azE",
     *     "scope": "custom_permissions web openid api id full",
     *     "instance_url": "https://talentbeyondboundaries.my.salesforce.com",
     *     "id": "https://login.salesforce.com/id/00D1N000002EPj7UAG/0051N000005qGt3QAE",
     *     "token_type": "Bearer"
     * }
     * <p/>
     * This code is based on 
     * https://help.salesforce.com/articleView?id=remoteaccess_oauth_jwt_flow.htm&type=5
     * 
     * @return JWTBearerToken
     * @throws GeneralSecurityException If there are signing problems
     */
    private String makeJwtBearerToken() throws GeneralSecurityException {
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
        claimArray[0] = "3MVG9mclR62wycM3f9iy572tIEVmeyMN8eFW5h2BK7eD96hD19zYvpx1vup07kfpCidboRyF56WF3QjL7LAYl";
        claimArray[1] = "jcameron@talentbeyondboundaries.org";
        claimArray[2] = "https://login.salesforce.com";
        claimArray[3] = Long.toString( ( System.currentTimeMillis()/1000 ) + 180);
        MessageFormat claims;
        claims = new MessageFormat(claimTemplate);
        String payload = claims.format(claimArray);

        //Add the encoded claims object
        token.append(Encoders.BASE64URL.encode(payload.getBytes(StandardCharsets.UTF_8)));

        //Sign the JWT Header + "." + JWT Claims Object
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(token.toString().getBytes(StandardCharsets.UTF_8));
        String signedPayload = Encoders.BASE64URL.encode(signature.sign());

        //Separate with a period
        token.append(".");

        //Add the encoded signature
        token.append(signedPayload);

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
     * The request containing these multiple updates is represented by {@link ContactRequestComposite}
     */
    @Getter
    @Setter
    @ToString
    class ContactRequest {
        public String AccountId;
        public String FirstName;
        public String LastName;
        public String MailingCountry;
        public String Id;
        public Long TBBid__c;

        public ContactRequest(Candidate candidate) {
            final String country = candidate.getCountry().getName();
            this.MailingCountry = country;

            //Set account id based on candidate's country 
            switch (country) {
                case "Jordan":
                    AccountId = tbbJordanAccountId;
                    break;
                case "Lebanon":
                    AccountId = tbbLebanonAccountId;
                    break;
                default:
                    AccountId = tbbOtherAccountId;
            }

            User user = candidate.getUser();
            this.FirstName = user.getFirstName();
            this.LastName = user.getLastName();

            this.TBBid__c = Long.valueOf(candidate.getCandidateNumber());
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
        public CompositeAttributes attributes;

        public ContactRecordComposite(Candidate candidate) {
            super(candidate);
            attributes = new CompositeAttributes("Contact");
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
         * Link (url) to Google CVs folder
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
        
        public EmployerOpportunityRequest(UpdateEmployerOpportunityRequest request) {
            //Note that we only populate the CVs and JD folders - we don't bother with the 
            //root folder. Not really needed because TBB staff can get to it on Google Drive 
            // from the others.
            CVs_Folder__c = request.getFoldercvlink();
            Job_Description_Folder__c = request.getFolderjdlink();
            Talent_Catalog_List__c = request.getListlink();
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
    @Getter
    @Setter
    @ToString
    class CandidateOpportunityRequest {
        public RecordTypeField RecordType;
        /**
         * Id of associated Account.
         */
        public String AccountId;
        
        /**
         * Id of associated Candidate contact record
         */
        public String Candidate_Contact__c;

        /**
         * Required field for creating opportunity - set to a year from now
         */
        public String CloseDate;

        /**
         * Name of candidate job opportunity
         */
        public String Name;

        /**
         * ID job opportunity owner
         */
        public String OwnerId;

        /**
         * Id of associated Job opportunity record
         */
        public String Parent_Opportunity__c;

        /**
         * Set to stage - default is "Prospect" 
         */
        public String StageName = "Prospect";

        /**
         * Opportunity next step 
         */
        public String NextStep;
        
        /**
         * This is the unique external id that defines all the candidate 
         * job opportunities that we are going to "upsert". 
         * It is constructed from the candidate id and the job opportunity
         * id.
         */
        public String TBBCandidateExternalId__c;

        public CandidateOpportunityRequest(String recordType, Candidate candidate,
            String stageName, String nextStep,
            String jobOpportunityName,
            String jobOpportunityId,
            String jobAccountId, String jobOwnerId) {
            User user = candidate.getUser();
            String candidateNumber = candidate.getCandidateNumber();

            RecordType = new RecordTypeField(recordType);
            
            Name = user.getFirstName() + 
                    "(" + candidateNumber + ")-" + jobOpportunityName;

            TBBCandidateExternalId__c = 
                    makeExternalId(candidateNumber, jobOpportunityId); 

            AccountId = jobAccountId;
            Candidate_Contact__c = candidate.getSfId();
            OwnerId = jobOwnerId;
            Parent_Opportunity__c = jobOpportunityId;
            
            LocalDateTime close = LocalDateTime.now().plusYears(1);
            CloseDate = close.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            StageName = stageName;
            NextStep = nextStep;
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
        public CompositeAttributes attributes;

        public CandidateOpportunityRecordComposite(String recordType, Candidate candidate,
            String stageName, String nextStep,
            String jobOpportunityName,
            String jobOpportunityId,
            String jobAccountId, String jobOwnerId) {
            super(recordType, candidate, stageName, nextStep, jobOpportunityName, jobOpportunityId, jobAccountId, jobOwnerId);
            attributes = new CompositeAttributes("Opportunity");
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
         * Note that it is not getSize, so that it doesn't look like an
         * attribute when the body is being extracted.
         * @return size
         */
        int checkSize();
    }
}
