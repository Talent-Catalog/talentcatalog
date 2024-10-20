/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.util.batch;

/**
 * This wraps an Object which holds "context" for keeping track of where a {@link BatchProcessor}
 * is up to in its processing as managed by a {@link BatchRunner}.
 * <p/>
 * For example the context object might just contain a Long representing the page number of
 * candidates from a search, that we are processing a page at a time.
 * @author John Cameron
 */
public interface BatchContext {
  Object getContext();
  void setContext(Object context);
}
