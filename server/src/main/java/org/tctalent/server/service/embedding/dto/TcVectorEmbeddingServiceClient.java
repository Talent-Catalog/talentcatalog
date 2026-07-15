package org.tctalent.server.service.embedding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Declarative HTTP client for the Python embedding service. */
@Validated
@HttpExchange(accept = "application/json", contentType = "application/json")
public interface TcVectorEmbeddingServiceClient {

    /**
     * Requests a batch of embeddings.
     *
     * <p>Change the path if the FastAPI endpoint uses a different route.</p>
     */
    @PostExchange("/embeddings")
    @NotNull
    GenerateEmbeddingsResponse generateEmbeddings(
        @Valid @RequestBody GenerateEmbeddingsRequest request
    );
}
