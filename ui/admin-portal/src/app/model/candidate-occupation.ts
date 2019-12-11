import {Occupation} from "./occupation";
import {User} from "./user";

export interface CandidateOccupation {
  id: number;
  occupation: Occupation;
  yearsExperience: number;
  migrationOccupation: string;
  verified: boolean;
  createdBy: User;
  createdDate: number;
  updatedBy: User;
  updatedDate: number;
}
