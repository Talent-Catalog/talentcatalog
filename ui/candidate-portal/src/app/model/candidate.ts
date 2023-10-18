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

import {CandidateOccupation} from "./candidate-occupation";
import {CandidateEducation} from "./candidate-education";
import {Country} from "./country";
import {CandidateJobExperience} from "./candidate-job-experience";
import {CandidateCertification} from "./candidate-certification";
import {CandidateLanguage} from "./candidate-language";
import {EducationLevel} from "./education-level";
import {User} from "./user";
import {SurveyType} from "./survey-type";
import {toDateOnly} from "../util/date";
import {CandidateOpportunity} from "./candidate-opportunity";

export interface Candidate {
  id: number;
  user: User;
  candidateNumber: string;
  phone: string;
  whatsapp: string;
  gender: string;
  dob: string;
  candidateOccupations: CandidateOccupation[];
  country: Country;
  state: string;
  city: string;
  yearOfArrival: number;
  nationality: Country;
  externalId: string;
  externalIdSource: string;
  unhcrRegistered: YesNoUnsure;
  unhcrNumber: string;
  unhcrConsent: YesNo;
  candidateJobExperiences: CandidateJobExperience[];
  candidateCertifications: CandidateCertification[];
  candidateLanguages: CandidateLanguage[];
  registrationId: string;
  maxEducationLevel: EducationLevel;
  candidateEducations: CandidateEducation[];
  additionalInfo: string;
  linkedInLink: string;
  candidateMessage: string;
  surveyType: SurveyType;
  surveyComment: string;
  status: CandidateStatus;
  taskAssignments: TaskAssignment[];
  candidateOpportunities: CandidateOpportunity[];

}

export interface TaskAssignment {
  id: number;
  abandonedDate: Date;
  candidateNotes: string;
  completedDate: Date;
  dueDate: Date;
  status: Status;
  task: Task
  answer: string;
}

/**
 * Defines standard sort for Task Assignments.
 * <p/>
 * Basically ongoing task assignments, come before completed (or abandoned) ones.
 * Then ongoing task assignments are sorted by due date (ascending order), and then alphabetically
 * for a given due date.
 * Completed (or abandoned) task assignments are just sorted alphabetically by task name.
 * @param a Task assignment
 * @param b Another task assignment
 * @return 1, 0 or -1 for "a" greater, equal or less than "b"
 */
export function taskAssignmentSort(a: TaskAssignment, b: TaskAssignment) {
  function isOngoingTaskAssignment(ta: TaskAssignment) {
    return ta.completedDate == null && ta.abandonedDate == null;
  }

  if (!isOngoingTaskAssignment(a) && !isOngoingTaskAssignment(b)) {
    //Neither task assignment is ongoing, just sort by task name
    return a.task.displayName.localeCompare(b.task.displayName);
  } else if (isOngoingTaskAssignment(a) && isOngoingTaskAssignment(b)) {
    //Both task assignments are ongoing, sort by due date, then task name
    //Strip any time off dueDate
    const aDateOnly = toDateOnly(a.dueDate);
    const bDateOnly = toDateOnly(b.dueDate);
    if (aDateOnly.getTime() === bDateOnly.getTime()) {
      //Dates are the same, sort by task name
      return a.task.displayName.localeCompare(b.task.displayName)
    } else {
      //Dates are different, sort by date - most recent first
      return aDateOnly > bDateOnly ? 1 : -1;
    }
  } else {
    //One is ongoing the other is not. The ongoing one comes first
    return isOngoingTaskAssignment(a) ? 1 : -1;
  }
}

class AllowedQuestionTaskAnswer {
  name: string;
  displayName: string;
}

export interface Task {
  id: number;
  name: string;
  daysToComplete: number,
  description: string;
  displayName: string;
  helpLink: string;
  optional: boolean;
  taskType: TaskType;
  uploadType?: UploadType;
  uploadSubfolderName?: string;
  uploadableFileTypes?: string;
  candidateAnswerField?: string;
  allowedAnswers?: AllowedQuestionTaskAnswer[];
}

export enum TaskType {
  Question = "Question",
  Simple = "Simple",
  Upload = "Upload",
  YesNoQuestion = "YesNoQuestion"
}

export enum UploadType {
  collaborationAgreement,
  conductEmployer,
  conductEmployerTrans,
  conductMinistry,
  conductMinistryTrans,
  cos,
  cv,
  degree,
  degreeTranscript,
  degreeTranscriptTrans,
  englishExam,
  idCard,
  infoReleaseForm,
  licencing,
  licencingTrans,
  offer,
  otherId,
  otherIdTrans,
  passport,
  policeCheck,
  policeCheckTrans,
  proofAddress,
  proofAddressTrans,
  references,
  residenceAttest,
  residenceAttestTrans,
  studiedInEnglish,
  other,
  unhcrUnrwaRegCard,
  vaccination,
  vaccinationTrans
}

export enum CandidateStatus {
  active = "active",
  autonomousEmployment = "autonomous employment (inactive)",
  deleted = "deleted (inactive)",
  draft = "draft (inactive)",
  employed = "employed (inactive)",
  incomplete = "incomplete",
  ineligible = "ineligible (inactive)",
  pending = "pending",
  unreachable = "unreachable",
  withdrawn = "withdrawn (inactive)"
}

export enum Status {
  active = "active",
  inactive = "inactive",
  deleted = "deleted"
}

export enum YesNo {
  Yes = "Yes",
  No = "No",
}

export enum YesNoUnsure {
  Yes = "Yes",
  No = "No",
  Unsure = "Unsure"
}

export class BaseCandidateContactRequest {
  id: number;
  email: string;
  phone: string;
  whatsapp: string;
}

export class RegisterCandidateRequest extends BaseCandidateContactRequest {
  username: string;
  password: string;
  passwordConfirmation: string;
  reCaptchaV3Token: string;
  partnerAbbreviation: string;
  referrerParam?: string;
  utmCampaign?: string;
  utmContent?: string;
  utmMedium?: string;
  utmSource?: string;
  utmTerm?: string;
  consentPartner?: string;
  consentAllPartners?: string;
}

export class LoginRequest {
  username: string;
  password: string;
  reCaptchaV3Token: string;
}

export class SendResetPasswordEmailRequest {
  email: string;
  reCaptchaV3Token: string;
}

/**
 * Defines the start of a linkedIn profile URL.
 * Defined here in case the link structure changes so only needs changing in one place.
 */
export const linkedInUrl: string = 'https://www.linkedin.com/in/';

