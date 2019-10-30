import {User} from "./user";

export enum AttachmentType {
  file = 'file',
  link = 'link'
}

export interface CandidateAttachment {
  id: number;
  type: AttachmentType;
  name: string;
  location: string;
  createdBy: User;
  createdDate: number;
  updatedBy: User
  updatedDate: number;
}
