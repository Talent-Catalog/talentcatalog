import {User} from "./user";
import {Country} from "./country";
import {Nationality} from "./nationality";
import {CandidateReviewStatusItem} from "./candidate-review-status-item";
import {EducationMajor} from "./education-major";
import {EducationLevel} from "./education-level";
import {SurveyType} from "./survey-type";
import {Router} from "@angular/router";
import {Location} from "@angular/common";
import {getExternalHref} from "../util/url";

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

export interface CandidateCitizenship {
  citizenNationalityId?: number;
  citizenHasPassport?: HasPassport;
  citizenNotes?: string;
}

export interface CandidateIntakeData {
  citizenNationalityId?: number;
  citizenHasPassport?: HasPassport;
  citizenNotes?: string;

  returnedHome?: YesNoUnsure;
  returnedHomeNotes?: string;
  returnedHomeReason?: string;

  visaIssues?: VisaIssue[];
  visaIssuesNotes?: string;

  availImmediate?: YesNoUnsure;
  availImmediateReason?: AvailImmediateReason;
  availImmediateNotes?: string;

  familyMove?: YesNo;
  familyMoveNotes?: string;
  familyHealth?: YesNo;
  familyHealthNotes?: string;
}

/*
  Enumerations. These should match equivalent enumerations on the server (Java)
  side.

  The string associated each enumerated value can be anything. It is what is
  displayed to the user in corresponding drop down selections. It is only
  used on the front end and can be changed any time without needing to change
  anything on the server.
*/

export enum HasPassport {
  NoResponse = "",
  ValidPassport = "Has valid passport",
  InvalidPassport = "Has invalid passport",
  NoPassport = "No passport"
}

export enum ReturnedHome {
  NoResponse = "No response",
  Returned = "Returned home",
  NotReturned = "Has not returned home"
}

export enum VisaIssue {
  Health = "Health issues",
  Military = "Military service",
  GovtWork = "Work for foreign government",
  Criminal = "Criminal record",
  VisaRejections = "Visa rejections",
  Other = "Other"
}

export enum AvailImmediateReason {
  Family = "Family",
  Health = "Health",
  CurrentWork = "Current Work",
  Studies = "Studies",
  Other = "Other"
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
