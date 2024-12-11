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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Describes a particular published document column instance - eg a column in a spreadsheet.
 * <p/>
 * It comprises details of a standard column definition, plus optional special configuration
 * for this particular instance of that column.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocColumnConfig {

  @Nullable
  /**
   * Optional properties which can override some standard column configuration.
   */
  private PublishedDocColumnProps columnProps;

  @NonNull
  /**
   * The standard definition of this column.
   */
  private PublishedDocColumnDef columnDef;
}
