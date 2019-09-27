import {CandidateOccupation} from "./candidate-occupation";
import {CandidateEducation} from "./candidate-education";
import {Country} from "./country";
import {Nationality} from "./nationality";
import {CandidateJobExperience} from "./candidate-job-experience";
import {CandidateCertification} from "./candidate-certification";
import {CandidateLanguage} from "./candidate-language";

export interface Candidate {
  id: number;
  username: string;
  candidateNumber: string;
  firstName: string;
  lastName: string;
  email: string;
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
  jobExperiences: CandidateJobExperience[];
  certifications: CandidateCertification[];
  candidateLanguages: CandidateLanguage[];
  registrationId: string;
  educationLevel: string;
  educations: CandidateEducation[];
  additionalInfo: string;
}
