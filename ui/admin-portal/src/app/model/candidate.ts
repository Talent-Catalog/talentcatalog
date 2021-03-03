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

import {User} from './user';
import {Country} from './country';
import {Nationality} from './nationality';
import {CandidateReviewStatusItem} from './candidate-review-status-item';
import {EducationMajor} from './education-major';
import {EducationLevel} from './education-level';
import {SurveyType} from './survey-type';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {getExternalHref} from '../util/url';
import {Occupation} from './occupation';
import {LanguageLevel} from './language-level';

export interface Candidate {
  id: number;
  candidateNumber: string;
  status: string;
  gender: string;
  dob: Date;
  address1: string;
  city: string;
  country: Country;
  yearOfArrival: number;
  nationality: Nationality;
  phone: string;
  whatsapp: string;
  user: User;
  candidateReviewStatusItems: CandidateReviewStatusItem[];
  migrationEducationMajor: EducationMajor;
  additionalInfo: string;
  linkedInProfile: string;
  candidateMessage: string;
  maxEducationLevel: EducationLevel;
  folderlink: string;
  sflink: string;
  videolink: string;
  surveyType: SurveyType;
  surveyComment: string;
  selected: boolean;
  createdDate: number;
  updatedDate: number;
  contextNote: string;
  maritalStatus: MaritalStatus;
  drivingLicense: DrivingLicenseStatus;
  unhcrStatus: UnhcrStatus;
}

export interface CandidateIntakeData {
  asylumYear?: string;
  availImmediate?: YesNoUnsure;
  availImmediateJobOps?: string;
  availImmediateReason?: AvailImmediateReason;
  availImmediateNotes?: string;

  candidateCitizenships?: CandidateCitizenship[];
  candidateDependants?: CandidateDependant[];

  candidateDestinations?: CandidateDestination[];

  candidateVisaChecks?: CandidateVisaCheck[];

  candidateExams?: CandidateExam[];

  canDrive?: YesNo;

  children?: YesNo;
  childrenAge?: YesNo;

  birthCountry?: Country;

  dependants?: number;
  dependantsNotes?: string;

  conflict?: YesNo;
  conflictNotes?: string;

  crimeConvict?: YesNoUnsure;
  crimeConvictNotes?: string;

  destLimit?: YesNo;
  destLimitNotes?: string;

  destJob?: YesNo;
  destJobNotes?: string;

  drivingLicense?: DrivingLicenseStatus;
  drivingLicenseExp?: string;
  drivingLicenseCountry?: Country;

  familyMove?: YesNo;
  familyMoveNotes?: string;

  familyHealth?: YesNo;
  familyHealthNotes?: string;

  homeLocation?: string;

  hostChallenges?: string;
  hostBorn?: YesNo;
  hostEntryYear?: number;
  hostEntryLegally?: YesNo;
  hostEntryLegallyNotes?: string;
  intRecruitReasons?: IntRecruitReason[];
  intRecruitOther?: string;
  intRecruitRural?: YesNoUnsure;
  intRecruitRuralNotes?: string;
  langAssessment?: string;
  langAssessmentScore?: IeltsScore;
  leftHomeReasons?: LeftHomeReason[];
  leftHomeOther?: string;
  militaryService?: YesNo;
  militaryWanted?: YesNo;
  militaryNotes?: string;
  militaryStart?: string;
  militaryEnd?: string;
  maritalStatus?: MaritalStatus;
  partnerRegistered?: YesNoUnsure;
  partnerCandidate?: Candidate;
  partnerEduLevel?: EducationLevel;
  partnerEduLevelNotes?: string;
  partnerOccupation?: Occupation;
  partnerOccupationNotes?: string;
  partnerEnglish?: YesNo;
  partnerEnglishLevel?: LanguageLevel;
  partnerIelts?: IeltsStatus;
  partnerIeltsScore?: IeltsScore;
  partnerIeltsYr?: number;
  partnerCitizenship?: Nationality;

  returnedHome?: YesNoUnsure;
  returnedHomeNotes?: string;
  returnedHomeReason?: string;
  returnedHomeReasonNo?: string;

