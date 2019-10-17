import {Occupation} from "./occupation";

export interface CandidateOccupation {
  id: number;
  yearsExperience: number;
  occupation?: Occupation;
  // Request object variables
  occupationId?: number;
}
