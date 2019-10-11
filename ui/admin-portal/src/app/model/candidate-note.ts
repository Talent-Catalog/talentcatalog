import {User} from "./user";

export interface CandidateNote {
  id: number;
  subject: string;
  comment: string;
  user: User;
  createdDate: number;
}
