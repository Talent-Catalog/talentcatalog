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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

/**
 * Allowable answers to a given question
 *
 * @author John Cameron
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AllowedQuestionTaskAnswer {

    /**
     * Short name of answer. This often corresponds to the name of an enum. It is not displayed
     * to the user (see {@link #displayName} below), but it is the value that is passed back to
     * the server and it is the value actually stored.
     */
    @NonNull
    String name;

    /**
     * Displayable name of answer. This is what is presented to the user to select from.
     * It may be the same as {@link #name}, but not necessarily.
     */
    @NonNull
    String displayName;
}
