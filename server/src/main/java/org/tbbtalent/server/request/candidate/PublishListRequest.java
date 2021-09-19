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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.tbbtalent.server.model.db.ExportColumn;
import org.tbbtalent.server.model.db.SavedList;

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
  private List<PublishedDocColumnConfig> columns;
  
  public Set<ExportColumn> getExportColumns(SavedList savedList) {
    Set<ExportColumn> exportColumns = new HashSet<>();
    int index = 0;
    for (PublishedDocColumnConfig config : columns) {
      ExportColumn col = new ExportColumn();
      col.setSavedList(savedList);
      col.setIndex(index++);
      col.setKey(config.getColumnDef().getKey());
      col.setProperties(config.getColumnProps());
      exportColumns.add(col);
    }
    return exportColumns;
  }

  /**
   * Applies any extra column configuration to the standard column definitions returning
   * a modified version of the columns definitions, including the requested configuration
   * - eg changed header, changed constant value.
   * @return Column definitions after applying requested configuration.
   */
  public List<PublishedDocColumnDef> getConfiguredColumns() {
    List<PublishedDocColumnDef> columnInfos = new ArrayList<>();
    for (PublishedDocColumnConfig columnConfig : columns) {
      PublishedDocColumnDef info = columnConfig.getColumnDef();
      columnInfos.add(info);
      PublishedDocColumnProps props = columnConfig.getColumnProps();
      
      //Now apply any properties to the default definitions
      if (props != null) {

        String header = props.getHeader();
        if (header != null) {
          info.setHeader(header);
        }

        String constant = props.getConstant();
        if (constant != null) {
          PublishedDocValueSource valueSource = info.getContent().getValue();
          if (valueSource instanceof PublishedDocConstantSource) {
            valueSource.setConstant(constant);
          }
        }
      }
    }
    
    return columnInfos; 
  } 
}
