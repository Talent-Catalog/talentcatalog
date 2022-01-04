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

import org.springframework.lang.Nullable;

/**
 * Task where candidate is required to answer a yes/no question.
 *
 * @author John Cameron
 */
public interface YesNoQuestionTask extends QuestionTask {

    /**
     * Candidate's answer to the question.
     * @return True if answer is yes, False if no. Null if not answered.
     */
    @Nullable
    Boolean getAnswer();

    /**
     * Candidate may provide optional notes explaining their answer.
     * @return Optional notes
     */
    @Nullable
    String getNotes();
}
