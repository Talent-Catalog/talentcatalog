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

package org.tbbtalent.server.request.task;

import lombok.Getter;
import lombok.Setter;


//TODO Note for Caroline: This request and CreateUploadTaskRequest have a lot in common (name, description,
// timeframe, adminOnly) - so you should use inheritance. Both should subclass, say CreateTask,
// which has all those attributes - then the sub tasks just add what makes them different
// eg question and answer.

@Getter
@Setter
public class CreateQuestionTaskRequest {
    private String name;
    private String description;
    private String timeframe;
    private boolean adminOnly;

    private String question;
    private String answer;
}
