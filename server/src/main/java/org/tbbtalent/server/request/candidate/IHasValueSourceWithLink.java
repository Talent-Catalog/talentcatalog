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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Something that has a value and a link. 
 * Objects like this can be used to construct hyperlinks - eg in a spreadsheet.  
 *
 * @author John Cameron
 */
public interface IHasValueSourceWithLink {
  @NonNull
  IValueSource getValueSource();
  
  @Nullable
  String getLink();
}
