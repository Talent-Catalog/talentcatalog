import {Occupation} from './occupation';

export interface CandidateOccupation {
  id: number;
  yearsExperience: number;
  occupation?: Occupation;
  migrationOccupation?: string;
  // Request object variables
  occupationId?: number;
}
