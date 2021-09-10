/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.db.Candidate;

/**
 * This defines where the data displayed on a published doc comes from.
 * <p/>
 * There are two implementations: one where the data displayed comes from a particular candidate
 * field, and another where the data is a constant.
 *
 * @author John Cameron
 */
public interface IValueSource {

  /**
   * Fetches the actual value from the source.
   * @param candidate Used if the data is associated with a particular candidate. Ignored otherwise.
   * @return The displayed data.
   */
  Object fetchValue(Candidate candidate);
}
