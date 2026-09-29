package org.tctalent.server.service.explanation.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** Configures the declarative HTTP client used to call the Python match explanation service. */
@Configuration
public class CandidateMatchExplanationServiceClientConfig {

    @Bean
    public CandidateMatchExplanationServiceClient candidateMatchExplanationServiceClient(
        RestClient.Builder restClientBuilder,
        @Value("${tc-match-explanation-service.apiUrl}") String apiUrl
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
