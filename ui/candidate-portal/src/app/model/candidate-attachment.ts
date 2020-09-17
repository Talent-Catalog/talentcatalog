import {Candidate} from './candidate';
import {User} from './user';

export enum AttachmentType {
  googlefile = 'googlefile',
  file = 'file',
  link = 'link'
}

export interface CandidateAttachment {
  id?: number;
  name: string;
  location: string;
  fileType: string;
  type: AttachmentType;
  migrated: boolean;
  cv: boolean;
  candidate?: Candidate;
  createdBy: User;
  createdDate: string;
}
