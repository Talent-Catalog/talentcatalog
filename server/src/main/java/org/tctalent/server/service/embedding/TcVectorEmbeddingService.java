/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package org.tctalent.server.service.embedding;

import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.service.embedding.dto.EmbeddingInput;
import org.tctalent.server.service.embedding.dto.EmbeddingInputType;
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
     * @param inputs A list of EmbeddingInputs containing the data to be embedded.
     * @param type See {@link #generateEmbedding}
     * @return A response containing the generated embeddings and any associated errors.
     */
    @NonNull
    EmbeddingsResponse generateEmbeddings(
        @NonNull String modelKey,
        @NonNull List<EmbeddingInput> inputs,
        @NonNull EmbeddingInputType type);

    /**
     * Generates an embedding for a single source text using the specified embedding model.
     *
     * @param modelKey The key of the embedding model to use.
     * @param context The context for which to generate an embedding.
     *                See {@link org.tctalent.server.service.embedding.dto.EmbeddingInput}
     * @param text The source text for which to generate an embedding.
     * @param type Indicates whether the source text is a query.
     *             Some embedding models may treat query texts differently from other types of text.
     *             For example,
     *             <a href="https://huggingface.co/BAAI/bge-base-en-v1.5">BAAI/bge-base-en-v1.5</a>
     * @return The result containing the generated embedding or any associated error.
     */
    @NonNull
    EmbeddingResult generateEmbedding(
        @NonNull String modelKey,
        @Nullable String context, @Nullable String text,
        @NonNull EmbeddingInputType type);
}