  residenceStatus?: ResidenceStatus;
  residenceStatusNotes?: string;

  returnHomeSafe?: YesNoUnsure;

  returnHomeFuture?: YesNoUnsure;
  returnHomeWhen?: string;

  resettleThird?: YesNo;
  resettleThirdStatus?: string;

  workAbroad?: YesNo;
  workAbroadCountryIds?: number[];
  workAbroadYrs?: number;
  workAbroadNotes?: string;
  workPermit?: WorkPermitValidity;
  workPermitDesired?: YesNoUnsure;
  workDesired?: YesNoUnemployed;
  workDesiredNotes?: string;
  unhcrRegistered?: YesNoUnsure;
  unhcrStatus?: UnhcrStatus;
  unhcrOldStatus?: UnhcrStatus;
  unhcrNumber?: string;
  unhcrFile?: number;
  unhcrNotes?: string;
  unhcrPermission?: YesNo;
  unrwaRegistered?: YesNoUnsure;
  unrwaStatus?: UnrwaStatus;
  unrwaNumber?: string;
  unrwaNotes?: string;
  visaReject?: YesNoUnsure;
  visaRejectNotes?: string;
  visaIssues?: VisaIssue[];
  visaIssuesNotes?: string;
}

export interface CandidateCitizenship {
  id?: number;
  nationality?: {id};
  hasPassport?: HasPassport;
  passportExp?: string;
  notes?: string;
}

export interface CandidateDependant {
  id?: number;
  relation?: DependantRelations;
  dob?: string;
  name?: string;
  registered?: string;
  healthConcern?: string;
  notes?: string;
}

export interface CandidateExam {
  id?: number;
  exam?: Exam;
  otherExam?: string;
  score?: string;
  year?: number;
}

export interface CandidateDestination {
  id?: number;
  country?: Country;
  interest?: YesNoUnsure;
  family?: FamilyRelations;
  location?: string;
  notes?: string;
}

export interface CandidateVisaCheck {
  id?: number;
  country?: Country;
  eligibility?: VisaEligibility;
  assessmentNotes?: string;
  checkedBy?: User;
  checkedDate?: string;
  updatedBy?: User;
  updatedDate?: number;
  protection?: YesNo;
  protectionGrounds?: string;
  tbbEligibilityAssessment?: TBBEligibilityAssessment;
}

/*
  Enumerations. These should match equivalent enumerations on the server (Java)
  side.

  The string associated each enumerated value can be anything. It is what is
  displayed to the user in corresponding drop down selections. It is only
  used on the front end and can be changed any time without needing to change
  anything on the server.
*/

export enum AvailImmediateReason {
  Family = "Family",
  Health = "Health",
  CurrentWork = "Current Work",
  Studies = "Studies",
  Other = "Other"
}

export enum FamilyRelations {
  NoResponse = "",
  NoRelation = "No relatives",
  Child = "Daughter/Son",
  Parent = "Mother/Father",
  Sibling = "Sister/Brother",
  AuntUncle = "Aunt/Uncle",
  Grandparent = "Grandmother/Grandfather",
  Cousin = "First Cousin",
  Other = "Other"
}

export enum DependantRelations {
  NoResponse = "",
  Partner = "Spouse/Partner",
  Child = "Daughter/Son",
  Parent = "Mother/Father",
  Sibling = "Sister/Brother",
  AuntUncle = "Aunt/Uncle",
  Grandparent = "Grandmother/Grandfather",
  Cousin = "First Cousin",
  Other = "Other"
}

export enum HasPassport {
  NoResponse = "",
  ValidPassport = "Has valid passport",
  InvalidPassport = "Has invalid passport",
  NoPassport = "No passport"
}

export enum TBBEligibilityAssessment {
  NoResponse = "",
  Proceed = "Proceed",
  Discuss = "Discuss further",
  DontProceed = "Don't proceed",
}

export enum VisaEligibility {
  NoResponse = "",
  No = "No",
  DiscussFurther = "Discuss further",
  SeekAdvice = "Seek advice",
  Yes = "Yes",
  YesBut = "Yes (but manage expectations about visa pathway)"
}

