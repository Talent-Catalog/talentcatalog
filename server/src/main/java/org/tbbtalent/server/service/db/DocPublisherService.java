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

package org.tbbtalent.server.service.db;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import org.tbbtalent.server.request.candidate.PublishedDocColumnSetUp;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Creates an external document from the given data - representing a number of rows of data
 * where each row consists of a fixed number of columns.
 * ie a typical "spreadsheet" structure.
 *
 * @author John Cameron
 */
public interface DocPublisherService {

  /**
   * Creates an external document with the given name from the given data 
   *
   * @param drive Drive where doc is created
   * @param folder Folder where doc is created
   * @param name Name of created document
   * @param dataRangeName Name of range for candidate data             
   * @param data Data which comprises the published document
   * @param props Extra properties to send to sheet. Each property key should correspond to 
   *              a named cell reference in the document template, and the value is the value
   *              to appear in that cell.
   * @param columnSetUpMap Defines how columns are formatted. In the map each key is a column number 
   *                       (starting at 0).
   * @return A link to the created document
   */
  String createPublishedDoc(GoogleFileSystemDrive drive, GoogleFileSystemFolder folder,
      String name, String dataRangeName, List<List<Object>> data, Map<String, Object> props,
      Map<Integer, PublishedDocColumnSetUp> columnSetUpMap) 
      throws GeneralSecurityException, IOException;

  /**
   * Looks for data from a number of given columns in an already published doc.
   * Reads the data for those columns that appear in the doc.
   * @param docUrl Url of published document
   * @param columnNamedRanges List of named ranges of columns to be read if present in doc 
   * @return Map of lists of column values read. The key of the map is the name of the column found.
   * @throws GeneralSecurityException if there are security problems accessing the document
   * @throws IOException if there are problems reading the document 
   */
  Map<String, List<Object>> readPublishedDocColumns(String docUrl, List<String> columnNamedRanges)
      throws GeneralSecurityException, IOException;
}
