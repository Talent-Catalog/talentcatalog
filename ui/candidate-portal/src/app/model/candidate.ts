import {Profession} from "./profession";
import {Education} from "./education";
import {Country} from "./country";

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
  registrationId: string;
  educationLevel: string;
  educations: Education[];
  additionalInfo: string;
}
