import {Country} from "./country";
import {CandidateOccupation} from "./candidate-occupation";

export interface CandidateJobExperience {
  id: number;
  companyName: string;
  role: string;
  startDate: string;
  endDate: string;
  fullTime: string;
  paid: string;
  description: string;
  country?: Country;
  candidateOccupation?: CandidateOccupation;
  // Request object variables
  countryId?: number;
  candidateOccupationId?: number;
}
