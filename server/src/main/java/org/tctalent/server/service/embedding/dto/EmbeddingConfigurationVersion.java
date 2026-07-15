package org.tctalent.server.service.embedding.dto;

/**
 * Identifies the preprocessing and embedding configuration used to generate
 * an embedding.
 *
 * <p>Existing configuration versions must not change behaviour after
 * embeddings have been generated and stored. A preprocessing change requires
 * a new configuration version.</p>
 */
public enum EmbeddingConfigurationVersion {
    SBERT_RAW_V1,
    SPACY_PREPROCESSING_V1,
    SPACY_PREPROCESSING_V2,
    SPACY_PREPROCESSING_V3
}
