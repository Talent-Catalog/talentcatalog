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
  candidateMessage: string;
  maxEducationLevel: EducationLevel;
  folderlink: string;
  sflink: string;
  videolink: string;
  unRegistered: string;
  unRegistrationNumber: string;
  surveyType: SurveyType;
  surveyComment: string;
  selected: boolean;
  createdDate: number;
  updatedDate: number;
  contextNote: string;
}

export interface CandidateIntakeData {
  asylumYear?: string;
  availImmediate?: YesNoUnsure;
  availImmediateReason?: AvailImmediateReason;
  availImmediateNotes?: string;

  candidateCitizenships?: CandidateCitizenship[];

  candidateDestinations?: CandidateDestination[];

  destLimit?: YesNo;
  destLimitNotes?: string;

  familyMove?: YesNo;
  familyMoveNotes?: string;
  familyHealth?: YesNo;
  familyHealthNotes?: string;
  homeLocation?: string;

  returnedHome?: YesNoUnsure;
  returnedHomeNotes?: string;
  returnedHomeReason?: string;

  visaIssues?: VisaIssue[];
  visaIssuesNotes?: string;

  intRecruitReasons?: IntRecruitReason[];
  intRecruitRural?: YesNoUnsure;

  returnHomeSafe?: YesNoUnsure;
  workPermit?: WorkPermitValidity;
  workPermitDesired?: YesNoUnsure;
  workLegally?: YesNo;
  workDesired?: WorkDesiredField;
  hostEntryYear?: string;
  unhcrStatus?: UnhcrStatus;
  unhcrOldStatus?: UnhcrStatus;
  unhcrNumber?: string;
  unhcrFile?: number;
  unhcrNotes?: string;
  unhcrPermission?: YesNo;
  unrwaRegistered?: YesNoUnsure;
  unrwaWasRegistered?: YesNoUnsure;
  unrwaNumber?: string;
  unrwaNotes?: string;
}

export interface CandidateCitizenship {
  id?: number;
  nationality?: {id};
  hasPassport?: HasPassport;
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
  Parents = "Mother/Father",
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
  Assessed = "Assessed by UNHCR as a mandate refugee",
  RegisteredAsylum = "Registered with UNHCR as asylum seeker",
  RegisteredStateless = "Registered with UNHCR as stateless",
  NotRegistered = "Not registered",
  Unsure = "Unsure"
}

export enum WorkPermitValidity {
  NoResponse = "",
  YesNotDesired = "Yes - a permit to work but not in my desired field",
  YesDesired = "Yes - a permit to work in my desired field",
  No = "No - I do not have a work permit",
}

export enum WorkDesiredField {
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

export function getCandidateNavigation(candidate: Candidate): any[] {
  return ['candidate', candidate.candidateNumber];
}

export function getCandidateExternalHref(
  router: Router, location: Location, candidate: Candidate): string {
  return getExternalHref(router, location, getCandidateNavigation(candidate));
}
