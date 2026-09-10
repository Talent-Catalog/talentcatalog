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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.task.AllowedQuestionTaskAnswer;
import org.tctalent.server.model.db.task.TaskType;

class QuestionTaskImplTest {

  @Test
  void getTaskTypeReturnsQuestion() {
    QuestionTaskImpl task = new QuestionTaskImpl();

    assertEquals(TaskType.Question, task.getTaskType());
  }

  @Test
  void lombokBackedQuestionFieldsCanBeSetAndRead() {
    QuestionTaskImpl task = new QuestionTaskImpl();

    List<String> explicitAllowedAnswers = List.of("Yes", "No", "Maybe");
    List<AllowedQuestionTaskAnswer> allowedAnswers = List.of(
        new AllowedQuestionTaskAnswer("Yes", "Yes"),
        new AllowedQuestionTaskAnswer("No", "No")
    );

    task.setExplicitAllowedAnswers(explicitAllowedAnswers);
    task.setAllowedAnswers(allowedAnswers);
    task.setCandidateAnswerField("leftHomeNotes");

    assertSame(explicitAllowedAnswers, task.getExplicitAllowedAnswers());
    assertSame(allowedAnswers, task.getAllowedAnswers());
    assertEquals("leftHomeNotes", task.getCandidateAnswerField());
  }

  @Test
  void lombokBackedQuestionFieldsAreNullByDefault() {
    QuestionTaskImpl task = new QuestionTaskImpl();

    assertNull(task.getExplicitAllowedAnswers());
    assertNull(task.getAllowedAnswers());
    assertNull(task.getCandidateAnswerField());
  }
}