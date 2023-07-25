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

package org.tbbtalent.server.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

/**
 * Based on 
 * https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.clients.rest
 * and
 * https://www.baeldung.com/spring-data-elasticsearch-tutorial
 * 
 * @author John Cameron
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "org.tbbtalent.server.repository.es")
public class ElasticsearchConfiguration extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private List<String> uris;
    @Value("${spring.elasticsearch.username}")
    private String username;
    @Value("${spring.elasticsearch.password}")
    private String password;

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchConfiguration.class);

    @Override
    @Bean
    public @NonNull RestHighLevelClient elasticsearchClient() {
        try {
            if (uris != null && uris.size() > 0) {
                URI uri = new URI(uris.get(0));
                String hostAndPort = uri.getAuthority();
                String protocol = uri.getScheme();
                boolean useSsl = "https".equals(protocol);
                
                log.info("Connecting to Elasticsearch at " + hostAndPort);
                
                ClientConfiguration.MaybeSecureClientConfigurationBuilder x
                        = ClientConfiguration.builder().connectedTo(hostAndPort);

                if (useSsl) {
                    x = (ClientConfiguration.MaybeSecureClientConfigurationBuilder) 
                            x.usingSsl();
                }
                
                if (username != null && username.length() > 0) {
                    x = (ClientConfiguration.MaybeSecureClientConfigurationBuilder) 
                            x.withBasicAuth(username, password);                    
                }
                ClientConfiguration clientConfiguration = x.build();

                return RestClients.create(clientConfiguration).rest();
            } else {
                throw new RuntimeException("Missing Elasticsearch URL");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Badly formatted Elasticsearch URL: " + uris, e);
        }
    }
}
