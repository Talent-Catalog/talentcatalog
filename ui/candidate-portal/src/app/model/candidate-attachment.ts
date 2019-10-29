import {Candidate} from "./candidate";
import {User} from "./user";

export enum AttachmentType {
  file = 'file',
  link = 'link'
}

export interface CandidateAttachment {
  name: string;
  location: string;
  fileType: string;
  type: AttachmentType;
  migrated: boolean;
  adminOnly: boolean;
  candidate?: Candidate;
  createdBy: User;
  createdDate: string;
}
