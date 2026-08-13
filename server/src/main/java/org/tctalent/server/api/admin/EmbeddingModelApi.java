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

package org.tctalent.server.api.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.service.db.EmbeddingModelService;

/**
 * Access embedding models
 *
 * @author John Cameron
 */
@RestController
@RequestMapping("/api/admin/embedding-model")
@RequiredArgsConstructor
@Slf4j
public class EmbeddingModelApi {
    private final EmbeddingModelService embeddingModelService;

    @GetMapping("/ready")
    public ResponseEntity<List<EmbeddingModel>> getReadyModels() {
        return ResponseEntity.ok(embeddingModelService.getReadyModels());
    }
}
