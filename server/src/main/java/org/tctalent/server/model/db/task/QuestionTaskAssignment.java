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

package org.tctalent.server.model.db.task;

import org.springframework.lang.Nullable;

/**
 * Task assignment associated with a Question Task.
 *
 * @author John Cameron
 */
public interface QuestionTaskAssignment extends TaskAssignment {

    /**
     * Answer provided to question.
     * <p/>
     * Would normally be null if the assignment is not completed.
     * @return Answer to question
     */
    @Nullable
    String getAnswer();

    /**
     * Set the question's answer
     * @param answer Answer to question
     */
    void setAnswer(@Nullable String answer);

}
