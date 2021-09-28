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

import {User} from './user';
import {AuthService} from '../services/auth.service';
import {ExportColumn} from "./saved-list";

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
  displayOnly,
  employerCandidateNotes,
  employerCandidateDecision
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
  watched
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
  ReviewStatus[ReviewStatus.unverified],
  ReviewStatus[ReviewStatus.verified]
];

export interface HasId {
  id: number;
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
  sfJoblink?: string;
  users?: User[];
  watcherUserIds?: number[];
}

export interface Opportunity {
  name: string;
}

export interface HasJobRelatedLinks {
  sfJoblink: string;
  listlink?: string;
  folderlink?: string;
  foldercvlink?: string;
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

export function isSharedWithMe(source: CandidateSource, auth: AuthService) {
  let sharedWithMe: boolean = false;
  const me: User = auth.getLoggedInUser();
  if (source && me) {
    sharedWithMe = source.users.find(u => u.id === me.id ) !== undefined;
  }
  return sharedWithMe;
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

export function isAdminUser(auth: AuthService) {
  const loggedInUser =
    auth ? auth.getLoggedInUser() : null;
  const role = loggedInUser ? loggedInUser.role : null;
  return role !== 'semilimited' && role !== 'limited';
}

