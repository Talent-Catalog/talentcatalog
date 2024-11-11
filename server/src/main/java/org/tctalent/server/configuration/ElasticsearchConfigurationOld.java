/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * Based on
 * https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.clients.rest
 * and
 * https://www.baeldung.com/spring-data-elasticsearch-tutorial
 *
 * @author John Cameron
 */
//@Configuration
//@EnableElasticsearchRepositories(basePackages = "org.tctalent.server.repository.es")
@Slf4j
public class ElasticsearchConfigurationOld
//    extends AbstractElasticsearchConfiguration
{

    @Value("${spring.elasticsearch.uris}")
    private List<String> uris;
    @Value("${spring.elasticsearch.username}")
    private String username;
    @Value("${spring.elasticsearch.password}")
    private String password;

//    @Override
//    @Bean
//    public @NonNull RestHighLevelClient elasticsearchClient() {
//        try {
//            if (uris != null && uris.size() > 0) {
//                URI uri = new URI(uris.get(0));
//                String hostAndPort = uri.getAuthority();
//                String protocol = uri.getScheme();
//                boolean useSsl = "https".equals(protocol);
//
//                LogBuilder.builder(log)
//                    .action("ElasticsearchConfiguration")
//                    .message("Connecting to Elasticsearch at " + hostAndPort)
//                    .logInfo();
//
//                ClientConfiguration.MaybeSecureClientConfigurationBuilder x
//                        = ClientConfiguration.builder().connectedTo(hostAndPort);
//
//                if (useSsl) {
//                    x = (ClientConfiguration.MaybeSecureClientConfigurationBuilder)
//                            x.usingSsl();
//                }
//
//                if (username != null && username.length() > 0) {
//                    x = (ClientConfiguration.MaybeSecureClientConfigurationBuilder)
//                            x.withBasicAuth(username, password);
//                }
//                ClientConfiguration clientConfiguration = x.build();
//
//                return RestClients.create(clientConfiguration).rest();
//            } else {
//                throw new RuntimeException("Missing Elasticsearch URL");
//            }
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Badly formatted Elasticsearch URL: " + uris, e);
//        }
//    }
}
