/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db.task;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Particular kind of task which simply involves the candidate answering a given question.
 *
 * @author John Cameron
 */
public interface QuestionTask extends Task {

    /**
     * Question which the candidate is required to answer.
     * @return Question to be answered.
     */
    @NonNull
    String getQuestion();

    /**
     * Initially null, this provides the candidate's answer to the question.
     * @return Answer to question - different subclasses code for different kinds of answers
     * - for example {@link YesNoQuestionTask} is for questions where the answer is a simple
     * yes or no.
     */
    @Nullable
    Object getAnswer();
}
