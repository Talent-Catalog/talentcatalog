import {Profession} from "./profession";
import {Education} from "./education";
import {Country} from "./country";
import {WorkExperience} from "./work-experience";
import {Certification} from "./certification";
import {CandidateLanguage} from "./candidate-language";

export interface Candidate {
  id: number;
  candidateNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  whatsapp: string;
  gender: string;
  dob: string;
  professions: Profession[];
  country: Country;
  city: string;
  yearOfArrival: number;
  nationality: string;
  registeredWithUN: boolean;
  workExperiences: WorkExperience[];
  certifications: Certification[];
  candidateLanguages: CandidateLanguage[];
  registrationId: string;
  educationLevel: string;
  educations: Education[];
  additionalInfo: string;
}
