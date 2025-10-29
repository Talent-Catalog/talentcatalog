// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.service.db.impl;

import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.tctalent.server.configuration.properties.SpacySkillsExtractionApiProperties;
import org.tctalent.server.service.db.SkillsExtractionService;

/**
 * Service implementation for extracting
 * <a href="https://esco.ec.europa.eu/en/classification/skill_main">ESCO</a>
 * skills from text using
 * <a href="https://spacy.io/">Spacy</a>.
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class SpacySkillsExtractionServiceImpl implements SkillsExtractionService {
    private final SpacySkillsExtractionApiProperties properties;
    private final RestClient restClient;

    public SpacySkillsExtractionServiceImpl(RestClient.Builder restClientBuilder,
        SpacySkillsExtractionApiProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.getApiUrl()).build();
    }

    @Override
    public List<String> extractSkillsFromText(String text) throws RestClientException {
        return restClient.post()
            .uri("/extract-skills")
            .header("x-api-key", properties.getApiKey())
            .contentType(TEXT_PLAIN)
            .body(text)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
   }
}
