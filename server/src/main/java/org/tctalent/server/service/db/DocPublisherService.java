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

package org.tctalent.server.service.db;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import org.tctalent.server.request.candidate.PublishedDocColumnDef;
import org.tctalent.server.request.candidate.PublishedDocColumnSetUp;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Creates an external document from the given data - representing a number of rows of data
 * where each row consists of a fixed number of columns.
 * ie a typical "spreadsheet" structure.
 *
 * @author John Cameron
 */
public interface DocPublisherService {

  /**
   * Creates an external document with the given name ready to be populated with a given number
   * of rows of candidate data which will be populated later.
   * <p/>
   * The document is made viewable by anyone with a link to it.
   *
   * @param folder Folder where doc is created
   * @param name Name of created document
   * @param dataRangeName Name of range for candidate data
   * @param props Extra properties to send to sheet. Each property key should correspond to
   *              a named cell reference in the document template, and the value is the value
   *              to appear in that cell.
   * @param columnSetUpMap Defines how columns are formatted. In the map each key is a column number
   *                       (starting at 0).
   * @return A link to the created document
   * @throws GeneralSecurityException if there are security problems accessing the document
   * @throws IOException if there are communication problems accessing the document
   * @see #populatePublishedDoc
   */
  String createPublishedDoc(GoogleFileSystemFolder folder,
      String name, String dataRangeName, int nRowsData, Map<String, Object> props,
      Map<Integer, PublishedDocColumnSetUp> columnSetUpMap)
      throws GeneralSecurityException, IOException;

  /**
   * Populate candidate data in published doc with given link.
   * <p/>
   * This link would typically be returned from a call to {@link #createPublishedDoc}
   * </p>
   * This is designed to be run asynchronously, so that a doc can be created quickly by
   * {@link #createPublishedDoc} but it can be populated over time - which ca take a while depending
   * on how many candidates are to be displayed in the doc.
   * @param publishedDocLink Link to published doc
   * @param savedListId Id of list associated with the published doc
   * @param candidateIds Ids of candidates whose data is used to populate the doc. Note that this
   *                     will be the candidates in the list.
   * @param columnInfos Specifies the columns of data to be populated
   * @param publishedSheetDataRangeName The name of the range in the doc where data will be written
   * @throws GeneralSecurityException if there are security problems accessing the document
   * @throws IOException if there are communication problems accessing the document
   * @see #createPublishedDoc
   */
  void populatePublishedDoc(String publishedDocLink, long savedListId, List<Long> candidateIds,
      List<PublishedDocColumnDef> columnInfos, String publishedSheetDataRangeName)
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
