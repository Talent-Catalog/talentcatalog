import {User} from "./user";
import {Candidate} from "./candidate";

export interface CandidateShortlistItem {
  id: number;
  shortlistStatus: string;
  comment: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User
  updatedDate: number;
}
