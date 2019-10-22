import {Occupation} from "./occupation";

export interface CandidateOccupation {
  id: number;
  occupation: Occupation;
  yearsExperience: number;
  migrationOccupation: string;
  verified: boolean;
}
