/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.model.db;

import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.task.AllowedQuestionTaskAnswer;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.model.db.task.TaskType;

/**
 * Default Implementation
 *
 * @author John Cameron
 */
@Entity(name="QuestionTask")
@DiscriminatorValue("QuestionTask")
@Getter
@Setter
public class QuestionTaskImpl extends TaskImpl implements QuestionTask {

    /**
     * Explicitly set allowed answers that come from the task creation.
     * If exist the question task answer format will be a dropdown.
     */
    @Nullable
    @Convert(converter = CommaDelimitedStringsConverter.class)
    private List<String> explicitAllowedAnswers;

    /**
     * Set by candidate answer field if present, otherwise if explicit answer is not null will be set as the explicit answers.
     * If allowedAnswers is not null, the quesiton task answer format will be a dropdown.
     * If allowedAnswers is null, the question answer format will be a text box.
     */
    @Transient
    @Nullable
    List<AllowedQuestionTaskAnswer> allowedAnswers;

    @Nullable
    private String candidateAnswerField;

    /*
      Note that this should not be necessary because the interface provides a default implementation
      but PropertyUtils does not find this taskType property if it is just provided by the default
      interface implementations. Looks like some kind of bug.
      - John Cameron
     */
    @Override
    public TaskType getTaskType() {
        return QuestionTask.super.getTaskType();
    }
}
