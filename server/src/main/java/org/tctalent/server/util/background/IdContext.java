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

package org.tctalent.server.util.background;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Context based on an id which identifies the last item processed.
 * The context also specifies how many items to process in a call.
 * <p/>
 * This is an example of CONTEXT that can be used to configure a {@link BackRunner} and
 * {@link BackProcessor}.
 *
 * @author John Cameron
 */
@Getter
@Setter
@AllArgsConstructor
public class IdContext {

    /**
     * The last processed id. null if none processed yet
     */
    private Long lastProcessedId;

    /**
     * The number of ids to process during each processing call.
     */
    private long numToProcess;

    /**
     * Enables count across entire process of any other processing variable, e.g. no. of records
     * actually updated.
     */
    private Long count;
}
