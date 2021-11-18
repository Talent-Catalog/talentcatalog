/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.service.db.impl;

import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.CandidateTodo;
import org.tbbtalent.server.request.todo.CreateCandidateTodoRequest;
import org.tbbtalent.server.request.todo.UpdateCandidateTodoRequest;
import org.tbbtalent.server.service.db.CandidateTodoService;

import java.util.List;

@Service
public class CandidateTodoServiceImpl implements CandidateTodoService {
    @Override
    public List<CandidateTodo> listCandidateTodos(Long candidateId) {
        return null;
    }

    @Override
    public CandidateTodo createCandidateTodo(CreateCandidateTodoRequest request) {
        return null;
    }

    @Override
    public CandidateTodo updateCandidateTodo(UpdateCandidateTodoRequest request) {
        return null;
    }

    @Override
    public void deleteCandidateTodo(Long id) {

    }
}
