/*
 * Copyright (c) 2024 Talent Catalog.
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
  PublishedDocColumnType,
  PublishedDocColumnWidth,
  SearchCandidateSourcesRequest
} from "./base";
import {isSavedSearch, SavedSearchRef} from "./saved-search";
import {UpdateCandidateStatusInfo} from "./candidate";
import {environment} from "../../environments/environment";
import {Task} from "./task";

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
  fileJdLink?: string;
  fileJdName?: string;
  fileJoiLink?: string;
  fileJoiName?: string;
  fileInterviewGuidanceLink?: string;
  fileInterviewGuidanceName?: string;
  fileMouLink?: string;
  fileMouName?: string;
  folderlink?: string;
  folderjdlink?: string;
  publishedDocLink?: string;
  tbbShortName?: string;
  sfJobCountry?: string;
  sfJobStage?: string;
  tasks?: Task[];
  registeredJob?: boolean;
}

export interface ShortSavedList {
  id: number;
  name: string;
}

export function externalDocLink(savedList: SavedList): string {
  return savedList?.tbbShortName ? environment.publishUrl + "/" + savedList.tbbShortName : null;
}

export function isSavedList(source: CandidateSource): source is SavedList {
  return source == null ? false : !isSavedSearch(source);
}

export function isSubmissionList(source: CandidateSource): source is SavedList {
  return isSavedList(source) && source.registeredJob && source.sfJobOpp != null;
}

export interface UpdateSavedListInfoRequest {
  name?: string;
  fixed?: boolean;
  registeredJob?: boolean;
  jobId?: number;
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

/**
 * See Java PublishedDocValueSource for documentation
 */
export class PublishedDocValueSource {
  fieldName?: string;
  propertyName?: string;
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

export class PublishedDocPropertySource extends PublishedDocValueSource {
  constructor(propertyName: string) {
    super();
    super.propertyName = propertyName;
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
   * The type of column. The default column type is display only.
   * Other types of column allow different types of feedback which we can process.
   */
  type: PublishedDocColumnType = PublishedDocColumnType.DisplayOnly;

  /**
   * Width of column best suited to display of the column data
   */
  width: PublishedDocColumnWidth = PublishedDocColumnWidth.Medium;

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
}

/**
 * See doc for corresponding Java class
 */
export class PublishedDocColumnConfig {
  columnProps: PublishedDocColumnProps;
  columnDef: PublishedDocColumnDef;
}

export class PublishedDocImportReport {
  message: string;
  numCandidates: number;
  numEmployerFeedbacks: number;
  numJobOffers: number;
  numNoJobOffers: number;
}

/**
 * See doc for corresponding Java class
 */
export class ExportColumn {
  key: string;
  properties?: PublishedDocColumnProps;
}

export class PublishListRequest {
  publishClosedOpps?: boolean;
  columns: PublishedDocColumnConfig[] = [];
}

export class SearchSavedListRequest extends SearchCandidateSourcesRequest {
  shortName?: boolean;
  registeredJob?: boolean;
  sfOppClosed?: boolean;
}

export class SavedListGetRequest extends PagedSearchRequest {
  keyword?: string;
  showClosedOpps?: boolean;
}

export interface UpdateSharingRequest {
  savedSearchId: number;
}

export interface UpdateShortNameRequest {
  savedListId: number;
  tbbShortName: string;
}
