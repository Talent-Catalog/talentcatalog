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
    this.addColumn("candidateNumber", "Candidate #", new PublishedDocFieldSource("candidateNumber"));
    this.addColumnWithLink("candidateNumberLinkCv", "Candidate # \n (link to CV)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("shareableCv.location"));
    this.addColumnWithLink("candidateNumberLinkTc", "Candidate # \n(link to TC)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("tcLink"));
    this.addColumn("contextNote", "Context Note", new PublishedDocFieldSource("contextNote"));
    this.addColumn("email", "Email", new PublishedDocFieldSource("user.email"));

    //todo These special feedback fields need to have flag containing feedback type
    this.addColumn("employerDecision", "Employer Decision", null);
    this.addColumn("employerFeedback", "Employer Feedback", null);

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
      const config = this.getDefaultColumnConfigFromKey(exportColumn.key);
      if (config != null) {
        const props = config.columnProps;
        if (exportColumn.properties != null) {
          props.header = exportColumn.properties?.header;
          props.constant = exportColumn.properties?.constant;
        }
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

  private getDefaultColumnConfigFromKey(columnKey: string): PublishedDocColumnConfig {
    const columnDef = this.getColumnDefFromKey(columnKey);
    if (columnDef == null) {
      return null;
    }
    const config = new PublishedDocColumnConfig();
    config.columnDef = columnDef;
    config.columnProps = new PublishedDocColumnProps();
    return config;
  }

  public getDefaultColumns(): PublishedDocColumnConfig[] {
    const columns: PublishedDocColumnConfig[] = [];
    columns.push(this.getDefaultColumnConfigFromKey("candidateNumberLinkCv"));
    columns.push(this.getDefaultColumnConfigFromKey("name"));
    columns.push(this.getDefaultColumnConfigFromKey("shareableNotes"));
    columns.push(this.getDefaultColumnConfigFromKey("contextNote"));
    return columns;
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
