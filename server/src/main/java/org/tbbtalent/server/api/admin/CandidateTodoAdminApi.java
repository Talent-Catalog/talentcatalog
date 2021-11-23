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

package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.db.CandidateTodo;
import org.tbbtalent.server.request.todo.CreateCandidateTodoRequest;
import org.tbbtalent.server.request.todo.UpdateCandidateTodoRequest;
import org.tbbtalent.server.service.db.CandidateTodoService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-todo")
public class CandidateTodoAdminApi {
    private final CandidateTodoService candidateTodoService;

    @Autowired
    public CandidateTodoAdminApi(CandidateTodoService candidateTodoService) {
        this.candidateTodoService = candidateTodoService;
    }

    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long candidateId) {
        List<CandidateTodo> candidateTodos = this.candidateTodoService.listCandidateTodos(candidateId);
        return candidateTodoDto().buildList(candidateTodos);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@Valid @PathVariable("id") Long candidateId,
                                      @Valid @RequestBody CreateCandidateTodoRequest request) {
        request.setCandidateId(candidateId);
        CandidateTodo candidateTodo = candidateTodoService.createCandidateTodo(request);
        return candidateTodoDto().build(candidateTodo);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateTodoRequest request) {
        request.setId(id);
        CandidateTodo candidateTodo = this.candidateTodoService.updateCandidateTodo(request);
        return candidateTodoDto().build(candidateTodo);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        candidateTodoService.deleteCandidateTodo(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder candidateTodoDto() {
        return new DtoBuilder()
                .add("id")
                .add("type")
                .add("name")
                .add("completed")
                .add("admin")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }
}
