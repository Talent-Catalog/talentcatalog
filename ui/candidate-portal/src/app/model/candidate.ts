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

import {CandidateOccupation} from "./candidate-occupation";
import {CandidateEducation} from "./candidate-education";
import {Country} from "./country";
import {CandidateJobExperience} from "./candidate-job-experience";
import {CandidateCertification} from "./candidate-certification";
import {CandidateLanguage} from "./candidate-language";
import {EducationLevel} from "./education-level";
import {Location} from '@angular/common';
import {ShortUser, User} from "./user";
import {SurveyType} from "./survey-type";
import {CandidateOpportunity} from "./candidate-opportunity";
import {HasId} from "./base";
import {EducationMajor} from "./education-major";
import {CandidateAttachment} from "./candidate-attachment";
import {Occupation} from "./occupation";
import {LanguageLevel} from "./language-level";
import {OpportunityProgressParams} from "./opportunity";
import {Router} from "@angular/router";
import {getExternalHref} from "../util/url";
import {CandidateReviewStatusItem} from "./candidate-review-status-item";
import {TaskAssignment} from "./task-assignment";
import {Job} from "./job";

export interface ShortCandidate {
  id: number;
  candidateNumber: string;
  user: ShortUser;
}

export interface Candidate extends HasId {
  id: number;
  candidateNumber: string;
  publicId: string;
  status: string;
  allNotifications: boolean;
  gender: string;
  dob: Date;
  address1: string;
  city: string;
  state: string;
  country: Country;
  yearOfArrival: number;
  nationality: Country;
  candidateCitizenships: CandidateCitizenship[];
  phone: string;
  whatsapp: string;
  externalId: string;
  externalIdSource: string;
  partnerRef: string;
  unhcrRegistered: YesNoUnsure;
  unhcrNumber: string;
  unhcrConsent: YesNo;
  unrwaRegistered: YesNoUnsure;
  unrwaNumber: string;
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
  regoPartnerParam: string;
  regoReferrerParam: string;
  regoUtmCampaign: string;
  regoUtmContent: string;
  regoUtmMedium: string;
  regoUtmSource: string;
  regoUtmTerm: string;
  shareableCv: CandidateAttachment;
  shareableDoc: CandidateAttachment;
  listShareableCv: CandidateAttachment;
  listShareableDoc: CandidateAttachment;
  shareableNotes: string;
  surveyType: SurveyType;
  surveyComment: string;
  selected: boolean;
  createdDate: number;
  updatedDate: number;
  contextNote: string;
  maritalStatus: MaritalStatus;
  drivingLicense: DrivingLicenseStatus;
  unhcrStatus: UnhcrStatus;
  ieltsScore: string;
  numberDependants: number;
  englishAssessmentScoreIelts: string;
  frenchAssessmentScoreNclc: number;
  candidateExams: CandidateExam[];
  candidateAttachments?: CandidateAttachment[];
  taskAssignments?: TaskAssignment[];
  candidateOpportunities: CandidateOpportunity[];
  candidateProperties?: CandidateProperty[];
  mediaWillingness?: string;
  // relocated address fields
  relocatedAddress: string;
  relocatedCity: string;
  relocatedState: string;
  relocatedCountry: Country;

  //These are only used in the candidate portal on the browser code
  candidateCertifications?: CandidateCertification[];
  candidateEducations?: CandidateEducation[];
  candidateJobExperiences?: CandidateJobExperience[];
  candidateLanguages?: CandidateLanguage[];
  candidateOccupations?: CandidateOccupation[];
  candidateDestinations?: CandidateDestination[];

}

export interface CandidateProperty {
  name: string;
  value: string;
}

export interface CandidateIntakeData {
  asylumYear?: string;
  availDate?: string;
  availImmediate?: YesNo;
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

  covidVaccinated?: YesNo;
  covidVaccinatedStatus?: VaccinationStatus;
  covidVaccinatedDate?: string;
  covidVaccineName?: string;
  covidVaccineNotes?: string;

  crimeConvict?: YesNoUnsure;
  crimeConvictNotes?: string;

  arrestImprison?: YesNoUnsure;
  arrestImprisonNotes?: string;

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
  englishAssessment?: string;
  englishAssessmentScoreIelts?: string;
  leftHomeReasons?: LeftHomeReason[];
  leftHomeNotes?: string;
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
  partnerCitizenship?: number[];

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
  unhcrConsent?: YesNo;
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
  nationality?: Country;
  hasPassport?: HasPassport;
  passportExp?: string;
  notes?: string;
}

export interface CandidateDependant {
  id?: number;
  relation?: DependantRelations;
  relationOther?: string;
  dob?: string;
  gender?: Gender;
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
  interest?: YesNoUnsureLearn;
  family?: FamilyRelations;
  location?: string;
  notes?: string;
}

export interface CandidateVisa {
  id?: number;
  country?: Country;
  protection?: YesNo;
  protectionGrounds?: string;
  tbbEligibilityAssessment?: TBBEligibilityAssessment;
  englishThreshold?: YesNo;
  englishThresholdNotes?: string;
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
  visaEligibilityAssessment?: YesNo;
  pathwayAssessment?: YesNoUnsure;
  pathwayAssessmentNotes?: string;
  candidateVisaJobChecks?: CandidateVisaJobCheck[];

}

