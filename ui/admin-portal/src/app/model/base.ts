import {User} from "./user";

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

