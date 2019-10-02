import {User} from "./user";

export interface Candidate {
  id: number;
  candidateNumber: string;
  user: User;
}
