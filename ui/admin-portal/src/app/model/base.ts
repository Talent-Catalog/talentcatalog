import {User} from './user';
import {AuthService} from '../services/auth.service';

export enum CandidateSourceType {
  SavedList,
  SavedSearch
}

export enum ReviewedStatus {
  pending,
  verified,
  rejected
}

export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe,
  watched
}

/**
 * Defines what TBB Salesforce url should look like.
 * <p/>
 * Basically it starts with TBB's Salesforce url. It also contains a
 * Salesforce record ID which is a string 15 or more of "word" characters
 * preceeded by a record type delimited by "/".
 * eg /Opportunity/...id...
 */
export const salesforceUrlPattern: string =
  'https://talentbeyondboundaries.lightning.force.com/' +
  '.*/[\\w]+/[\\w]{15,}[^\\w]?.*';

export const salesforceUrlRegExp: RegExp = new RegExp(salesforceUrlPattern);

export const defaultReviewStatusFilter: string[] = [
  ReviewedStatus[ReviewedStatus.pending],
  ReviewedStatus[ReviewedStatus.verified]
];

export interface Auditable {
  id: number;
  createdBy?: User;
  createdDate?: number;
  updatedBy?: User
  updatedDate?: number;
}

export interface CandidateSource extends Auditable {
  name: string;
  fixed: boolean;
  sfJoblink?: string;
  users?: User[];
  watcherUserIds?: number[];
}

export interface Opportunity {
  name: string;
}

export class PagedSearchRequest {
  pageSize?: number;
  pageNumber?: number;
  sortFields?: string[];
  sortDirection?: string;
}

export class SearchCandidateSourcesRequest extends PagedSearchRequest {
  keyword?: string;
  fixed?: boolean;
  global?: boolean;
  owned?: boolean;
  shared?: boolean;
  watched?: boolean;
}

export class UpdateCandidateContextNoteRequest {
  candidateId: number;
  contextNote: string;
}

export class LoginRequest {
  username: string;
  password: string;
  reCaptchaV3Token: string;
}

export function isMine(source: CandidateSource, auth: AuthService) {
  let mine: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (source && source.createdBy && me) {
    mine = source.createdBy.id === me.id;
  }
  return mine;
}

export function isSharedWithMe(source: CandidateSource, auth: AuthService) {
  let sharedWithMe: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (source && me) {
    sharedWithMe = source.users.find(u => u.id === me.id ) !== undefined;
  }
  return sharedWithMe;
}

export function canEditSource(source: CandidateSource, auth: AuthService) {
  //We can change the source if we own the savedSearch or if it not fixed.
  let changeable: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (source) {
    // If source is NOT FIXED anyone can edit it
    if (!source.fixed) {
      changeable = true;
      // If source is FIXED but it belongs to me, I can change it. If it doesn't belong to me I can't.
    } else {
      //Only can edit source if we own that source.
      changeable = isMine(source, auth);
    }
  }
  return changeable;
}

