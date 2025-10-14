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

package org.tctalent.server.service.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.tctalent.server.configuration.properties.TcSkillsExtractionServiceProperties;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
public class TcSkillsExtractionServiceImpl implements TcSkillsExtractionService {

    private final TcSkillsExtractionServiceProperties properties;
    private final RestClient restClient;

    public TcSkillsExtractionServiceImpl(RestClient.Builder restClientBuilder,
        TcSkillsExtractionServiceProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.getApiUrl()).build();
    }

    @Override
    public ExtractSkillsResponse extractSkills(ExtractSkillsRequest request) throws RestClientException {
        return restClient.post()
            .uri("/extract_skills")
            .contentType(APPLICATION_JSON)
            .header("x-api-key", properties.getApiKey())
            .body(request)
            .retrieve()
            .body(ExtractSkillsResponse.class);

    }
}
