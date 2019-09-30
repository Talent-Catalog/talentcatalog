import {User} from "./user";

export interface Candidate {
  id: number;
  candidateNumber: string;
  displayName: string;
  user: User;
}
