import {User} from "./user";
import {Country} from "./country";
import {Nationality} from "./nationality";
import {CandidateShortlistItem} from "./candidate-shortlist-item";
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
  candidateShortlistItems: CandidateShortlistItem[];
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
}

export function getCandidateNavigation(candidate: Candidate): any[] {
  return ['candidates', candidate.id];
}

export function getCandidateExternalHref(
  router: Router, location: Location, candidate: Candidate): string {
  return getExternalHref(router, location, getCandidateNavigation(candidate));
}
