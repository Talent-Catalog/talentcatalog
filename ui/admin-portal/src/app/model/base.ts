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

import {Role, User} from './user';
import {AuthService} from '../services/auth.service';
import {ExportColumn} from "./saved-list";
import {PartnerType} from "./partner";

export interface HasName {
  name?: string;
}

export enum CandidateSourceType {
  SavedList,
  SavedSearch
}

export enum Progress {
  NotStarted,
  Started,
  Finished
}

/**
 * Published columns can for display purposes only, or they can allow different kinds of data to be
 * entered in them (eg by employers) which we can process, interpreting the data depending on its
 * type.
 * This entered data may get imported back into the Talent Catalog data base or into Salesforce.
 */
export enum PublishedDocColumnType {
  DisplayOnly,
  EmployerCandidateNotes,
  EmployerCandidateDecision
}

/**
 * Published doc columns can be configured in three sizes based on their width.
 * <p/>
 * The width can be used to determine other automated formatting - eg alignment.
 */
export enum PublishedDocColumnWidth {
  /**
   * Narrow columns are good for small amounts of data - eg a candidate number, or a status.
   * <p/>
   * Automated formatting may choose to center align the values in narrow columns.
   */
  Narrow,

  /**
   * This is the default column width and does not need to be specified. It can be used for
   * candidate names, for example.
   * <p/>
   * It doesn't trigger any automated formatting. The defaults for the template from which a doc
   * is created will be used.
   */
  Medium,

  /**
   * Wide columns are good for holding descriptive text - eg candidate descriptions or
   * employer feedback.
   * <p/>
   * Automated formatting will typically choose to left justify the text.
   */
  Wide
}

export enum ReviewStatus {
  unverified,
  verified,
  rejected
}

/**
 * This restricts the data displayed in our browse tabs.
 * For example, if a tab is showing all my lists or all my searches - this would be "all".
 * Or if a tab is showing all searches of a given type (eg Profession, Job, Other), this would
 * be "type"
 */
export enum SearchBy {
  type,
  all,
  mine,
  sharedWithMe,
  watched,
  externalLink,
  registeredJob
}

export enum SearchJobsBy {
  all,
  mine,
  starredByMe,
  live
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

/**
 * Defines the start of a linkedIn profile URL.
 * Defined here in case the link structure changes so only needs changing in one place.
 */
export const linkedInUrl: string = 'https://www.linkedin.com/in/';

export const defaultReviewStatusFilter: string[] = [
  ReviewStatus[ReviewStatus.rejected]
];

export interface HasId {
  id?: number;
}

export function indexOfHasId(id: number, hasIds: HasId[]): number {
  for (let i = 0; i < hasIds.length; i++) {
    if (hasIds[i].id === id) {
      return i;
    }
  }
  return -1;
}

export function findHasId(id: number, hasIds: HasId[]): HasId {
  const idx = indexOfHasId(id, hasIds);
  return idx < 0 ? null : hasIds[idx];
}

export interface Auditable extends HasId {
  createdBy?: User;
  createdDate?: number;
  updatedBy?: User
  updatedDate?: number;
}

export interface CandidateSource extends Auditable {
  name: string;
  description?: string;
  displayedFieldsLong?: string[];
  displayedFieldsShort?: string[];
  exportColumns?: ExportColumn[];
  fixed: boolean;
  global: boolean;
  sfJobOpp?: OpportunityIds;
  users?: User[];
  watcherUserIds?: number[];
}

export interface OpportunityIds extends HasId {
  sfId?: string;
}

export interface Opportunity extends OpportunityIds {
  closingComments?: string;
  lastModifiedDate?: Date;
  name: string;
  nextStep?: string;
  nextStepDueDate?: Date;

}

export interface HasJobRelatedLinks {
  sfJoblink: string;
  listlink?: string;
  fileJdLink?: string;
  fileJdName?: string;
  fileJoiLink?: string;
  fileJoiName?: string;
  folderlink?: string;
  folderjdlink?: string;
}

export interface UpdateEmployerOpportunityRequest extends HasJobRelatedLinks {
}

export interface PostJobToSlackRequest extends HasJobRelatedLinks {
  jobName?: string;
}
export interface PostJobToSlackResponse {
  slackChannelUrl: string;
}

export class PagedSearchRequest {
  pageSize?: number;
  pageNumber?: number;
  sortFields?: string[];
  sortDirection?: string;
}

export class PagedFilteredSearchRequest extends PagedSearchRequest {
  keyword?: string;
  status?: string;
}

export class SearchPartnerRequest extends PagedFilteredSearchRequest {
  contextJobId?: number;
  partnerType?: PartnerType;
}

export class SearchUserRequest extends PagedFilteredSearchRequest {
  partnerId?: number;
  role?: Role;
}

export class SearchTaskRequest extends PagedFilteredSearchRequest {}

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

export class UpdateCandidateSourceDescriptionRequest {
  description: string;
}

export class UpdateDisplayedFieldPathsRequest {
  displayedFieldsLong?: string[];
  displayedFieldsShort?: string[];
}

export class LoginRequest {
  username: string;
  password: string;
  reCaptchaV3Token: string;

  /**
   * Time based One Time Password (TOTP) used for multi factor authentication
   */
  totpToken: string;
}

export function isMine(source: CandidateSource, auth: AuthService) {
  let mine: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (source && source.createdBy && me) {
    mine = source.createdBy.id === me.id;
  }
  return mine;
}

export function isStarredByMe(users: User[], auth: AuthService) {
  let starredByMe: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (users && me) {
    starredByMe = users.find(u => u.id === me.id ) !== undefined;
  }
  return starredByMe;
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

export enum Status {
  active = "active",
  inactive = "inactive",
  deleted = "deleted"
}
