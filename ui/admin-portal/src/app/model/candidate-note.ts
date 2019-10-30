import {User} from "./user";

export interface CandidateNote {
  id: number;
  title: string;
  comment: string;
  noteType: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User;
  updatedDate: number;
}
