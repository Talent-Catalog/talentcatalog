// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.embedding;

import java.util.Map;
import org.springframework.lang.NonNull;
import org.tctalent.server.service.embedding.dto.EmbeddingResult;
import org.tctalent.server.service.embedding.dto.EmbeddingsResponse;

/**
 * Service interface for generating vector embeddings from source texts.
 * This service provides a method to generate embeddings using a specified embedding model and
 * returns the results along with any associated errors.
 *
 * @author John Cameron
 */
public interface TcVectorEmbeddingService {
    /**
     * Generates embeddings for the given source texts using the specified embedding model.
     *
     * @param modelKey The key of the embedding model to use.
     * @param sourceTexts      A map of source text identifiers to their corresponding text
     *                         content.
     * @return A response containing the generated embeddings and any associated errors.
     */
    @NonNull
    EmbeddingsResponse generateEmbeddings(String modelKey, Map<String, String> sourceTexts);

    /**
     * Generates an embedding for a single source text using the specified embedding model.
     *
     * @param modelKey The key of the embedding model to use.
     * @param sourceText The source text for which to generate an embedding.
     * @param isQueryText A flag indicating whether the source text is a query text. Some
     *                    embedding models may treat query texts differently from other types of
     *                    texts. For example,
     *                    <a href="https://huggingface.co/BAAI/bge-base-en-v1.5">BAAI/bge-base-en-v1.5</a>
     * @return The result containing the generated embedding or any associated error.
     */
    @NonNull
    EmbeddingResult generateEmbedding(String modelKey, String sourceText, boolean isQueryText);
}
