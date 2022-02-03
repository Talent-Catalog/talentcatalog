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
import {PublishedDocColumnType, PublishedDocColumnWidth} from "../model/base";

@Injectable({
  providedIn: 'root'
})
export class PublishedDocColumnService {

  private allColumnInfosMap = new Map<string, PublishedDocColumnDef>();

  constructor() {
    //Keep empty column first, so we know the index and can sort at the end.
    this.addColumn("emptyColumn", "Empty Column", null);

    this.addColumn("candidateNumber", "Candidate #",
      new PublishedDocFieldSource("candidateNumber"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("candidateNumberLinkCv", "Candidate # \n (link to CV)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("shareableCv.url"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("candidateNumberLinkTc", "Candidate # \n(link to TC)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("tcLink"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumn("contextNote", "Context Note",
      new PublishedDocFieldSource("contextNote"))
    .width = PublishedDocColumnWidth.Wide;

    this.addColumn("dob", "DOB", new PublishedDocFieldSource("dob"));

    this.addColumn("email", "Email", new PublishedDocFieldSource("user.email"));

    //These are special employer feedback fields (ie not display only fields, which are the default type)
    this.addColumnWithType("employerDecision", "Employer\nDecision",
      PublishedDocColumnType.EmployerCandidateDecision, null)
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithType("employerFeedback", "Employer\nFeedback",
      PublishedDocColumnType.EmployerCandidateNotes, null)
    .width = PublishedDocColumnWidth.Wide;

    this.addColumn("firstName", "First Name", new PublishedDocFieldSource("user.firstName"));

    this.addColumn("gender", "Gender", new PublishedDocFieldSource("gender"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumn("id", "Candidate id", new PublishedDocFieldSource("id"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumn("ieltsScore", "IELTS Score", new PublishedDocFieldSource("ieltsScore"));
    this.addColumn("lastName", "Last Name", new PublishedDocFieldSource("user.lastName"));
    this.addColumn("location", "Location", new PublishedDocFieldSource("country.name"));
    this.addColumn("city", "City", new PublishedDocFieldSource("city"));
    this.addColumn("state", "State", new PublishedDocFieldSource("state"));
    this.addColumn("name", "Name", new PublishedDocFieldSource("user"));
    this.addColumn("nationality", "Nationality", new PublishedDocFieldSource("nationality.name"));

    // Summary fields
    this.addColumn("occupations", "Occupations", new PublishedDocFieldSource("occupationSummary"));
    this.addColumn("englishExams", "English Exams", new PublishedDocFieldSource("englishExamsSummary"));
    this.addColumn("educations", "Education", new PublishedDocFieldSource("educationsSummary"));
    this.addColumn("certifications", "Certifications", new PublishedDocFieldSource("certificationsSummary"));

    this.addColumn("shareableNotes", "Notes", new PublishedDocFieldSource("shareableNotes"))
    .width = PublishedDocColumnWidth.Wide;

    this.addColumnWithLink("doc", "Other document", new PublishedDocConstantSource("doc"),
      new PublishedDocFieldSource("shareableDoc.url"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("cv", "CV", new PublishedDocConstantSource("cv"),
      new PublishedDocFieldSource("shareableCv.url"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("address", "Folder: Address",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Address"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("character", "Folder: Character",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Character"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("employer", "Folder: Employer",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Employer"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("identity", "Folder: Identity",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Identity"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("medical", "Folder: Medicals",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Medicals"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("qualification", "Folder: Qualification",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Qualification"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("registration", "Folder: Registration",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("subfolder.Registration"))
    .width = PublishedDocColumnWidth.Narrow;
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

  private addColumnWithTypeLink(
    key: string, name: string, type: PublishedDocColumnType, value: PublishedDocValueSource,
    link: PublishedDocValueSource): PublishedDocColumnDef {
    const info = new PublishedDocColumnDef(key, name);
    info.type = type;
    info.content.value = value;
    info.content.link = link;
    this.allColumnInfosMap.set(key, info);
    return info;
  }

  private addColumnWithLink(key: string, name: string, value: PublishedDocValueSource,
                            link: PublishedDocValueSource): PublishedDocColumnDef {
    return this.addColumnWithTypeLink(key, name, PublishedDocColumnType.DisplayOnly,
      value, link);
  }

  private addColumn(key: string, name: string, value: PublishedDocValueSource): PublishedDocColumnDef {
    return this.addColumnWithLink(key, name, value, null);
  }

  private addColumnWithType(key: string, name: string, type: PublishedDocColumnType,
                            value: PublishedDocValueSource): PublishedDocColumnDef {
    return this.addColumnWithTypeLink(key, name, type, value, null);
  }
}
