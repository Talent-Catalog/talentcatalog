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
import {TargetListSelection} from "../components/list/select/select-list.component";
import {UpdateCandidateStatusInfo} from "./candidate";

export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe
}

export enum ContentUpdateType {
  add,
  delete,
  replace
}

export interface SavedList extends CandidateSource {
  savedSearchSource?: SavedSearchRef;
  folderlink?: string;
  foldercvlink?: string;
  folderjdlink?: string;
}

export function isSavedList(source: CandidateSource): source is SavedList {
  return !isSavedSearch(source);
}

export interface UpdateSavedListInfoRequest {
  name?: string;
  fixed?: boolean;
  sfJoblink?: string;
}

export interface UpdateSavedListContentsRequest extends UpdateSavedListInfoRequest {

  sourceListId?: number;

  statusUpdateInfo?: UpdateCandidateStatusInfo;

  updateType?: ContentUpdateType;

}

export interface UpdateExplicitSavedListContentsRequest extends UpdateSavedListContentsRequest {
  candidateIds: number[];
}

export interface CopySourceContentsRequest extends UpdateSavedListContentsRequest {
  savedListId: number;
  newListName?: string;
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
