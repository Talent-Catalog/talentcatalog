import {User} from "./user";

export interface CandidateShortlistItem {
  id: number;
  shortlistStatus: string;
  comment: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User
  updatedDate: number;
}
