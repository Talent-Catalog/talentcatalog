/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.configuration.properties;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.tctalent.server.model.db.embedding.EmbeddingModel;

/**
 * Configuration properties for our vector embeddings.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "vector-embedding-model")
public class VectorEmbeddingModelProperties {

  /**
   * This identifies the embedding model used by the JobExperienceEmbedding entity.
   * <p>
   *     It should match a modelKey in an {@link EmbeddingModel} entity.
   * </p>
   */
  private String embeddingModelKey;

  /**
   * Optional embedding model used to populate the {@link #alternateEmbeddingTable} table.
   * <p>
   *     It should match a modelKey in an {@link EmbeddingModel} entity.
   * </p>
   */
  @Nullable
  private String alternateEmbeddingModelKey;

  /**
   * This is the name of an optional alternate job experience embedding table. It can be used
   * to change to a different embedding model for candidate matching without
   * any down-time.
   * <p>
   *     If present, it should match a table name in the database.
   * </p>
   * <p>
   *     Vector embeddings of job experiences will be computed using the model specified by the
   *     {@link #alternateEmbeddingModelKey} and updated in the table as well
   *     as in the table associated with the JobExperienceEmbedding entity.
   * </p>
   * <p>
   *     There is also a standard SystemAdminApi batch that can be run to initialize the contents
   *     of this table by computing embeddings of all job experiences.
   * </p>
   */
  @Nullable
  private String alternateEmbeddingTable;

  @AssertTrue(message = "alternateEmbeddingModelKey must be set if alternateEmbeddingTable is set")
  public boolean isValid() {
    return alternateEmbeddingModelKey != null || alternateEmbeddingTable == null;
  }
}
