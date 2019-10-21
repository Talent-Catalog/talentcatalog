import {User} from "./user";

export interface CandidateAttachment {
  id: number;
  type: string;
  name: string;
  location: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User
  updatedDate: number;
}
