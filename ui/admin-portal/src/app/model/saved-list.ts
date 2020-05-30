/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {User} from "./user";

export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe
}

export interface SavedList {
  id: number;
  name: string;
  fixed: boolean;
  users?: User[];
  watcherUserIds?: number[];
  createdBy?: User;
}

export function indexOfSavedList(savedListID: number, savedLists: SavedList[]): number {
  for (let i = 0; i < savedLists.length; i++) {
    if (savedLists[i].id === savedListID) {
      return i;
    }
  }
  return -1;
}

export interface CreateSavedListRequest {
  name: string;
  fixed?: boolean;
  candidateIds?: number[];
}

export interface IHasSetOfSavedLists {
  savedListIds: number[];
}

export interface IHasSetOfCandidates {
  candidateIds: number[];
}

export interface SearchSavedListRequest {
  keyword?: string;
  fixed?: boolean;
  owned?: boolean;
  shared?: boolean;
  pageSize?: number;
  pageNumber?: number;
  sortDirection?: string;
  sortFields?: string[];
}

export interface UpdateSavedListInfoRequest {
  name: string;
  fixed?: boolean;
}

export interface SavedListGetRequest {
  savedSearchId: number;
  pageNumber?: number;
  pageSize?: number;
  sortFields?: string[];
  sortDirection?: string;
}

export interface UpdateSharingRequest {
  savedSearchId: number;
}
