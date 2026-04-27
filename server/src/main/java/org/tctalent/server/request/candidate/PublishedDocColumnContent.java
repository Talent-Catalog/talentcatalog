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
import org.springframework.lang.Nullable;

/**
 * Represents the data behind the content of a column in a published Google Sheet document.
 * <p/>
 * Each column will either display a single value, or else it will display a link constructed of
 * two values: one which is the visible value and the other is the link that you
 * go to when you click on that value.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocColumnContent {

  /**
   * If not null, indicates the source of the data which will serve as a link (url) followed
   * when someone clicks on the column's displayed value.
   */
  @Nullable
  private PublishedDocValueSource link;

  /**
   * If not null, indicates the source of the data which is displayed in the column.
   */
  @Nullable
  private PublishedDocValueSource value;
}
