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
import {CandidateReviewStatusItem} from './candidate-review-status-item';
import {EducationMajor} from './education-major';
import {EducationLevel} from './education-level';
import {SurveyType} from './survey-type';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {getExternalHref} from '../util/url';
import {Occupation} from './occupation';
import {LanguageLevel} from './language-level';
import {HasId} from "./base";
import {ContextNote} from "./context-note";

export interface Candidate extends HasId {
  id: number;
  candidateNumber: string;
  status: string;
  gender: string;
  dob: Date;
  address1: string;
  city: string;
  country: Country;
  yearOfArrival: number;
  nationality: Country;
  phone: string;
  whatsapp: string;
  user: User;
  candidateReviewStatusItems: CandidateReviewStatusItem[];
  migrationEducationMajor: EducationMajor;
  additionalInfo: string;
  linkedInLink: string;
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
  contextNote: ContextNote;
  maritalStatus: MaritalStatus;
  drivingLicense: DrivingLicenseStatus;
  unhcrStatus: UnhcrStatus;
  ieltsScore: string;
  langAssessmentScore: string;
  candidateExams: CandidateExam[];
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

  candidateVisaChecks?: CandidateVisa[];

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

  healthIssues?: string;
  healthIssuesNotes?: string;

  homeLocation?: string;

  hostChallenges?: string;
  hostBorn?: YesNo;
  hostEntryYear?: number;
  hostEntryYearNotes?: string;
  hostEntryLegally?: YesNo;
  hostEntryLegallyNotes?: string;
  intRecruitReasons?: IntRecruitReason[];
  intRecruitOther?: string;
  intRecruitRural?: YesNoUnsure;
  intRecruitRuralNotes?: string;
  langAssessment?: string;
  langAssessmentScore?: string;
  leftHomeReasons?: LeftHomeReason[];
  leftHomeOther?: string;
  militaryService?: YesNo;
  militaryWanted?: YesNo;
  militaryNotes?: string;
  militaryStart?: string;
  militaryEnd?: string;
  maritalStatus?: MaritalStatus;
  maritalStatusNotes?: string;
  partnerRegistered?: YesNoUnsure;
  partnerCandidate?: Candidate;
  partnerEduLevel?: EducationLevel;
  partnerEduLevelNotes?: string;
  partnerOccupation?: Occupation;
  partnerOccupationNotes?: string;
  partnerEnglish?: YesNo;
  partnerEnglishLevel?: LanguageLevel;
  partnerIelts?: IeltsStatus;
  partnerIeltsScore?: string;
  partnerIeltsYr?: number;
  partnerCitizenship?: Country;

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
  workAbroadNotes?: string;
  workPermit?: WorkPermitValidity;
  workPermitDesired?: YesNoUnsure;
  workPermitDesiredNotes?: string;
  workDesired?: YesNoUnemployedOther;
  workDesiredNotes?: string;
  unhcrRegistered?: YesNoUnsure;
  unhcrStatus?: UnhcrStatus;
  unhcrNumber?: string;
  unhcrFile?: number;
  unhcrNotRegStatus?: NotRegisteredStatus;
  unhcrNotes?: string;
  unrwaRegistered?: YesNoUnsure;
  unrwaNumber?: string;
  unrwaFile?: number;
  unrwaNotRegStatus?: NotRegisteredStatus;
  unrwaNotes?: string;
  visaReject?: YesNoUnsure;
  visaRejectNotes?: string;
  visaIssues?: YesNoUnsure;
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
  relationOther?: string;
  dob?: string;
  name?: string;
  registered?: Registrations;
  registeredNumber?: string;
  registeredNotes?: string;
  healthConcern?: YesNo;
  healthNotes?: string;
}

export interface CandidateExam {
  id?: number;
  exam?: Exam;
  otherExam?: string;
  score?: string;
  year?: number;
  notes?: string;
}

export interface CandidateDestination {
  id?: number;
  country?: Country;
  interest?: YesNoUnsure;
  family?: FamilyRelations;
  location?: string;
  notes?: string;
}

export interface CandidateVisa {
  id?: number;
  country?: Country;
  eligibility?: VisaEligibility;
  assessmentNotes?: string;
  protection?: YesNo;
  protectionGrounds?: string;
  tbbEligibilityAssessment?: TBBEligibilityAssessment;
  healthAssessment: YesNo;
  healthAssessmentNotes: string;
  characterAssessment: YesNo;
  characterAssessmentNotes: string;
  securityRisk: YesNo;
  securityRiskNotes: string;
  validTravelDocs: YesNo;
  validTravelDocsNotes: string;
  overallRisk: string;
  overallRiskNotes: string;
  intProtection?: string;
  createdBy?: User;
  createdDate?: number;
  updatedBy?: User;
  updatedDate?: number;
  candidateVisaJobChecks?: CandidateVisaJobCheck[];

}

