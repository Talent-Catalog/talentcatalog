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
import {CandidateOpportunity} from "../model/candidate-opportunity";
import {SavedList} from "../model/saved-list";

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

  private intakeDateFormatter = (value) => {
    return this.getLatestIntakeDates(value);
  }

  private ieltsScoreFormatter = (value) => {
    return this.getIeltsScore(value);
  }
  private englishAssessmentScoreDetFormatter = (value) => {
    return this.getEnglishAssessmentScoreDet(value);
  }

  private nclcScoreFormatter = (value) => {
    return this.getNclcScore(value);
  }

  private getOverallTasksStatus = (value) => {
    return this.getTasksStatus(value);
  }

  private unhcrStatusTooltip = (value) => UnhcrStatus[value.unhcrStatus];

  private intakeDatesTooltip = (value) => {
    return this.getIntakeDates(value);
  }

  private nextStepFormatter = (value: any, value2: any) => {
    return this.getNextStep(value, value2);
  }

  private addedByFormatter = (value: any, value2: any) => {
    return this.getAddedBy(value, value2);
  }

  private addedByTooltip = (value: any, value2: any) => {
    return this.getAddedByPartner(value, value2);
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
    "latestIntake",
    "ieltsScore",
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
        null, this.isCandidateContactViewable, true),
      new CandidateFieldInfo("Whatsapp", "whatsapp", null,
        null, this.isCandidateContactViewable, true),
      new CandidateFieldInfo("Email", "user.email", null,
        null, this.isCandidateContactViewable, true),
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
        this.ieltsScoreFormatter, null, true),
    new CandidateFieldInfo("DET Score", "englishAssessmentScoreDet", null,
      this.englishAssessmentScoreDetFormatter, null, true),
      new CandidateFieldInfo("NCLC Score", "frenchAssessmentScoreNclc", null,
        this.nclcScoreFormatter, null, true),
      new CandidateFieldInfo("Legal status", "residenceStatus", null,
        this.residenceStatusFormatter, null, true),
      new CandidateFieldInfo("Dependants", "numberDependants", null,
        null, null, false),
      new CandidateFieldInfo("Next Step", "nextStep", null,
      this.nextStepFormatter, this.isSourceSubmissionList, false),
      new CandidateFieldInfo("Added By", "addedBy", this.addedByTooltip,
      this.addedByFormatter, this.isSourceSubmissionList, false),
      new CandidateFieldInfo("Latest Intake", "latestIntake", this.intakeDatesTooltip,
      this.intakeTypeFormatter, null, false),
      new CandidateFieldInfo("Latest Intake Date", "latestIntakeDate", null,
      this.intakeDateFormatter, null, false),
      new CandidateFieldInfo("Survey Type", "surveyType.name", null,
        null, null, true),
      new CandidateFieldInfo("Survey Comment", "surveyComment", null,
        null, null, true)
      // REMOVED THIS COLUMN FOR NOW, AS IT ISN'T SORTABLE. INSTEAD ADDED TASKS MONITOR.
      // new CandidateFieldInfo("Tasks Status", "taskAssignments", null,
      //   this.getOverallTasksStatus, null),
    ];

    for (const field of this.allDisplayableFields) {
      this.allDisplayableFieldsMap.set(field.fieldPath, field);
    }
  }

  getDefaultDisplayableFieldsLong(source: CandidateSource): CandidateFieldInfo[] {
    return this.getFieldsFromPaths(this.defaultDisplayedFieldPathsLong, source);
  }

  getDefaultDisplayableFieldsShort(source: CandidateSource): CandidateFieldInfo[] {
    return this.getFieldsFromPaths(this.defaultDisplayedFieldPathsShort, source);
  }

  getDisplayableFieldsMap(source: CandidateSource): Map<string, CandidateFieldInfo> {
    const fields = new Map<string, CandidateFieldInfo>();
    //Filter based on field selectors
    for (const field of this.allDisplayableFields) {
      if (field.fieldSelector == null || field.fieldSelector(source)) {
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
      fields = this.getFieldsFromPaths(fieldPaths, source);
    }
    return fields;
  }


  getFieldsFromPaths(fieldPaths: string [], source:CandidateSource): CandidateFieldInfo[] {
    const fields: CandidateFieldInfo[] = [];

    for (const fieldPath of fieldPaths) {
      const field = this.allDisplayableFieldsMap.get(fieldPath);
      if (field == null) {
        console.error("CandidateFieldService: Could not find field for " + fieldPath)
      } else {
        //Ignore fields with a selector which returns false
        if (field.fieldSelector == null || field.fieldSelector(source)) {
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

  /**
   * Regarding funny syntax, see above comments for isCandidateNameViewable
   */
  isCandidateContactViewable = (): boolean => {
    return this.authService.canViewCandidateContact()
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }

  isSourceSubmissionList = (source: CandidateSource): boolean => {
    return (source as SavedList).registeredJob === true;
  };


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
    if (candidate?.ieltsScore != null) {
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
  getEnglishAssessmentScoreDet(candidate: Candidate): string {
    let score: string = null;
    if (candidate?.englishAssessmentScoreDet != null) {
      score = candidate.englishAssessmentScoreDet + ' (Det)';
    }
    return score;
  }
  getNclcScore(candidate: Candidate): string {
    let score: string = null;
    if (candidate?.frenchAssessmentScoreNclc != null) {
      // todo - add check for type of score (e.g. TEF, TCF, etc.) when available - see #768
      score = candidate?.frenchAssessmentScoreNclc + ' (Est)'
    }
    return score;
  }

  getIntakesCompleted(candidate: Candidate): string {
    let full = candidate?.fullIntakeCompletedDate != null ? 'Full' : null;
    let mini = candidate?.miniIntakeCompletedDate != null ? 'Mini' : null;
    let intakeStatus = null;
    if (full) {
      intakeStatus = mini ? full : full + ' *no mini';
    } else if (mini) {
      intakeStatus = mini;
    }
    return intakeStatus;
  }

  getLatestIntakeDates(candidate: Candidate): string {
    let full = candidate?.fullIntakeCompletedDate != null ?
      this.dateFormatter(candidate.fullIntakeCompletedDate) : null;
    let mini = candidate?.miniIntakeCompletedDate != null ?
      this.dateFormatter(candidate.miniIntakeCompletedDate) : null;
    let intakeDate = null;
    if (full) {
      intakeDate = full;
    } else if (mini) {
      intakeDate = mini;
    }
    return intakeDate;
  }

  getIntakeDates(candidate: Candidate): string {
    let mini = candidate?.miniIntakeCompletedDate != null ? 'Mini' : null;
    let full = candidate?.fullIntakeCompletedDate != null ? 'Full' : null;
    if (mini && full) {
      return 'Mini intake: ' + this.dateFormatter(candidate?.miniIntakeCompletedDate)
        + ' Full intake: ' + this.dateFormatter(candidate?.fullIntakeCompletedDate);
    } else if (mini) {
      return 'Mini intake: ' + this.dateFormatter(candidate?.miniIntakeCompletedDate)
    } else if (full) {
      return 'Full intake: ' + this.dateFormatter(candidate?.fullIntakeCompletedDate);
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

  public getNextStep(candidate: Candidate, source: CandidateSource): string {
    let candidateOppForThisList: CandidateOpportunity =
      this.findRelevantCandidateOpp(candidate, source);

    return candidateOppForThisList === null ? '?' : candidateOppForThisList.nextStep;
  }

  public getAddedBy(candidate: Candidate, source: CandidateSource): string {
    const candidateOppForThisList: CandidateOpportunity =
      this.findRelevantCandidateOpp(candidate, source);

    return candidateOppForThisList === null ? '?' : candidateOppForThisList.createdBy.firstName + " " +
      candidateOppForThisList.createdBy.lastName;
  }

  /**
   * Populates the tooltip for values in the Added By column — saves table space by providing the
   * partner name in this form.
   * @param candidate the given candidate for this table row
   * @param source the source that the displayed candidates belong to, e.g., submission list, saved
   * search.
   */
  public getAddedByPartner(candidate: Candidate, source: CandidateSource): string {
    const candidateOppForThisList: CandidateOpportunity =
      this.findRelevantCandidateOpp(candidate, source);

    return candidateOppForThisList === null ? '?' : candidateOppForThisList.createdBy.partner.name;
  }

  // When displaying a submission list, will find the relevant candidate opp for given candidate.
  private findRelevantCandidateOpp(candidate: Candidate, source: CandidateSource): CandidateOpportunity {
    const opp = candidate.candidateOpportunities.find(
      opp => opp.jobOpp.submissionList.id == source.id
    );
    if (opp == null) {
      console.warn('No matching opp found for this candidate and source');
      return null;
    } else {
      return opp;
    }
  }

}
