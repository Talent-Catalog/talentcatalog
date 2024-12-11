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

/**
 * Defines the data which defines a column in a published Google Sheet doc
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocColumnDef {

  /**
   * This the unique id of this column.
   * <p/>
   * Candidate sources store these in their exportColumns field inside ExportColumn objects,
   * representing the last used list of columns used to publish with.
   */
  private String key;

  /**
   * The type of column. The default column type is display only.
   * Other types of column allow different types of feedback which we can process.
   */
  private PublishedDocColumnType type;

  /**
   * Width of column best suited to display of the column data
   */
  private PublishedDocColumnWidth width;

  /**
   * This string appears as the column header
   */
  private String header;

  /**
   * This represents the data used to construct cells in a particular column.
   * If the column contains a link, this will be the value that is displayed, plus the link
   * if someone clicks on that cell.
   */
  private PublishedDocColumnContent content = new PublishedDocColumnContent();

  public PublishedDocColumnDef() {
  }

  public PublishedDocColumnDef(String key, String header) {
    this.key = key;
    this.header = header;
  }
}
