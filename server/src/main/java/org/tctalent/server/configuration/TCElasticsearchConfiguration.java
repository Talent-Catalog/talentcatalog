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

package org.tctalent.server.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;
import org.tctalent.server.logging.LogBuilder;

/**
 * Elasticsearch configuration for Talent Catalog.
 * <p/>
 * See <a href="https://docs.spring.io/spring-data/elasticsearch/reference/migration-guides/migration-guide-4.4-5.0.html">
 *     Spring Doc on upgrading to new Elasticsearch API</a>
 *
 * @author John Cameron
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "org.tctalent.server.repository.es")
@Slf4j
public class TCElasticsearchConfiguration extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private List<String> uris;
    @Value("${spring.elasticsearch.username}")
    private String username;
    @Value("${spring.elasticsearch.password}")
    private String password;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        try {
            if (uris != null && !uris.isEmpty()) {
                URI uri = new URI(uris.get(0));
                String hostAndPort = uri.getAuthority();
                String protocol = uri.getScheme();
                boolean useSsl = "https".equals(protocol);

                LogBuilder.builder(log)
                    .action("ElasticsearchConfiguration")
                    .message("Connecting to Elasticsearch at " + hostAndPort)
                    .logInfo();

                ClientConfiguration.MaybeSecureClientConfigurationBuilder x
                    = ClientConfiguration.builder().connectedTo(hostAndPort);

                if (useSsl) {
                    x = (ClientConfiguration.MaybeSecureClientConfigurationBuilder)
                        x.usingSsl();
                }

                if (username != null && !username.isEmpty()) {
                    x = (ClientConfiguration.MaybeSecureClientConfigurationBuilder)
                        x.withBasicAuth(username, password);
                }
                return x.build();
            } else {
                throw new RuntimeException("Missing Elasticsearch URL");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Badly formatted Elasticsearch URL: " + uris, e);
        }
    }
}
