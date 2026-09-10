package org.tctalent.server.service.embedding.dto;

/** Stable item-level error codes returned to the batch caller. */
public enum EmbeddingErrorCode {
    INVALID_TEXT,
    PREPROCESSING_FAILED,
    EMBEDDING_FAILED,
    INVALID_DIMENSIONS,
    INVALID_EMBEDDING
}
