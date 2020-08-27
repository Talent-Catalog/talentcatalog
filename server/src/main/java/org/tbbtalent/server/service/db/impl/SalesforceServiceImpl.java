/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

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
import org.tbbtalent.server.exception.NotImplementedException;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.service.db.SalesforceService;
import org.tbbtalent.server.service.db.email.EmailHelper;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;

/**
 * Standard implementation of Salesforce service
 *
 * @author John Cameron
 */
@Service
public class SalesforceServiceImpl implements SalesforceService, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SalesforceServiceImpl.class);

    private boolean alertedDuplicateSFRecord = false; 
    
    private final EmailHelper emailHelper;

    @Value("${salesforce.privatekey}")
    private String privateKeyStr;
    
    private PrivateKey privateKey;

    private final WebClient webClient;
    
    /**
     * This is the accessToken required for for all Salesforce REST API
     * calls. It should appear in the Authroization header, prefixed by
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
        
        WebClient.Builder builder = 
                WebClient.builder()
                .baseUrl("https://talentbeyondboundaries.my.salesforce.com/services/data/v49.0")
                .defaultHeader("Content_Type","application/json")
                .defaultHeader("Accept","application/json");

        webClient = builder.build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        privateKey = privateKeyFromPkcs8(privateKeyStr);
    }

    @Override
    @Nullable
    public Contact findContact(@NonNull String tbbId) 
            throws GeneralSecurityException, WebClientException {
        //Note that the fields requested in the query should match the fields
        //in the Contact record.
        String query = 
                "SELECT+Name,Id,TBBId__c+FROM+Contact+WHERE+TBBId__c=" + tbbId;
        
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
                        final String msg = "Candidate number " + tbbId + 
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
     * Executes a request with a retry if the accessToken validity has expired
     * in which case another accessToken is automatically requested.
     * @param spec for the request 
     * @return ClientResponse received
     * @throws GeneralSecurityException if there is a problem with our keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    private ClientResponse executeWithRetry(WebClient.RequestHeadersSpec<?> spec) 
            throws GeneralSecurityException, WebClientException {
        if (accessToken == null) {
            accessToken = requestAccessToken();
            spec.headers(headers -> headers.put("Authorization",
                        Collections.singletonList("Bearer " + accessToken)));
        }
        ClientResponse clientResponse = spec.exchange().block();
        if (clientResponse == null ||  clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
            //Get new token and try again
            accessToken = requestAccessToken();
            spec.headers(headers -> headers.put("Authorization",
                    Collections.singletonList("Bearer " + accessToken)));
            clientResponse = spec.exchange().block();
            if (clientResponse == null) {
                throw new RuntimeException("Null client response to Salesforce request");
            } else if (clientResponse.rawStatusCode() >= 400) {
                WebClientException ex = clientResponse.createException().block();
                assert ex != null;
                throw ex;
            }
        }
        return clientResponse;
    }

    @Override
    @NonNull
    public Contact createContact(@NonNull String tbbId) {
        //TODO JC createContact not implemented in SalesforceServiceImpl
        throw new NotImplementedException("SalesforceServiceImpl", "createContact");
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

    public static class BearerTokenResponse {
        public String access_token;
        public String scope;
        public String instance_url;
        public String id;
        public String token_type;
    }

    public static class ContactQueryResult {
        public int totalSize;
        public boolean done;
        public List<Contact> records;
        
    }
    
}
