/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.model.db.task;

import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;

import java.util.List;

/**
 * Particular kind of task which simply involves the candidate answering a given question.
 * <p/>
 * The question to be answered is the name of the task.
 *
 * @author John Cameron
 */
public interface QuestionTask extends Task {

    /**
     * Allowable answers provided (eg Yes, No, Needs further discussion etc).
     */
    @Nullable
    List<String> getExplicitAllowedAnswers();

    /**
     * Get allowed answers to this question, or null if there are no restrictions on the answers.
     * @return Allowable answers
     */
    @Nullable
    List<AllowedQuestionTaskAnswer> getAllowedAnswers();

    /**
     * Set allowed answers to this question, or null if there are no restrictions on the answers.
     * @param allowedAnswers Allowable answers
     */
    void setAllowedAnswers(@Nullable List<AllowedQuestionTaskAnswer> allowedAnswers);

    /**
     * If not null, returns the name of the {@link Candidate} field
     * that the answer to the task should be stored in.
     * <p/>
     * For example "leftHomeNotes" if the answer should populate the
     * {@link Candidate#setLeftHomeNotes} field.
     * <p/>
     * If null, the answer will stored in a CandidateProperty using the {@link Task#getName()} as
     * the name of the property.
     * @return Candidate field name if answer is stored there, otherwise null is answer is stored
     * as a CandidateProperty.
     */
    @Nullable
    String getCandidateAnswerField();

    default TaskType getTaskType() {
        return TaskType.Question;
    }
}