export enum VisaIssue {
  Health = "Health issues",
  Military = "Military service",
  GovtWork = "Work for foreign government",
  Criminal = "Criminal record",
  VisaRejections = "Visa rejections",
  Other = "Other"
}

export enum IntRecruitReason {
  CantReturnHome = "I cannot return to my home country",
  Citizenship = "I am seeking new citizenship",
  Experience = "I am looking to get experience",
  Children = "I would like a better future for my children",
  Other = "Other"
}

export enum UnhcrStatus {
  NoResponse = "",
  MandateRefugee = "Assessed by UNHCR as a mandate refugee",
  RegisteredAsylum = "Registered with UNHCR as asylum seeker",
  RegisteredStateless = "Registered with UNHCR as stateless",
  NotRegistered = "Not registered",
  Unsure = "Unsure",
  NA = "Not applicable"
}

export enum WorkPermitValidity {
  NoResponse = "",
  YesNotDesired = "Yes - a permit to work but not in my desired field",
  YesDesired = "Yes - a permit to work in my desired field",
  No = "No - I do not have a work permit",
}

export enum YesNoUnemployed {
  NoResponse = "",
  Yes = "Yes",
  No = "No",
  Unemployed = "I am unemployed",
}

export enum YesNo {
  NoResponse = "",
  Yes = "Yes",
  No = "No",
}

export enum YesNoUnsure {
  NoResponse = "",
  Yes = "Yes",
  No = "No",
  Unsure = "Unsure"
}

export enum IeltsStatus {
  NoResponse = "",
  YesGeneral = "Yes - Ielts General",
  YesAcademic = "Yes - Ielts Academic",
  No = "No",
  Unsure = "Unsure"
}

export enum YesNoUnsureLearn {
  NoResponse = "",
  Yes = "Yes",
  No = "No",
  Unsure = "Unsure - I need to learn more."
}

export enum UnrwaStatus {
  NoResponse = "",
  Registered = "Registered",
  WasRegistered = "No longer registered, but was registered previously.",
  NeverRegistered = "Never been registered",
  Unsure = "Unsure",
  NA = "Not applicable"
}

export enum Exam {
  OET = "OET",
  IELTSGen = "IELTS General",
  IELTSAca = "IELTS Academic",
  TOEFL = "TOEFL",
  Other = "Other"
}

export enum ResidenceStatus {
  NoResponse = "",
  LegalRes = "Legal Residency",
  IllegalRes = "Illegal Residency"
}

export enum LeftHomeReason {
  Safety = "Safety/Protection",
  Job = "Job Opportunities",
  Other = "Other"
}

export enum MaritalStatus {
  NoResponse = "",
  Married = "Married",
  Engaged = "Engaged",
  Defacto = "Defacto",
  Single = "Single",
  Divorced = "Divorced"
}

export enum IeltsScore {
  NoResponse = "",
  Unsure = "Unsure",
  Zero = "0",
  Half = "0.5",
  One = "1",
  OneHalf = "1.5",
  Two = "2",
  TwoHalf = "2.5",
  Three = "3",
  ThreeHalf = "3.5",
  Four = "4",
  FourHalf = "4.5",
  Five = "5",
  FiveHalf = "5.5",
  Six = "6",
  SixHalf = "6.5",
  Seven = "7",
  SevenHalf = "7.5",
  Eight = "8",
  EightHalf = "8.5",
  Nine = "9",
}

export enum DrivingLicenseStatus {
  NoResponse = "",
  Valid = "Valid",
  Expired = "Expired",
  None = "None"
}

export enum Registrations {
  NoResponse = "",
  UNHCR = "UNHCR only",
  UNRWA = "UNRWA only",
  UNHCRUNRWA = "UNHCR & UNRWA",
  Neither = "Neither",
  NA = "Not Applicable",
}

export function getCandidateNavigation(candidate: Candidate): any[] {
  return ['candidate', candidate.candidateNumber];
}

export function getCandidateExternalHref(
  router: Router, location: Location, candidate: Candidate): string {
  return getExternalHref(router, location, getCandidateNavigation(candidate));
}
