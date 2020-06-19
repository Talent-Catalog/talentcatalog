import {User} from "./user";

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

export interface PagedSearchRequest {
  pageSize: number;
  pageNumber: number;
  sortFields: string[];
  sortDirection: string;
}
