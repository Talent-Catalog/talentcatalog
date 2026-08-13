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
package org.tctalent.server.model.db.embedding;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.AbstractDomainObject;

/**
 * Describes a particular embedding model.
 * <p>
 *     Embedding models cannot be mixed. Embedding vectors from different models have no
 *     relationship to each others and cannot be meaningfully compared.
 * </p>
 * <p>
 *     At any given time the server will be doing matches under a single embedding model.
 *     Moving from one model to another is a major transition which involves recomputing the
 *     embedding vectors for all candidates.
 * </p>
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "embedding_model")
@SequenceGenerator(name = "seq_gen", allocationSize = 1, sequenceName = "embedding_model_id_seq")
@NoArgsConstructor
public class EmbeddingModel extends AbstractDomainObject<Long> {

    /**
     * The same modelName can be configured or used differently.
     * For example, we currently do preprocessing of text using Spacy before computing the
     * embedding. This affects the generated vectors, so effectively defines a different
     * EmbeddingModel.
     * <p>
     *     For example, the configurationVersion corresponding to our current Spacy preprocessing
     *     is <code>SPACY_PREPROCESSING_V3</code>
     * </p>
     */
    private String configurationVersion;

    /**
     * The size of the embedding vector.
     * <p>
     * This is defined by the particular model. For example
     * <code>sentence-transformers/all-MiniLM-L6-v2</code> is designed for VECTOR(384).
     * </p>
     * Note that a Postgres VECTOR database embedding field must have a size specified,
     * e.g. VECTOR(384).
     * Note also that it is not recommended to declare a large VECTOR size, like VECTOR(10000) and
     * just not use some of the dimensions, like you would, for example, with a VARCHAR(x) field.
     * If you need two different size vectors, they will need to be in two separate tables.
     */
    private int dimensions;

    /**
     * This is a short internal key, specific to the TC, that we use to identify a particular
     * embedding model, including our configuration of that model.
     * <p>
     * It is a unique key of this table.
     * </p>
     * <p>
     *     For example, our unique model key for our <code>SPACY_PREPROCESSING_V3</code>
     *     configuration of the <code>sentence-transformers/all-MiniLM-L6-v2</code> model is
     *     <code>MINILM_L6_SPACY_V3</code>
     * </p>
     */
    private String modelKey;

    /**
     * The official name of the embedding model.
     * <p>
     * For example <code>sentence-transformers/all-MiniLM-L6-v2</code>
     * See <a href="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2">here</a>.
     * </p>
     */
    private String modelName;

    /**
     * Optional link to documentation on the model.
     * For example <a href="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2">here</a>.
     */
    @Nullable
    private String modelUrl;

    /**
     * The "namespace" of the model. Sometimes it matches the model's provider, e.g. "google" or
     * "microsoft". Sometimes it represents a project - for example "sentence-transformers".
     */
    private String provider;

    /**
     * The current status of this embedding model on the TC.
     */
    @Enumerated(EnumType.STRING)
    private EmbeddingModelStatus status;
}
