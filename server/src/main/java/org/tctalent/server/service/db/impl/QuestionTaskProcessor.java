/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.repository.db.CandidatePropertyRepository;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.TaskProcessor;

@Service
public class QuestionTaskProcessor implements TaskProcessor {
  private final CandidatePropertyRepository candidatePropertyRepository;
  private final AuthService authService;

  @Autowired
  public QuestionTaskProcessor(CandidatePropertyRepository candidatePropertyRepository, AuthService authService) {
    this.candidatePropertyRepository = candidatePropertyRepository;
    this.authService = authService;
  }

  @Override
  public TaskType getTaskType() {
    return TaskType.Question;
  }

  @Override
  public TaskAssignmentImpl createTaskAssignment() {
    return new QuestionTaskAssignmentImpl();
  }

  @Override
  public TaskAssignmentImpl completeTask(TaskAssignmentImpl assignment, TaskCompletionContext context) {
    if (!(assignment.getTask() instanceof QuestionTask)) {
      throw new IllegalArgumentException("Task is not a QuestionTask");
    }
    QuestionTask questionTask = (QuestionTask) assignment.getTask();
    Candidate candidate = assignment.getCandidate();
    Long candidateId = context.candidateId;
    if (!candidate.getId().equals(candidateId)) {
      throw new InvalidSessionException("Candidate does not match task assignment");
    }

    String answer = context.fieldAnswers.get("answer");
    if (answer != null && !answer.trim().isEmpty()) {
      String propertyName = questionTask.getCandidateAnswerField() != null
          ? questionTask.getCandidateAnswerField()
          : questionTask.getName();
      CandidateProperty prop = new CandidateProperty();
      prop.setCandidateId(candidateId);
      prop.setName(propertyName);
      prop.setValue(answer);
      prop.setRelatedTaskAssignment(assignment);
      candidatePropertyRepository.save(prop);
    }

    assignment.setCompletedDate(OffsetDateTime.now());
    return assignment;
  }

  @Override
  public void handleCompletion(TaskAssignmentImpl assignment) {
    // No specific post-completion logic for QuestionTask
  }
}
