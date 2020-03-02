import {User} from "./user";
import {Country} from "./country";
import {Nationality} from "./nationality";
import {CandidateShortlistItem} from "./candidate-shortlist-item";
import {EducationMajor} from "./education-major";
import {EducationLevel} from "./education-level";

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
}
