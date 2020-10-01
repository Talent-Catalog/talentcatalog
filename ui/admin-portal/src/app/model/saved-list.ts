/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {
  CandidateSource,
  PagedSearchRequest,
  SearchCandidateSourcesRequest
} from "./base";

export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe
}

export interface SavedList extends CandidateSource {
}

export function indexOfSavedList(savedListID: number, savedLists: SavedList[]): number {
  for (let i = 0; i < savedLists.length; i++) {
    if (savedLists[i].id === savedListID) {
      return i;
    }
  }
  return -1;
}

export interface CreateSavedListRequest extends UpdateSavedListInfoRequest {
  sourceListId?: number;
  candidateIds?: number[];
}

export interface UpdateSavedListInfoRequest {
  name: string;
  fixed?: boolean;
  sfJoblink?: string;
}

export interface IHasSetOfSavedLists {
  savedListIds: number[];
}

export interface IHasSetOfCandidates {
  sourceListId?: number;
  candidateIds: number[];
}

export class SearchSavedListRequest extends SearchCandidateSourcesRequest {

}

export class SavedListGetRequest extends PagedSearchRequest {
}

export interface UpdateSharingRequest {
  savedSearchId: number;
}