export interface CandidateVisaJobCheck {
  jobOpp?: Job;
  id?: number;
  occupation?: Occupation;
  occupationNotes?: string;
  qualification?: YesNo;
  qualificationNotes?: string;
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
  relevantWorkExp?: string;
  ageRequirement?: YesNo;
  preferredPathways?: string;
  ineligiblePathways?: string;
  eligiblePathways?: string;
  occupationCategory?: string;
  occupationSubCategory?: string;
  englishThreshold?: YesNo;
  englishThresholdNotes?: string;
  relocatingDependantIds?: number[];
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

export interface CandidateOpportunityParams extends OpportunityProgressParams {
  closingComments?: string;
  closingCommentsForCandidate?: string;
  employerFeedback?: string;
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
  contactConsentRegistration?: string;
  contactConsentPartners?: string;
}

export interface UpdateCandidateOppsRequest {
  candidateIds: number[];
  sfJobOppId: string;
  candidateOppParams?: CandidateOpportunityParams;
}

export interface UpdateCandidateListOppsRequest {
  savedListId: number;
  candidateOppParams?: CandidateOpportunityParams;
}

export interface UpdateCandidateShareableNotesRequest {
  shareableNotes?: string;
}

export interface UpdateCandidateShareableDocsRequest {
  shareableCvAttachmentId: number,
  shareableDocAttachmentId: number,
  savedListId?: number
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

export interface UpdateCandidateNotificationPreferenceRequest {
  allNotifications: boolean;
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
  Child = "Child",
  Parent = "Parent",
  Sibling = "Sibling",
  AuntUncle = "Aunt/Uncle",
  Grandparent = "Grandparent",
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
  NoResponse= "No response received from candidate",
  MandateRefugee = "Assessed by UNHCR as a mandate refugee (RSD)",
  RegisteredAsylum = "Registered with UNHCR as asylum seeker",
  RegisteredStateless = "Registered with UNHCR as stateless",
  RegisteredStatusUnknown = "Registered with UNHCR but status unknown",
  NotRegistered = "Not registered with UNHCR",
  Unsure = "Candidate was unsure",
  NA = "Not applicable"
}

export enum NotRegisteredStatus {
  WasRegistered = "No longer registered, but was registered previously.",
  NeverRegistered = "Never been registered",
  Registering = "Attempted to register and pending",
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
  OET = "OET Overall",
  OETRead = "OET Reading",
  OETList = "OET Listening",
  OETLang = "OET Language Knowledge",
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

export enum EducationType {
  Associate = "Associates",
  Vocational = "Vocational",
  Bachelor = "Bachelors",
  Masters = "Masters",
  Doctoral = "Doctoral",
}

export enum Gender {
  female = "Female",
  male = "Male",
  other = "Other",
}

export enum VaccinationStatus {
  Full = "Fully Vaccinated",
  Partial = "Partially Vaccinated",
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

export function checkIeltsScoreType(candidate: Candidate): string {
  if (candidate.candidateExams.length > 0) {
    const type: CandidateExam = candidate.candidateExams?.find(e => e?.score === candidate.ieltsScore.toString());
    if (type != null && type.exam != null) {
      return type.exam;
    } else {
      return null;
    }
  }
}

export function getIeltsScoreTypeString(candidate: Candidate): string {
  const type = checkIeltsScoreType(candidate);
  if (type === "IELTSGen") {
    return 'General';
  } else if (type === "IELTSAca") {
    return 'Academic';
  } else {
    return 'Estimated'
  }
}

/**
 * Returns the immigration pathway link for each destination country. Used in the visa intake.
 * We are hard coding these links as the websites should stay the same.
 * Note: These are currently for demo purposes only.
 * @param countryId The country we want the relevant links for.
 */
export function getDestinationPathwayInfoLink(countryId: number): string {
  switch (countryId) {
    // Canada
    case 6216:
      return 'https://www.canada.ca/en/immigration-refugees-citizenship/services/work-canada/hire-permanent-foreign.html';

    // Australia
    case 6191:
      return 'test link: www.australia.com';
  }
}

/**
 * Returns the occupation category help link for each destination country. Used in the visa intake.
 * We are hard coding these links as the websites should stay the same.
 * Note: These are currently for demo purposes only.
 * @param countryId The country we want the relevant links for.
 */
export function getDestinationOccupationCatLink(countryId: number): string {
  switch (countryId) {
    // Canada
    case 6216:
      return 'https://noc.esdc.gc.ca/Home/Welcome';

    //Australia
    case 6191:
      return 'this is a test link for australia to use';
  }
}

/**
 * Returns the occupation sub category help link for each destination country. Used in the visa intake.
 * We are hard coding these links as the websites should stay the same.
 * Note: These are currently for demo purposes only.
 * @param countryId The country we want the relevant links for.
 */
export function getDestinationOccupationSubcatLink(countryId: number): string {
  switch (countryId) {
    // Canada
    case 6216:
      return 'https://noc.esdc.gc.ca/Structure/Hierarchy';

    //Australia
    case 6191:
      return 'this is a test link for australia to use';
  }
}

export class SendResetPasswordEmailRequest {
  email: string;
  reCaptchaV3Token: string;
}

