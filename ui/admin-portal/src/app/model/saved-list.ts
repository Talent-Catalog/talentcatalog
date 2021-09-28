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
  publishedDocLink?: string;
}

export function isSavedList(source: CandidateSource): source is SavedList {
  return !isSavedSearch(source);
}

export interface UpdateSavedListInfoRequest {
  name?: string;
  fixed?: boolean;
  registeredJob?: boolean;
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

export class PublishedDocValueSource {
  fieldName?: string;
  constant?: any;
}

export class PublishedDocFieldSource extends PublishedDocValueSource {
  constructor(fieldName: string) {
    super();
    super.fieldName = fieldName;
  }
}

export class PublishedDocConstantSource extends PublishedDocValueSource {
  constructor(constant: any) {
    super();
    super.constant = constant;
  }
}

/**
 * See doc for corresponding Java class
 */
export class PublishedDocColumnContent {
  link?: PublishedDocValueSource;
  value?: PublishedDocValueSource;
}

export class PublishedDocColumnDef {
  /**
   * This is the unique key for this column.
   * This key is stored on the server in the exportColumns field as part of a ExportColumn object
   * associated with the candidate source.
   */
  key: string;

  /**
   * This is the name which is displayed to Angular users when they are deciding which columns
   * should appear in the published doc.
   * <p/>
   * Note that this is not sent down to the Spring Server - it is only used to display the column
   * to Angular users.
   */
  name: string;

  /**
   * This is the header for the column (it defaults to name)
   */
  header: string;

  /**
   * Defines the content of the cells in the column.
   */
  content: PublishedDocColumnContent = new PublishedDocColumnContent();

  /**
   *
   * @param key Unique for this column
   * @param name Name displayed to Angular user, also provides default header
   */
  constructor(key: string, name: string) {
    this.key = key;
    this.name = name;
    this.header = name;
  }
}

/**
 * See doc for corresponding Java class
 */
export class PublishedDocColumnProps {
  header: string;
  constant: string;
  //todo  Enumerated column type: noFeedback, employerCandidateNotes, employerDecision
}

/**
 * See doc for corresponding Java class
 */
export class PublishedDocColumnConfig {
  columnProps: PublishedDocColumnProps;
  columnDef: PublishedDocColumnDef;
}

/**
 * See doc for corresponding Java class
 */
export class ExportColumn {
  key: string;
  properties?: PublishedDocColumnProps;
}

export class PublishListRequest {
  columns: PublishedDocColumnConfig[] = [];
}

export class SearchSavedListRequest extends SearchCandidateSourcesRequest {

}

export class SavedListGetRequest extends PagedSearchRequest {
}

export interface UpdateSharingRequest {
  savedSearchId: number;
}
