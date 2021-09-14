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
    this.addColumn("id", "Candidate id", null, new PublishedDocFieldSource("id"));
    this.addColumn("candidateNumber", "Candidate number", null, new PublishedDocFieldSource("candidateNumber"));
    this.addColumn("contextNote", "Context Note", null, new PublishedDocFieldSource("contextNote"));
    this.addColumn("email", "Email", null, new PublishedDocFieldSource("user.email"));
    this.addColumn("firstName", "First Name", null, new PublishedDocFieldSource("user.firstName"));
    this.addColumn("gender", "Gender", null, new PublishedDocFieldSource("gender"));
    this.addColumn("ieltsScore", "IELTS", null, new PublishedDocFieldSource("ieltsScore"));
    this.addColumn("interviewDate", "Interview Date", null, null);
    this.addColumn("interviewTime", "Interview Time", null, null);
    this.addColumn("interviewPanel", "Interview Panel", null, null);
    this.addColumn("interviewFeedback", "Interview Feedback", null, null);
    this.addColumn("lastName", "Last Name", null, new PublishedDocFieldSource("user.lastName"));
    this.addColumn("name", "Name", "Full name", new PublishedDocFieldSource("user"));
    this.addColumn("offer", "Offer?", null, null);
    this.addColumn("shareableNotes", "Notes", null, new PublishedDocFieldSource("shareableNotes"));
    this.addColumnWithLink("doc", "Other document", null, new PublishedDocConstantSource("doc"),
      new PublishedDocFieldSource("shareableDoc.location"));
    this.addColumnWithLink("cv", "CV", null, new PublishedDocConstantSource("cv"),
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

  private addColumnWithLink(key: string, name: string, header: string,
                         value: PublishedDocValueSource, link: PublishedDocValueSource) {
    const info = new PublishedDocColumnInfo(key, name, header);
    info.content.value = value;
    info.content.link = link;
    this.allColumnInfosMap.set(key, info);
  }

  private addColumn(key: string, name: string, header: string, value: PublishedDocValueSource) {
    this.addColumnWithLink(key, name, header, value, null);
  }
}
