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

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Defines how a column is set up in a generated sheet
 * <p/>
 * Null values are ignored.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocColumnSetUp {

  /**
   * Horizontal alignment: CENTER, LEFT, RIGHT
   */
  @Nullable
  private String alignment;

  /**
   * Pixel width of column
   */
  @Nullable
  private Integer columnSize;

  /**
   * Drop down data entry, selected from one of the list of Strings.
   * <p/>
   * (This is defined as Data Validation in the sheet)
   */
  @Nullable
  private List<String> dropDowns;

  /**
   * NamedRange defined in sheet corresponding to the data in this column (excluding the header)
   */
  @Nullable
  private String rangeName;
}
