/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {
  CandidateSource,
  PagedSearchRequest,
  SearchCandidateSourcesRequest
} from "./base";
import {isSavedSearch, SavedSearchRef} from "./saved-search";

export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe
}

export interface SavedList extends CandidateSource {
  savedSearchSource?: SavedSearchRef
}

export function isSavedList(source: CandidateSource): source is SavedList {
  return !isSavedSearch(source);
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
