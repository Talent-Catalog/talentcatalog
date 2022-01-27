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

package org.tbbtalent.server.model.db;

import lombok.Getter;
import lombok.Setter;

//todo Note for Caroline: This doesn't implement the UploadTask interface, and it duplicates
// fields from TaskImpl. It should subclass it normally (reflecting how the QuestionTask interface
//extends the Task interface). Let's go over this together.
//
//One issue we need to look into is how the way we implement inheritance using JPA affects
//our class design.

@Getter
@Setter
// todo this should be extending from the task interface?
public class QuestionTask {
    private String name;
    private String description;
    private String timeframe;
    private boolean adminOnly;

    private String question;

    // todo this should be an enum or another class? See UML
    private String answer;
}
