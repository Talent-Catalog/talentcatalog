// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.embedding;

import java.util.Map;
import org.springframework.lang.NonNull;
import org.tctalent.server.service.embedding.dto.GenerateEmbeddingsResponse;

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
    GenerateEmbeddingsResponse generateEmbeddings(String modelKey, Map<String, String> sourceTexts);
}
