import {User} from "./user";
import {AuthService} from "../services/auth.service";

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
  sharedWithMe
}

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
  users?: User[];
  watcherUserIds?: number[];
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
  owned?: boolean;
  shared?: boolean;
}

export function isMine(source: CandidateSource, auth: AuthService) {
  let mine: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (source && me) {
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

