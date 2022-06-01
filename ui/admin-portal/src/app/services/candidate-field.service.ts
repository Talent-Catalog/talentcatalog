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
import {DatePipe, TitleCasePipe} from "@angular/common";
import {CandidateFieldInfo} from "../model/candidate-field-info";
import {AuthService} from "./auth.service";
import {CandidateSource, Status} from "../model/base";
import {Candidate, checkIeltsScoreType, ResidenceStatus} from "../model/candidate";
import {
  checkForAbandoned,
  checkForCompleted,
  checkForOngoing,
  checkForOverdue,
  TaskAssignment
} from "../model/task-assignment";
import {Role} from "../model/user";

@Injectable({
  providedIn: 'root'
})
export class CandidateFieldService {

  //Note - if you want to use any other pipes for formatting, you also need to
  //add them to providers array in app.module.ts.
  //See https://stackoverflow.com/a/48785621/929968
  private dateFormatter = (value) => this.datePipe.transform(value, "yyyy-MM-dd");
  private titleCaseFormatter = (value) => this.titleCasePipe.transform(value);
  private levelGetNameFormatter = (value) => value.name;

  //Residence status is a string enum - you can display the string value of the enum like this.
  private residenceStatusFormatter = (value) => ResidenceStatus[value];

  private getIeltsScoreType = (value) => {
    return this.getIeltsScore(value);
  }
  private getOverallTasksStatus = (value) => {
    return this.getTasksStatus(value);
  }

  private allDisplayableFields = [
    new CandidateFieldInfo("First Name", "user.firstName",
      null, this.isCandidateNameViewable),
    new CandidateFieldInfo("Driving License", "drivingLicense",
      null, null),
    new CandidateFieldInfo("Gender", "gender",
      this.titleCaseFormatter, null),
    new CandidateFieldInfo("Last Name", "user.lastName",
      null, this.isCandidateNameViewable),
    new CandidateFieldInfo("Location", "country.name",
      null, this.isCountryViewable),
    new CandidateFieldInfo("State", "state",
      null, this.isCountryViewable),
    new CandidateFieldInfo("City", "city",
      null, this.isCountryViewable),
    new CandidateFieldInfo("Married?", "maritalStatus",
      null, null),
    new CandidateFieldInfo("Nationality", "nationality.name",
      null, this.isCountryViewable),
    new CandidateFieldInfo("Phone", "phone",
      null, null),
    new CandidateFieldInfo("Status", "status",
      this.titleCaseFormatter, null),
    new CandidateFieldInfo("UNHCR Status", "unhcrStatus",
      null, null),
    new CandidateFieldInfo("Updated", "updatedDate",
      this.dateFormatter, null),
    new CandidateFieldInfo("DOB", "dob",
      this.dateFormatter, null),
    new CandidateFieldInfo("Highest Level of Edu", "maxEducationLevel.level",
      this.levelGetNameFormatter, null),
    new CandidateFieldInfo("IELTS Score", "ieltsScore",
      this.getIeltsScoreType, null),
    new CandidateFieldInfo("Legal status", "residenceStatus",
      this.residenceStatusFormatter, null),
    new CandidateFieldInfo("Dependants", "numberDependants",
      null, null),
    // REMOVED THIS COLUMN FOR NOW, AS IT ISN'T SORTABLE. INSTEAD ADDED TASKS MONITOR.
    // new CandidateFieldInfo("Tasks Status", "taskAssignments",
    //   this.getOverallTasksStatus, null),
  ];

  private allDisplayableFieldsMap = new Map<string, CandidateFieldInfo>();

  private defaultDisplayedFieldPathsLong: string [] = [
    "user.firstName",
    "user.lastName",
    "status",
    "updatedDate",
    "nationality.name",
    "country.name",
    "gender"
  ];

  private defaultDisplayedFieldPathsShort: string [] = [
    "user.firstName",
    "user.lastName",
    "nationality.name",
    "country.name",
    "gender"
  ];

  constructor(
    private authService: AuthService,
    private datePipe: DatePipe,
    private titleCasePipe: TitleCasePipe
  ) {

    for (const field of this.allDisplayableFields) {
      this.allDisplayableFieldsMap.set(field.fieldPath, field);
    }
  }

  get defaultDisplayableFieldsLong(): CandidateFieldInfo[] {
    return this.getFieldsFromPaths(this.defaultDisplayedFieldPathsLong);
  }

