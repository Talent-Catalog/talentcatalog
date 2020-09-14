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
}

export interface CandidateIntakeData {
  returnedHome?: boolean;
  returnedHomeNotes?: string;
  returnedHomeReason?: string;
}

export function getCandidateNavigation(candidate: Candidate): any[] {
  return ['candidate', candidate.candidateNumber];
}

export function getCandidateExternalHref(
  router: Router, location: Location, candidate: Candidate): string {
  return getExternalHref(router, location, getCandidateNavigation(candidate));
}
