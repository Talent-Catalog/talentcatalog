import {User} from './user';

export enum AttachmentType {
  googlefile = 'googlefile',
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
  migrated: boolean; // A flag determining is the file was migrated from the previous system
  cv: boolean;
}

export class CandidateAttachmentRequest {
  candidateId: number;
  type: AttachmentType;
  name: string;
  location: string;
  cv: boolean;
  fileType?: string; //Not needed for links
  folder?: string; //Only used by S3. Not needed for links or Google
}

export interface SearchCandidateAttachmentsRequest {
  candidateId: number;
  cvOnly: boolean;
}
