package org.tctalent.server.service.explanation.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configures the declarative HTTP client used to call the Python match explanation service.
 *
 * <p>The explanation endpoint is served by the same Python FastAPI application as the vector
 * embedding endpoint, so this reuses the {@code tc-vector-embedding-service.apiUrl} setting
 * rather than introducing a separately configurable URL. This may be split out (and that
 * property likely renamed to something more general, e.g. {@code tc-ai-service}) if the two
 * services are ever deployed independently.</p>
 */
@Configuration
public class CandidateMatchExplanationServiceClientConfig {

    @Bean
    public CandidateMatchExplanationServiceClient candidateMatchExplanationServiceClient(
        RestClient.Builder restClientBuilder,
        @Value("${tc-vector-embedding-service.apiUrl}") String apiUrl
    ) {
        RestClient restClient = restClientBuilder
            .baseUrl(apiUrl)
            .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();

        return proxyFactory.createClient(CandidateMatchExplanationServiceClient.class);
    }
}
