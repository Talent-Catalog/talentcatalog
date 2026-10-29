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

package org.tctalent.server.service.explanation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.util.http.RecordingHttpServer.json;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.tctalent.server.service.embedding.dto.EmbeddingConfigurationVersion;
import org.tctalent.server.service.embedding.dto.EmbeddingErrorCode;
import org.tctalent.server.service.embedding.dto.EmbeddingInput;
import org.tctalent.server.service.embedding.dto.EmbeddingInputType;
import org.tctalent.server.service.embedding.dto.EmbeddingModelDetails;
import org.tctalent.server.service.embedding.dto.EmbeddingsRequest;
import org.tctalent.server.service.embedding.dto.EmbeddingsResponse;
import org.tctalent.server.service.embedding.dto.TcVectorEmbeddingServiceClient;
import org.tctalent.server.service.embedding.dto.TcVectorEmbeddingServiceClientConfig;
import org.tctalent.server.service.explanation.dto.CandidateMatchExplanationServiceClient;
import org.tctalent.server.service.explanation.dto.CandidateMatchExplanationServiceClientConfig;
import org.tctalent.server.service.explanation.dto.ExplanationCandidateInput;
import org.tctalent.server.service.explanation.dto.ExplanationExperienceInput;
import org.tctalent.server.service.explanation.dto.ExplanationResult;
import org.tctalent.server.service.explanation.dto.ExplanationsRequest;
import org.tctalent.server.service.explanation.dto.ExplanationsResponse;
import org.tctalent.server.util.http.RecordingHttpServer;

/**
 * Regression tests for the JSON exchanged with our Python services (match explanations and vector
 * embeddings).
 * <p>
 * The clients are built exactly as in production, from Spring Boot's auto-configured
 * RestClient.Builder, and talk to a real local HTTP server. So these tests verify the actual
 * snake_case JSON put on the wire and decoded from it by whatever JSON mapper Spring Boot
 * configures - independently of the Jackson version.
 * </p>
 * Added before the Spring Boot 4 (Jackson 3) migration - TC-1189.
 */
@SpringBootTest(classes = {
    CandidateMatchExplanationServiceClientConfig.class,
    TcVectorEmbeddingServiceClientConfig.class
})
@ImportAutoConfiguration({
    JacksonAutoConfiguration.class,
    HttpMessageConvertersAutoConfiguration.class,
    RestClientAutoConfiguration.class,
    ValidationAutoConfiguration.class
})
class PythonServiceClientsJsonPayloadTest {

    private static final RecordingHttpServer explanationsServer;
    private static final RecordingHttpServer embeddingsServer;

