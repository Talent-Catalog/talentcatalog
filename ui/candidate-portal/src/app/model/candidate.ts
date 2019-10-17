import {CandidateOccupation} from "./candidate-occupation";
import {CandidateEducation} from "./candidate-education";
import {Country} from "./country";
import {Nationality} from "./nationality";
import {CandidateJobExperience} from "./candidate-job-experience";
import {CandidateCertification} from "./candidate-certification";
import {CandidateLanguage} from "./candidate-language";
import {EducationLevel} from "./education-level";
import {User} from "./user";

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
  city: string;
  yearOfArrival: number;
  nationality: Nationality;
  registeredWithUN: boolean;
  candidateJobExperiences: CandidateJobExperience[];
  candidateCertifications: CandidateCertification[];
  candidateLanguages: CandidateLanguage[];
  registrationId: string;
  maxEducationLevel: EducationLevel;
  candidateEducations: CandidateEducation[];
  additionalInfo: string;
}
