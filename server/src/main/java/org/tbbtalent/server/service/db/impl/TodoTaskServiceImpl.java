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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.TodoTask;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.TodoTaskRepository;
import org.tbbtalent.server.request.todo.CreateCandidateTodoRequest;
import org.tbbtalent.server.request.todo.UpdateCandidateTodoRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.TodoTaskService;

import java.util.List;

@Service
public class TodoTaskServiceImpl implements TodoTaskService {

    private final AuthService authService;
    private final CandidateRepository candidateRepository;
    private final TodoTaskRepository todoTaskRepository;

    @Autowired
    public TodoTaskServiceImpl(AuthService authService,
                               CandidateRepository candidateRepository,
                               TodoTaskRepository todoTaskRepository) {
        this.authService = authService;
        this.candidateRepository = candidateRepository;
        this.todoTaskRepository = todoTaskRepository;
    }
    @Override
    public List<TodoTask> listTodoTasks(Long candidateId) {
        List<TodoTask> todos = this.todoTaskRepository.findByCandidateId(candidateId);
        return todos;
    }

    @Override
    public TodoTask createTodoTask(CreateCandidateTodoRequest request) {
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // Check a candidateTodo with the type name doesn't already exist (some todos are singular and some can have multiple...)
        TodoTask cd = todoTaskRepository.findByCandidateAndName(request.getCandidateId(), request.getName())
                .orElseThrow(() -> new EntityExistsException("A todo exists with this name."));

        // Create a new candidateTodo object to insert into the database
        TodoTask candidateTodo = new TodoTask();

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));

//        candidateTodo.setCandidate(candidate);
//        candidateTodo.setType(request.getType());
//        candidateTodo.setName(request.getName());
//        candidateTodo.setCompleted(request.isCompleted());
//        candidateTodo.setAuditFields(user);

        // Save the candidateTodo
        todoTaskRepository.save(candidateTodo);

        return candidateTodo;
    }

    @Override
    public TodoTask updateTodoTask(UpdateCandidateTodoRequest request) {
        return null;
    }

    @Override
    public void deleteTodoTask(Long id) {

    }
}