  get defaultDisplayableFieldsShort(): CandidateFieldInfo[] {
    return this.getFieldsFromPaths(this.defaultDisplayedFieldPathsShort);
  }

  get displayableFieldsMap(): Map<string, CandidateFieldInfo> {
    const fields = new Map<string, CandidateFieldInfo>();
    //Filter based on field selectors
    for (const field of this.allDisplayableFields) {
      if (field.fieldSelector == null || field.fieldSelector()) {
        fields.set(field.fieldPath, field);
      }
    }
    return fields;
  }

  getCandidateSourceFields(
    source: CandidateSource, longFormat: boolean): CandidateFieldInfo[] {
    let fields: CandidateFieldInfo[] = [];
    if (source) {
      let fieldPaths;
      if (longFormat) {
        fieldPaths = source.displayedFieldsLong;
        //Default if empty fieldPaths
        if (!fieldPaths || fieldPaths.length === 0) {
          fieldPaths = this.defaultDisplayedFieldPathsLong;
        }
      } else {
        fieldPaths = source.displayedFieldsShort;
        //Default if empty fieldPaths
        if (!fieldPaths || fieldPaths.length === 0) {
          fieldPaths = this.defaultDisplayedFieldPathsShort;
        }
      }
      fields = this.getFieldsFromPaths(fieldPaths);
    }
    return fields;
  }


  getFieldsFromPaths(fieldPaths: string []): CandidateFieldInfo[] {
    const fields: CandidateFieldInfo[] = [];

    for (const fieldPath of fieldPaths) {
      const field = this.allDisplayableFieldsMap.get(fieldPath);
      if (field == null) {
        console.error("CandidateFieldService: Could not find field for " + fieldPath)
      } else {
        //Ignore fields with a selector which returns false
        if (field.fieldSelector == null || field.fieldSelector()) {
          fields.push(field);
        }
      }
    }
    return fields;
  }

  isCandidateNameViewable(): boolean {
    const loggedInUser =
      this.authService ? this.authService.getLoggedInUser() : null;
    const role = loggedInUser ? loggedInUser.role : null;
    return Role[role] !== Role.semilimited && Role[role] !== Role.limited;
  }

  isCountryViewable(): boolean {
    const loggedInUser =
      this.authService ? this.authService.getLoggedInUser() : null;
    const role = loggedInUser ? loggedInUser.role : null;
    return Role[role] !== Role.limited;
  }

  isAnAdmin(): boolean {
    const loggedInUser =
      this.authService ? this.authService.getLoggedInUser() : null;
    const role = loggedInUser ? loggedInUser.role : null;
    return Role[role] !== Role.semilimited && Role[role] !== Role.limited;
  }

  isDefault(fieldPaths: string[], longFormat: boolean) {
    if (fieldPaths == null) {
      return false;
    }

    const defaultPaths = longFormat
      ? this.defaultDisplayedFieldPathsLong
      : this.defaultDisplayedFieldPathsShort;

    //Compare fieldPaths and defaultPaths, returning true if they are the same
    let same = fieldPaths.length === defaultPaths.length;
    if (same) {
      for (let i = 0; i < fieldPaths.length; i++) {
        if (fieldPaths[i] !== defaultPaths[i]) {
          same = false;
          break;
        }
      }
    }

    return same;
  }

  getIeltsScore(candidate: Candidate): string {
    let score: string = null;
    if (candidate?.ieltsScore) {
      const type = checkIeltsScoreType(candidate)
      if (type === "IELTSGen") {
        score = candidate?.ieltsScore + ' (Gen)';
      } else if (type === "IELTSAca") {
        score = candidate?.ieltsScore + ' (Aca)';
      } else {
        score = candidate?.ieltsScore + ' (Est)'
      }
    }
    return score;
  }

  getTasksStatus(values: TaskAssignment[]): string {
    let status: string;
    // Only run through active tasks.
    values = values.filter(ta => ta.status === Status.active);
    if (checkForOverdue(values)) {
      status = 'Overdue'
    } else if (checkForAbandoned(values)) {
      status = 'Abandoned'
    } else if (checkForCompleted(values) && !checkForOngoing(values)) {
      status = 'Completed'
    } else {
      status = null;
    }
    return status;
  }

}
