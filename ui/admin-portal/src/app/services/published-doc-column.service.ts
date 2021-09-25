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

import {Injectable} from '@angular/core';
import {
  ExportColumn,
  PublishedDocColumnConfig,
  PublishedDocColumnDef,
  PublishedDocColumnProps,
  PublishedDocConstantSource,
  PublishedDocFieldSource,
  PublishedDocValueSource
} from "../model/saved-list";

@Injectable({
  providedIn: 'root'
})
export class PublishedDocColumnService {

  private allColumnInfosMap = new Map<string, PublishedDocColumnDef>();

  constructor() {
    //Keep empty column first, so we know the index and can sort at the end.
    this.addColumn("emptyColumn", "Empty Column", null);
    this.addColumn("candidateNumber", "Candidate number", new PublishedDocFieldSource("candidateNumber"));
    this.addColumn("contextNote", "Context Note", new PublishedDocFieldSource("contextNote"));
    this.addColumn("email", "Email", new PublishedDocFieldSource("user.email"));
    this.addColumn("firstName", "First Name", new PublishedDocFieldSource("user.firstName"));
    this.addColumn("gender", "Gender", new PublishedDocFieldSource("gender"));
    this.addColumn("id", "Candidate id", new PublishedDocFieldSource("id"));
    this.addColumn("ieltsScore", "IELTS", new PublishedDocFieldSource("ieltsScore"));
    this.addColumn("lastName", "Last Name", new PublishedDocFieldSource("user.lastName"));
    this.addColumn("name", "Name", new PublishedDocFieldSource("user"));
    this.addColumn("shareableNotes", "Notes", new PublishedDocFieldSource("shareableNotes"));
    this.addColumnWithLink("doc", "Other document", new PublishedDocConstantSource("doc"),
      new PublishedDocFieldSource("shareableDoc.location"));
    this.addColumnWithLink("cv", "CV", new PublishedDocConstantSource("cv"),
      new PublishedDocFieldSource("shareableCv.location"));
  }

  getColumnConfigFromExportColumns(exportColumns: ExportColumn[]): PublishedDocColumnConfig[] {
    const columnConfigs: PublishedDocColumnConfig[] = [];
    for (const exportColumn of exportColumns) {
      const columnDef = this.getColumnDefFromKey(exportColumn.key);
      if (columnDef != null) {
        const config = new PublishedDocColumnConfig();
        config.columnDef = columnDef;
        const props = new PublishedDocColumnProps();
        if (exportColumn.properties != null) {
          props.header = exportColumn.properties?.header;
          props.constant = exportColumn.properties?.constant;
        }
        config.columnProps = props;
        columnConfigs.push(config);
      }
    }
    return columnConfigs;
  }

  getColumnConfigFromAllColumns(): PublishedDocColumnConfig[] {
    const columnConfigs: PublishedDocColumnConfig[] = [];
    for (const exportColumn of this.allColumnInfosMap) {
      const columnDef = this.getColumnDefFromKey(exportColumn[0]);
      if (columnDef != null) {
        const config = new PublishedDocColumnConfig();
        config.columnDef = columnDef;
        columnConfigs.push(config);
      }
    }
    return columnConfigs;
  }

  private getColumnDefFromKey(columnKey: string): PublishedDocColumnDef {
    return this.allColumnInfosMap.get(columnKey);
  }

  private addColumnWithLink(key: string, name: string,
                         value: PublishedDocValueSource, link: PublishedDocValueSource) {
    const info = new PublishedDocColumnDef(key, name);
    info.content.value = value;
    info.content.link = link;
    this.allColumnInfosMap.set(key, info);
  }

  private addColumn(key: string, name: string, value: PublishedDocValueSource) {
    this.addColumnWithLink(key, name, value, null);
  }
}
