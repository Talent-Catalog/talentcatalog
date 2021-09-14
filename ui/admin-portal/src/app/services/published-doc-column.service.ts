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

import { Injectable } from '@angular/core';
import {
  PublishedDocColumnInfo, PublishedDocConstantSource,
  PublishedDocFieldSource,
  PublishedDocValueSource
} from "../model/saved-list";
import {CandidateFieldInfo} from "../model/candidate-field-info";

@Injectable({
  providedIn: 'root'
})
export class PublishedDocColumnService {

  private allColumnInfosMap = new Map<string, PublishedDocColumnInfo>();

  constructor() {
    this.addColumn("id", "Candidate id", new PublishedDocFieldSource("id"));
    this.addColumn("candidateNumber", "Candidate number", new PublishedDocFieldSource("candidateNumber"));
    this.addColumn("contextNote", "Context Note", new PublishedDocFieldSource("contextNote"));
    this.addColumn("name", "Name", new PublishedDocFieldSource("user"));
    this.addColumn("firstName", "First Name", new PublishedDocFieldSource("user.firstName"));
    this.addColumn("lastName", "Last Name", new PublishedDocFieldSource("user.lastName"));
    this.addColumn("shareableNotes", "Notes", new PublishedDocFieldSource("shareableNotes"));
    this.addColumnWithLink("cv", "CV", new PublishedDocConstantSource("cv"),
      new PublishedDocFieldSource("shareableCv.location"));
  }

  getColumnInfosFromKeys(keys: string[]): PublishedDocColumnInfo[] {
    const columnInfos: PublishedDocColumnInfo[] = [];
    for (const columnKey of keys) {
      const columnInfo = this.getColumnInfoFromKey(columnKey);
      if (columnInfo != null) {
        columnInfos.push(columnInfo)
      }
    }
    return columnInfos;
  }

  private getColumnInfoFromKey(columnKey: string): PublishedDocColumnInfo {
    return this.allColumnInfosMap.get(columnKey);
  }

  private addColumnWithLink(key: string, header: string,
                         value: PublishedDocValueSource, link: PublishedDocValueSource) {
    const info = new PublishedDocColumnInfo(key, header);
    info.content.value = value;
    info.content.link = link;
    this.allColumnInfosMap.set(key, info);
  }

  private addColumn(key: string, header: string, value: PublishedDocValueSource) {
    this.addColumnWithLink(key, header, value, null);
  }
}
