import {Occupation} from "./occupation";

export interface CandidateOccupation {
  id: number;
  occupation: Occupation;
  yearsExperience: number;
}

export interface CandidateOccupationRequest {
  id: number;
  occupationId: number;
  yearsExperience: number;
}