    static {
        try {
            explanationsServer = RecordingHttpServer.start(json(200, """
                {
                  "requested": 1,
                  "succeeded": 1,
                  "failed": 0,
                  "unexpected_new_field": "ignored",
                  "results": [
                    {
                      "candidate_id": "123",
                      "generated_at": "2026-10-02T13:30:00+10:00",
                      "model_name": "qwen.qwen3-235b-a22b-2507-v1:0",
                      "summary": "The candidate is a strong match.",
                      "experience_explanations": [
                        {
                          "experience_id": "456",
                          "job_title": "Senior Software Engineer",
                          "explanation": "Directly relevant experience."
                        },
                        {
                          "experience_id": "789",
                          "job_title": null,
                          "explanation": "Some transferable experience."
                        }
                      ],
                      "limitations": [],
                      "error": null
                    }
                  ]
                }
                """));
            embeddingsServer = RecordingHttpServer.start(json(200, """
                {
                  "model": {
                    "model_name": "all-MiniLM-L6-v2",
                    "configuration_version": "SPACY_PREPROCESSING_V3",
                    "dimensions": 3
                  },
                  "requested": 2,
                  "succeeded": 1,
                  "failed": 1,
                  "results": [
                    {"id": "a", "embedding": [0.1, -0.25, 1.0], "error": null},
                    {"id": "b", "embedding": null,
                     "error": {"code": "INVALID_TEXT", "message": "Text is empty"}}
                  ]
                }
                """));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void serverUrls(DynamicPropertyRegistry registry) {
        //Needed to create the production client beans. The tests build their own clients from the
        //same configuration classes, pointing at the test servers.
        registry.add("tc-vector-embedding-service.apiUrl", () -> "http://placeholder");
    }

    @AfterAll
    static void stopServers() {
        explanationsServer.close();
        embeddingsServer.close();
    }

    @Autowired
    private ObjectProvider<RestClient.Builder> restClientBuilders;

    @Test
    @DisplayName("explanations request is sent as snake_case json, including null fields")
    void explanationsRequestJson() throws Exception {
        CandidateMatchExplanationServiceClient client =
            clientFor(explanationsServer, CandidateMatchExplanationServiceClient.class);

        client.generateExplanations(ExplanationsRequest.builder()
            .opportunityDescription("Software engineer in Sydney")
            .candidates(List.of(ExplanationCandidateInput.builder()
                .candidateId("123")
                .experiences(List.of(
                    ExplanationExperienceInput.builder()
                        .experienceId("456")
                        .jobTitle("Senior Software Engineer")
                        .description("Built things")
                        .build(),
                    ExplanationExperienceInput.builder()
                        .experienceId("789")
                        .build()))
                .build()))
            .build());

        RecordingHttpServer.RecordedRequest request = explanationsServer.lastRequest();
        assertAll(
            () -> assertEquals("POST", request.method()),
            () -> assertTrue(request.uri().endsWith("/explanations"), request.uri()),
            () -> assertTrue(request.contentType().startsWith("application/json"),
                request.contentType())
        );
        assertJson("""
            {
              "opportunity_description": "Software engineer in Sydney",
              "candidates": [
                {
                  "candidate_id": "123",
                  "experiences": [
                    {
                      "experience_id": "456",
                      "job_title": "Senior Software Engineer",
                      "description": "Built things"
                    },
                    {
                      "experience_id": "789",
                      "job_title": null,
                      "description": null
                    }
                  ]
                }
              ]
            }
            """, request.body());
    }

    @Test
    @DisplayName("explanations response is decoded from snake_case json, with nested objects and nulls")
    void explanationsResponseJson() {
        CandidateMatchExplanationServiceClient client =
            clientFor(explanationsServer, CandidateMatchExplanationServiceClient.class);

        ExplanationsResponse response = client.generateExplanations(minimalExplanationsRequest());

        ExplanationResult result = response.getResults().get(0);
        assertAll(
            () -> assertEquals(1, response.getRequested()),
            () -> assertEquals(1, response.getSucceeded()),
            () -> assertEquals(0, response.getFailed()),
            () -> assertEquals("123", result.getCandidateId()),
            //The instant is preserved. (Spring Boot's Jackson configuration adjusts it to UTC.)
            () -> assertTrue(OffsetDateTime.parse("2026-10-02T03:30:00Z")
                .isEqual(result.getGeneratedAt()), String.valueOf(result.getGeneratedAt())),
            () -> assertEquals("qwen.qwen3-235b-a22b-2507-v1:0", result.getModelName()),
            () -> assertEquals("The candidate is a strong match.", result.getSummary()),
            () -> assertEquals(2, result.getExperienceExplanations().size()),
            () -> assertEquals("456", result.getExperienceExplanations().get(0).getExperienceId()),
            () -> assertEquals("Senior Software Engineer",
                result.getExperienceExplanations().get(0).getJobTitle()),
            () -> assertEquals("Directly relevant experience.",
                result.getExperienceExplanations().get(0).getExplanation()),
            () -> assertNull(result.getExperienceExplanations().get(1).getJobTitle()),
            () -> assertEquals(List.of(), result.getLimitations()),
            () -> assertNull(result.getError())
        );
    }

    @Test
    @DisplayName("embeddings request is sent as snake_case json, with enum names and null fields")
    void embeddingsRequestJson() throws Exception {
        TcVectorEmbeddingServiceClient client =
            clientFor(embeddingsServer, TcVectorEmbeddingServiceClient.class);

        client.generateEmbeddings(embeddingsRequest());

        RecordingHttpServer.RecordedRequest request = embeddingsServer.lastRequest();
        assertAll(
            () -> assertEquals("POST", request.method()),
            () -> assertTrue(request.uri().endsWith("/embeddings"), request.uri()),
            () -> assertTrue(request.contentType().startsWith("application/json"),
                request.contentType())
        );
        assertJson("""
            {
              "model": {
                "model_name": "all-MiniLM-L6-v2",
                "configuration_version": "SPACY_PREPROCESSING_V3",
                "dimensions": 3
              },
              "type": "DOCUMENT",
              "inputs": [
                {"id": "a", "context": "Job title", "text": "Software engineer"},
                {"id": "b", "context": null, "text": null}
              ]
            }
            """, request.body());
    }

    @Test
    @DisplayName("embeddings response is decoded from snake_case json, with enums, arrays and nulls")
    void embeddingsResponseJson() {
        TcVectorEmbeddingServiceClient client =
            clientFor(embeddingsServer, TcVectorEmbeddingServiceClient.class);

        EmbeddingsResponse response = client.generateEmbeddings(embeddingsRequest());

        assertAll(
            () -> assertEquals("all-MiniLM-L6-v2", response.getModel().getModelName()),
            () -> assertEquals(EmbeddingConfigurationVersion.SPACY_PREPROCESSING_V3,
                response.getModel().getConfigurationVersion()),
            () -> assertEquals(3, response.getModel().getDimensions()),
            () -> assertEquals(2, response.getRequested()),
            () -> assertEquals(1, response.getSucceeded()),
            () -> assertEquals(1, response.getFailed()),
            () -> assertEquals("a", response.getResults().get(0).getId()),
            () -> assertEquals(List.of(0.1, -0.25, 1.0), response.getResults().get(0).getEmbedding()),
            () -> assertNull(response.getResults().get(0).getError()),
            () -> assertNull(response.getResults().get(1).getEmbedding()),
            () -> assertEquals(EmbeddingErrorCode.INVALID_TEXT,
                response.getResults().get(1).getError().getCode()),
            () -> assertEquals("Text is empty", response.getResults().get(1).getError().getMessage())
        );
    }

    /**
     * Creates a client configured exactly as the production bean (same configuration class and
     * auto-configured RestClient.Builder), but pointing at the given test server.
     */
    private <T> T clientFor(RecordingHttpServer server, Class<T> clientType) {
        if (clientType == CandidateMatchExplanationServiceClient.class) {
            return clientType.cast(new CandidateMatchExplanationServiceClientConfig()
                .candidateMatchExplanationServiceClient(restClientBuilder(), server.baseUrl()));
        }
        return clientType.cast(new TcVectorEmbeddingServiceClientConfig()
            .tcVectorEmbeddingServiceClient(restClientBuilder(), server.baseUrl()));
    }

    private RestClient.Builder restClientBuilder() {
        //RestClient.Builder is a prototype bean so each call gets a fresh, fully customized builder
        return restClientBuilders.getObject();
    }

    private static ExplanationsRequest minimalExplanationsRequest() {
        return ExplanationsRequest.builder()
            .opportunityDescription("Software engineer")
            .candidates(List.of(ExplanationCandidateInput.builder()
                .candidateId("123")
                .experiences(List.of())
                .build()))
            .build();
    }

    private static EmbeddingsRequest embeddingsRequest() {
        return EmbeddingsRequest.builder()
            .model(EmbeddingModelDetails.builder()
                .modelName("all-MiniLM-L6-v2")
                .configurationVersion(EmbeddingConfigurationVersion.SPACY_PREPROCESSING_V3)
                .dimensions(3)
                .build())
            .type(EmbeddingInputType.DOCUMENT)
            .input(EmbeddingInput.builder().id("a").context("Job title").text("Software engineer")
                .build())
            .input(EmbeddingInput.builder().id("b").build())
            .build();
    }

    /**
     * Strict comparison: same fields (no more, no fewer), same values, explicit nulls must be
     * present, same array order. Object key order is not compared.
     */
    private static void assertJson(String expected, String actual) throws Exception {
        JSONAssert.assertEquals("Actual JSON: " + actual, expected, actual, JSONCompareMode.STRICT);
    }
}
