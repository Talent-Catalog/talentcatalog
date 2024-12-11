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

/**
 * Published doc columns can be configured in three sizes based on their width.
 * <p/>
 * The width can be used to determine other automated formatting - eg alignment.
 *
 * @author John Cameron
 */
public enum PublishedDocColumnWidth {
  /**
   * Narrow columns are good for small amounts of data - eg a candidate number, or a status.
   * <p/>
   * Automated formatting may choose to center align the values in narrow columns.
   */
  Narrow,

  /**
   * This is the default column width and does not need to be specified. It can be used for
   * candidate names, for example.
   * <p/>
   * It doesn't trigger any automated formatting. The defaults for the template from which a doc
   * is created will be used.
   */
  Medium,

  /**
   * Wide columns are good for holding descriptive text - eg candidate descriptions or
   * employer feedback.
   * <p/>
   * Automated formatting will typically choose to left justify the text.
   */
  Wide
}
