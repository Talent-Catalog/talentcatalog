import {Candidate} from "./candidate";

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
}
