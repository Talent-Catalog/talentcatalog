package org.tctalent.server.service.embedding.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** Configures the declarative HTTP client used to call the Python service. */
@Configuration
public class TcVectorEmbeddingServiceClientConfig {

    @Bean
    public TcVectorEmbeddingServiceClient tcVectorEmbeddingServiceClient(
        RestClient.Builder restClientBuilder,
        @Value("${tc-vector-embedding-service.apiUrl}") String apiUrl,
        @Value("${tc-vector-embedding-service.apiKey}") String apiKey
    ) {
        RestClient restClient = restClientBuilder
            .baseUrl(apiUrl)
            .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();

        return proxyFactory.createClient(TcVectorEmbeddingServiceClient.class);
    }
}