export interface CandidateVisaJobCheck {
  id?: number;
  name?: string;
  sfJobLink?: string;
  occupation?: Occupation;
  qualification?: Qualification;
  salaryTsmit?: YesNo;
  regional?: YesNo;
  interest?: YesNo;
  interestNotes?: String;
  familyAus?: YesNo;
  eligible_494?: YesNo;
  eligible_494_Notes?: String;
  eligible_186?: YesNo;
  eligible_186_Notes?: String;
  eligibleOther?: YesNo;
  eligibleOtherNotes?: String;
  putForward?: VisaEligibility;
  tbbEligibility?: TBBEligibilityAssessment;
  notes?: String;
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

export enum CandidateOpportunityStage {
  prospect = "Prospect",
  miniIntake = "Mini intake",
  fullIntake = "Full intake",
  visaEligibility = "Visa eligibility",
  cvPreparation = "CV preparation",
  cvReview = "CV review",
  oneWayPreparation = "1 way preparation",
  oneWayReview = "1 way review",
  testPreparation = "Test preparation",
  testing = "Testing",
  twoWayPreparation = "2 way preparation",
  twoWayReview = "2 way review",
  offer = "Offer",
  acceptance = "Acceptance",
  provincialVisaPreparation = "Provincial visa preparation",
  provincialVisaProcessing = "Provincial visa processing",
  visaPreparation = "Visa preparation",
  visaProcessing = "Visa processing",
  relocating = "Relocating",
  relocated = "Relocated",
  settled = "Settled",
  durableSolution = "Durable solution",
  noJobOffer = "No job offer",
  noVisa = "No visa",
  notFitForRole = "Not fit for role",
  notEligibleForVisa = "Not eligible for visa",
  noInterview = "No interview",
  candidateRejectsOffer = "Candidate rejects offer",
  candidateWithdraws = "Candidate withdraws"

}

export enum CandidateStatus {
  active = "active",
  deleted = "deleted",
  draft = "draft",
  employed = "employed",
  incomplete = "incomplete",
  ineligible = "ineligible",
  pending = "pending",
  unreachable = "unreachable"
}

export interface SalesforceOppParams {
  stageName?: string;
  nextStep?: string;
}

export interface UpdateCandidateOppsRequest {
  candidateIds: number[];
  sfJobLink: string;
  salesforceOppParams?: SalesforceOppParams;
}

export interface UpdateCandidateListOppsRequest {
  savedListId: number;
  salesforceOppParams?: SalesforceOppParams;
}

export interface UpdateCandidateStatusInfo {
  status: CandidateStatus;
  comment?: string;
  candidateMessage?: string;
}

export interface UpdateCandidateStatusRequest {
  candidateIds: number[];
  info: UpdateCandidateStatusInfo;
}

export enum FamilyRelations {
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
  ValidPassport = "Has valid passport",
  InvalidPassport = "Has invalid passport",
  NoPassport = "No passport"
}

export enum TBBEligibilityAssessment {
  Proceed = "Proceed",
  Discuss = "Discuss further",
  DontProceed = "Don't proceed",
}

export enum VisaEligibility {
  Yes = "Yes",
  YesBut = "Yes (but manage expectations about visa pathway)",
  DiscussFurther = "Discuss further",
  SeekAdvice = "Seek advice",
  No = "No"
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
  MandateRefugee = "Assessed by UNHCR as a mandate refugee",
  RegisteredAsylum = "Registered with UNHCR as asylum seeker",
  RegisteredStateless = "Registered with UNHCR as stateless",
  NotRegistered = "Not registered",
  Unsure = "Unsure",
  NA = "Not applicable"
}

export enum NotRegisteredStatus {
  WasRegistered = "No longer registered, but was registered previously.",
  NeverRegistered = "Never been registered",
  Unsure = "Unsure",
  NA = "Not applicable"
}

export enum WorkPermitValidity {
  YesNotDesired = "Yes - a permit to work but not in my desired field",
  YesDesired = "Yes - a permit to work in my desired field",
  No = "No - I do not have a work permit",
}

export enum YesNoUnemployedOther {
  Yes = "Yes",
  No = "No",
  Unemployed = "I am unemployed",
  Other = "Other"
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

export enum IeltsStatus {
  YesGeneral = "Yes - Ielts General",
  YesAcademic = "Yes - Ielts Academic",
  No = "No",
  Unsure = "Unsure"
}

export enum YesNoUnsureLearn {
  Yes = "Yes",
  No = "No",
  Unsure = "Unsure - I need to learn more."
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
  IllegalRes = "Illegal Residency",
  Other = "Other"
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
  Divorced = "Divorced",
  Separated = "Separated",
  Widower = "Widow/er"
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
  Valid = "Valid",
  Expired = "Expired",
  None = "None"
}

export enum Registrations {
  UNHCR = "UNHCR only",
  UNRWA = "UNRWA only",
  Neither = "Neither",
  NA = "Not Applicable",
}

export enum TravelDocumentStatus {
  Valid = "Valid",
  Expired = "Expired",
  None = "None"
}

export enum RiskLevel {
  Low = "Low Risk",
  Medium = "Medium Risk",
  High = "High Risk"
}

export enum TbbEligibility {
  Proceed = "Ready to proceed",
  Discuss = "Needs discussion",
  NotProceed = "Do not proceed"
}

export enum OtherVisas {
  TempSkilled = "482 temporary skilled (medium stream)",
  SpecialHum = "202 (special humanitarian)",
  OtherHum = "Other humanitarian (200, 201, 203)",
  DirectEnt = "186 direct entry permanent stream",
  PointsIndep = "189/190 points tested independant stream",
}

export enum Qualification {
  HighSchool = "High School",
  Associates = "Associates",
  Bachelors = "Bachelors",
  Masters = "Masters",
  PHD = "PHD",
  Other = "Other"
}

export enum Gender {
  female = "Female",
  male = "Male",
  other = "Other",
}

export function getCandidateNavigation(candidate: Candidate): any[] {
  return ['candidate', candidate.candidateNumber];
}

export function getCandidateExternalHref(
  router: Router, location: Location, candidate: Candidate): string {
  return getExternalHref(router, location, getCandidateNavigation(candidate));
}

export function hasIeltsExam(candidate: Candidate): boolean {
  if (candidate.candidateExams.length > 0) {
    return candidate?.candidateExams?.find(e => e?.exam?.toString() === "IELTSGen") != null;
  } else {
    return false;
  }
}
