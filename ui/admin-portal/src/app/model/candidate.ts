import {User} from "./user";
import {Country} from "./country";

export interface Candidate {
  id: number;
  candidateNumber: string;
  dob: Date;
  city: string;
  country: Country;
  phone: string;
  whatsapp: string;
  user: User;
}
