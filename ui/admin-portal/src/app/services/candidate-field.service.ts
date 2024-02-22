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
import {AuthorizationService} from "./authorization.service";
import {CandidateSource, Status} from "../model/base";
import {Candidate, checkIeltsScoreType, ResidenceStatus, UnhcrStatus} from "../model/candidate";
import {
  checkForAbandoned,
  checkForCompleted,
  checkForOngoing,
  checkForOverdue,
  TaskAssignment
} from "../model/task-assignment";

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

  private intakeTypeFormatter = (value) => {
    return this.getIntakesCompleted(value);
  }
  private getIeltsScoreType = (value) => {
    return this.getIeltsScore(value);
  }
  private getOverallTasksStatus = (value) => {
    return this.getTasksStatus(value);
  }

  private unhcrStatusTooltip = (value) => UnhcrStatus[value.unhcrStatus];

  private intakeDatesTooltip = (value) => {
    return this.getIntakeDates(value);
  }

  private allDisplayableFields = [];

  private allDisplayableFieldsMap = new Map<string, CandidateFieldInfo>();

  /*
    These are the default candidate fields displayed if nothing special is stored in the database
   */
  private defaultDisplayedFieldPathsLong: string [] = [
    "user.firstName",
    "user.lastName",
    "status",
    "intakeStatus",
    "updatedDate",
    "nationality.name",
    "country.name",
    "user.partner.abbreviation",
    "gender"
  ];

  private defaultDisplayedFieldPathsShort: string [] = [
    "user.firstName",
    "user.lastName",
    "nationality.name",
    "country.name",
    "user.partner.abbreviation",
    "gender"
  ];

  constructor(
    private authService: AuthorizationService,
    private datePipe: DatePipe,
    private titleCasePipe: TitleCasePipe
  ) {

  this.allDisplayableFields = [
      new CandidateFieldInfo("First Name", "user.firstName", null,
        null, this.isCandidateNameViewable, true),
      new CandidateFieldInfo("Driving License", "drivingLicense", null,
        null, null, true),
      new CandidateFieldInfo("Gender", "gender", null,
        this.titleCaseFormatter, null, true),
      new CandidateFieldInfo("Last Name", "user.lastName", null,
        null, this.isCandidateNameViewable, true),
      new CandidateFieldInfo("Location", "country.name", null,
        null, this.isCountryViewable, true),
      new CandidateFieldInfo("State", "state", null,
        null, this.isCountryViewable, true),
      new CandidateFieldInfo("City", "city", null,
        null, this.isCountryViewable, true),
      new CandidateFieldInfo("Married?", "maritalStatus", null,
        null, null, true),
      new CandidateFieldInfo("Nationality", "nationality.name", null,
        null, this.isCountryViewable, true),
      new CandidateFieldInfo("Partner", "user.partner.abbreviation", null,
        null, null, true),
      new CandidateFieldInfo("Phone", "phone", null,
        null, null, true),
      new CandidateFieldInfo("Referrer", "regoReferrerParam", null,
        null, null, true),
      new CandidateFieldInfo("Status", "status", null,
        this.titleCaseFormatter, null, true),
      new CandidateFieldInfo("UNHCR Status", "unhcrStatus", this.unhcrStatusTooltip,
        null, null, true),
      new CandidateFieldInfo("Updated", "updatedDate", null,
        this.dateFormatter, null, true),
      new CandidateFieldInfo("DOB", "dob", null,
        this.dateFormatter, null, true),
      new CandidateFieldInfo("Highest Level of Edu", "maxEducationLevel.level", null,
        this.levelGetNameFormatter, null, true),
      new CandidateFieldInfo("IELTS Score", "ieltsScore", null,
        this.getIeltsScoreType, null, true),
      new CandidateFieldInfo("Legal status", "residenceStatus", null,
        this.residenceStatusFormatter, null, true),
      new CandidateFieldInfo("Dependants", "numberDependants", null,
        null, null, true),
      new CandidateFieldInfo("NextStep", "candidateOpportunities.nextStep", null,
      null, null, true),
      new CandidateFieldInfo("Intake Status", "intakeStatus", this.intakeDatesTooltip,
      this.intakeTypeFormatter, null, false)
      // REMOVED THIS COLUMN FOR NOW, AS IT ISN'T SORTABLE. INSTEAD ADDED TASKS MONITOR.
      // new CandidateFieldInfo("Tasks Status", "taskAssignments", null,
      //   this.getOverallTasksStatus, null),
    ];

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

  /**
   * Normally would be declared as:
   *    isCandidateNameViewable(): boolean {
   *
   * This odd syntax is necessary because of Javascript bug/oddity. The bug arises when we refer
   * to this method above in the allDisplayableFields CandidateFieldInfo definitions.
   * When you do that Javascript loses track of what "this" refers to - and inside the method ends
   * up returning undefined for this.authService.
   * <p/>
   * This solution to that problem is suggested here:
   * https://github.com/Microsoft/TypeScript/wiki/'this'-in-TypeScript#use-instance-functions
   */
  isCandidateNameViewable = (): boolean => {
    return this.authService.canViewCandidateName()
  }

  /**
   * Regarding funny syntax, see above comments for isCandidateNameViewable
   */
  isCountryViewable = (): boolean => {
    return this.authService.canViewCandidateCountry()
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
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

  getIntakesCompleted(candidate: Candidate): string {
    let mini = candidate?.miniIntakeCompletedDate != null ? 'Mini' : null;
    let full = candidate?.fullIntakeCompletedDate != null ? 'Full' : null;
    if (mini && full) {
      return mini + ', ' + full;
    } else if (mini) {
      return mini;
    } else if (full) {
      return full;
    }
  }

  getIntakeDates(candidate: Candidate): string {
    let mini = candidate?.miniIntakeCompletedDate != null ? 'Mini' : null;
    let full = candidate?.fullIntakeCompletedDate != null ? 'Full' : null;
    if (mini && full) {
      return 'Mini: ' + this.dateFormatter(candidate?.miniIntakeCompletedDate)
        + ', Full: ' + this.dateFormatter(candidate?.fullIntakeCompletedDate);
    } else if (mini) {
      return 'Mini: ' + this.dateFormatter(candidate?.miniIntakeCompletedDate)
    } else if (full) {
      return 'Full: ' + this.dateFormatter(candidate?.fullIntakeCompletedDate);
    }
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
