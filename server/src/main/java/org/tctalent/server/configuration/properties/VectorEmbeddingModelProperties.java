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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
   * This identifies the default embedding model to be used for matching.
   * <p>
   *     It should match a modelKey in an {@link EmbeddingModel} entity.
   * </p>
   */
  private String defaultEmbeddingModelKey;
}
