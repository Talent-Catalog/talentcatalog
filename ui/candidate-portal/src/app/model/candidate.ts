import {Profession} from "./profession";
import {Education} from "./education";

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
  country: string;
  city: string;
  yearOfArrival: string;
  nationality: string;
  registeredWithUN: string;
  registrationId: string;
  educationLevel: string;
  educations: Education[];
  additionalInfo: string;
}
