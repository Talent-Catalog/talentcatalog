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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to "publish" a list, ie to create a shareable external doc from the candidates in the
 * list.
 */
@Getter
@Setter
public class PublishListRequest {

  /**
   * Defines the columns of data to be displayed in the doc for each candidate in a list.
   */
  private List<PublishedDocColumnInfo> columns;

  /**
   * Retrieves the column keys associated with the columns in this request.
   * @return List of keys.
   */
  public List<String> getExportColumnKeys() {
    List<String> keys = new ArrayList<>();
    for (PublishedDocColumnInfo column : columns) {
      keys.add(column.getKey());
    }
    return keys;
  }
}
