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
import org.tbbtalent.server.model.db.TodoItem;
import org.tbbtalent.server.request.todo.CreateCandidateTodoRequest;
import org.tbbtalent.server.request.todo.UpdateCandidateTodoRequest;
import org.tbbtalent.server.service.db.TodoItemService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/todo-item")
public class TodoItemAdminApi {
    private final TodoItemService todoItemService;

    @Autowired
    public TodoItemAdminApi(TodoItemService todoItemService) {
        this.todoItemService = todoItemService;
    }

    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long candidateId) {
        List<TodoItem> candidateTodos = this.todoItemService.listTodoItems(candidateId);
        return todoItemDto().buildList(candidateTodos);
    }

    @PostMapping("{id}")
    public Map<String, Object> create(@Valid @PathVariable("id") Long candidateId,
                                      @Valid @RequestBody CreateCandidateTodoRequest request) {
        request.setCandidateId(candidateId);
        TodoItem candidateTodo = todoItemService.createTodoItem(request);
        return todoItemDto().build(candidateTodo);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateTodoRequest request) {
        request.setId(id);
        TodoItem candidateTodo = this.todoItemService.updateTodoItem(request);
        return todoItemDto().build(candidateTodo);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        todoItemService.deleteTodoItem(id);
        return ResponseEntity.ok().build();
    }

    private DtoBuilder todoItemDto() {
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
