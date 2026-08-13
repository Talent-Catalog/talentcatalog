/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.embedding.dto;

/**
 * Defines the type of input for generating embeddings. The input can either be a document or a query.
 * <p>
 * Some embedding models embed query texts differently from the text being searched.
 * <p>
 * For example, some models will embed a job description differently from a CV,
 * so that the two can be compared more meaningfully.
 * @author John Cameron
 */
public enum EmbeddingInputType {
    /**
     * The input is a document, such as a CV, which will be the target of matching searches.
     */
    DOCUMENT,

    /**
     * The input is a query, such as a job search query.
     */
    QUERY
}
