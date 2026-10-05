package org.tctalent.server.service.explanation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Declarative HTTP client for the Python match explanation service. */
@Validated
@HttpExchange(accept = "application/json", contentType = "application/json")
public interface CandidateMatchExplanationServiceClient {

    /**
     * Requests explanations for a batch of candidates against a single opportunity description.
     *
     * <p>Change the path if the FastAPI endpoint uses a different route.</p>
     */
    @PostExchange("/explanations")
    @NotNull
    ExplanationsResponse generateExplanations(
        @Valid @RequestBody ExplanationsRequest request
    );
}
